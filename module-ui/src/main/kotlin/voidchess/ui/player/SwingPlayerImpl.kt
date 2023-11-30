package voidchess.ui.player

import voidchess.common.board.BasicChessGame
import voidchess.common.board.getFigure
import voidchess.common.board.move.*
import voidchess.common.board.other.Chess960Index
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.Pawn
import voidchess.common.integration.ColdPromise
import voidchess.common.integration.TableAdapter
import voidchess.ui.swing.ChessboardComponent
import voidchess.ui.swing.MarkedPositions
import voidchess.ui.swing.showErrorDialog
import java.util.concurrent.Executors
import javax.swing.JOptionPane
import javax.swing.SwingUtilities


internal class SwingPlayerImpl(
    private val ui: ChessboardComponent,
    private val game: BasicChessGame,
    private val tableAdapter: TableAdapter
) : BoardUiListener {

    private lateinit var gameUIDisable: EnableUI
    private lateinit var resignSetEnabled: EnableButton
    private var mouseHoverWhileInactivePos: Position? = null
    private var waitingState: WaitingFor = WaitingFor.GameToStart
    private var isWhitePlayer: Boolean = true
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private var computeMoveJob: ColdPromise<*>? = null

    fun postConstruct(disableGameUI: EnableUI, resignSetEnabled: EnableButton) {
        this.gameUIDisable = disableGameUI        // ChessPanel.stop
        this.resignSetEnabled = resignSetEnabled  // ChessPanel.enableResign
    }

    private fun move(move: Move) {
        game.move(move)
        val humanMoveResult = tableAdapter.move(move)
        ui.repaintAfterMove(humanMoveResult.extendedHumanMove)

        when(humanMoveResult) {
            is HumanMoveResult.GameEnds -> {
                gameEnds()
            }
            is HumanMoveResult.Ongoing -> {
                waitForComputer(humanMoveResult.computerMovePromise)
            }
        }
    }

    private fun waitForComputer(computerMovePromise: ColdPromise<ComputerMoveResult>) {
        waitingState = WaitingFor.MyTurn
        ui.updateMarkedPositions(MarkedPositions.None)

        singleThreadExecutor.submit {
            computeMoveJob = computerMovePromise
            computerMovePromise.computeAndCallback { computerMoveResult ->
                val computerMove: ComputerMoveResult = computerMoveResult.getOrElse { exception ->
                    val isPlayerResignedException = exception.message?.let {
                        it.contains("CancellationException") && it.contains(resignMsg)
                    } ?: false
                    if(!isPlayerResignedException) {
                        showErrorDialog(ui, exception)
                    }
                    return@computeAndCallback
                }
                computeMoveJob = null
                game.move(computerMove.extendedComputerMove.move)
                SwingUtilities.invokeLater {
                    ui.repaintAfterMove(computerMove.extendedComputerMove)

                    when (computerMove) {
                        is ComputerMoveResult.GameEnds -> {
                            gameEnds()
                        }
                        is ComputerMoveResult.Ongoing -> {
                            waitingState = WaitingFor.FromPos
                            mouseMovedOver(mouseHoverWhileInactivePos)
                        }
                    }
                }
            }
        }
    }

    private fun gameEnds() {
        waitingState = WaitingFor.GameToStart
        ui.updateMarkedPositions(MarkedPositions.None)
        gameUIDisable()
    }

    override fun mouseMovedOver(pos: Position?) {
        when(val currentState = waitingState) {
            is WaitingFor.FromPos -> {
                val markedPositions = if(pos==null || !game.isSelectable(pos)) {
                    MarkedPositions.None
                } else {
                    MarkedPositions.PossibleFrom(pos)
                }
                ui.updateMarkedPositions(markedPositions)
            }
            is WaitingFor.ToPos -> {
                val markedPositions = if( pos==null) {
                    MarkedPositions.From(currentState.from)
                } else {
                    when {
                        game.isMovable(currentState.from, pos) -> {
                            MarkedPositions.FromAndPossibleTo(currentState.from, pos)
                        }
                        game.isSelectable(pos) -> {
                            MarkedPositions.FromAndPossibleFrom(currentState.from, pos)
                        }
                        else -> MarkedPositions.From(currentState.from)
                    }
                }
                ui.updateMarkedPositions(markedPositions)
            }
            else -> {
                // not my move
                mouseHoverWhileInactivePos = pos
            }
        }
    }

    override fun mouseClickedOn(pos: Position) {
        when(val currentState = waitingState) {
            is WaitingFor.FromPos -> {
                if (game.isSelectable(pos)) {
                    waitingState = WaitingFor.ToPos(from = pos)
                    ui.updateMarkedPositions(MarkedPositions.From(pos))
                }
            }
            is WaitingFor.ToPos -> {
                val currentFrom = currentState.from
                if (game.isMovable(currentFrom, pos)) {
                    // check if move is a pawn transformation
                    val move: Move = if (game.getFigure(currentFrom) is Pawn && (pos.row == 0 || pos.row == 7)) {
                        val pawnPromotionType = askForPawnPromotionType()
                        Move[currentFrom, pos, pawnPromotionType]
                    } else {
                        Move[currentFrom, pos]
                    }
                    runCatching {
                        mouseHoverWhileInactivePos = move.to
                        move(move)
                    }.onFailure { exception ->
                        showErrorDialog(ui, exception)
                    }
                } else if(game.isSelectable(pos)) {
                    waitingState = WaitingFor.ToPos(from = pos)
                    ui.updateMarkedPositions(MarkedPositions.From(pos))
                }
            }
            else -> {} // not my move nothing to do
        }
    }

    private fun askForPawnPromotionType(): PawnPromotion {
        val figs = arrayOf("Queen", "Knight", "Rook", "Bishop")
        val out = JOptionPane.showInputDialog(
            ui,
            "Promote pawn to what type?",
            "pawn promotion",
            JOptionPane.QUESTION_MESSAGE, null,
            figs,
            "Queen"
        ) as String

        return when (out) {
            "Queen" -> PawnPromotion.QUEEN
            "Rook" -> PawnPromotion.ROOK
            "Knight" -> PawnPromotion.KNIGHT
            else -> PawnPromotion.BISHOP
        }
    }

    override fun switchPlayerSelected() {
        isWhitePlayer = !isWhitePlayer
        ui.setViewPoint(isWhitePlayer)
    }

    override fun resignSelected() {
        computeMoveJob?.cancel(resignMsg)
        computeMoveJob = null
        tableAdapter.resign()
        gameEnds()
    }

    override fun startSelected(chess960Index: Chess960Index) {
        val startConfig = StartConfig.Chess960Config(chess960Index)
//        val startConfig = StartConfig.ManualConfig(true, 5,
//            "King-white-g1-4 King-black-c5-9 Pawn-white-a6-false".split(" ")
//        )
        game.initGame(startConfig)
        ui.startNewGame()
        if(isWhitePlayer) {
            waitingState = WaitingFor.FromPos
            tableAdapter.humanStartsGame(startConfig)
        } else {
            waitingState = WaitingFor.MyTurn
            val computerMovePromise = tableAdapter.computerStartsGame(startConfig)
            waitForComputer(computerMovePromise)
        }
    }
}

typealias EnableUI = ()->Unit
typealias EnableButton = (Boolean)->Unit

private const val resignMsg = "player resigned"

private sealed class WaitingFor {
    data object GameToStart: WaitingFor()
    data object MyTurn: WaitingFor()
    data object FromPos: WaitingFor()
    class ToPos(val from: Position): WaitingFor()
}
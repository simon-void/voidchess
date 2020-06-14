package voidchess.ui.player

import voidchess.common.board.BasicChessGame
import voidchess.common.board.getFigure
import voidchess.common.board.move.*
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.Pawn
import voidchess.common.integration.ColdPromise
import voidchess.common.integration.TableAdapter
import voidchess.ui.swing.ChessboardComponent
import voidchess.ui.swing.PosType
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
    private var from: Position? = null
    private var isMyTurn: Boolean = false
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
        isMyTurn = false
        dropMarkedPositions()

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
                            isMyTurn = true
                            mouseMovedOver(mouseHoverWhileInactivePos)
                        }
                    }
                }
            }
        }
    }

    private fun gameEnds() {
        isMyTurn = false
        dropMarkedPositions()
        gameUIDisable()
    }

    private fun dropMarkedPositions() {
        from = null
        ui.unmarkPosition(PosType.HOVER_FROM)
        ui.unmarkPosition(PosType.SELECT_FROM)
        ui.unmarkPosition(PosType.HOVER_TO)
    }

    override fun mouseMovedOver(pos: Position?) {
        if (!isMyTurn) {
            mouseHoverWhileInactivePos = pos
            return
        }

        val lockedFrom = from
        if( pos==null) {
            ui.unmarkPosition(if(lockedFrom==null) PosType.HOVER_FROM else PosType.HOVER_TO)
            return
        }

        if (lockedFrom == null) {
            ui.unmarkPosition(PosType.HOVER_FROM)
            if (game.isSelectable(pos)) {
                ui.markPosition(pos, PosType.HOVER_FROM)
            }
        } else {
            ui.unmarkPosition(PosType.HOVER_TO)
            if (game.isMovable(lockedFrom, pos)) {
                ui.markPosition(pos, PosType.HOVER_TO)
            }
        }
    }

    override fun mouseClickedOn(pos: Position) {
        if (!isMyTurn) return

        val lockedFrom = from
        if (lockedFrom == null) {
            if (game.isSelectable(pos)) {
                from = pos
                ui.markPosition(pos, PosType.SELECT_FROM)
            }
        } else {
            if (game.isMovable(lockedFrom, pos)) {
                // check if move is a pawn transformation
                val move: Move = if (game.getFigure(lockedFrom) is Pawn && (pos.row == 0 || pos.row == 7)) {
                    val pawnPromotionType = askForPawnPromotionType()
                    Move[lockedFrom, pos, pawnPromotionType]
                } else {
                    Move[lockedFrom, pos]
                }
                runCatching {
                    move(move)
                }.onFailure { exception ->
                    showErrorDialog(ui,exception)
                }
            }
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

    override fun startSelected(chess960Index: Int) {
        val startConfig = StartConfig.Chess960Config(chess960Index)
//        val startConfig = StartConfig.ManualConfig(true, 5,
//            "King-white-g1-4 King-black-c5-9 Pawn-white-a6-false".split(" ")
//        )
        game.initGame(startConfig)
        ui.startNewGame()
        isMyTurn = isWhitePlayer
        if(isMyTurn) {
            tableAdapter.humanStartsGame(startConfig)
        } else {
            val computerMovePromise = tableAdapter.computerStartsGame(startConfig)
            waitForComputer(computerMovePromise)
        }
    }
}

typealias EnableUI = ()->Unit
typealias EnableButton = (Boolean)->Unit

private const val resignMsg = "player resigned"
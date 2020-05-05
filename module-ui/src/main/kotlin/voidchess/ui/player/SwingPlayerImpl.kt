package voidchess.ui.player

import voidchess.common.board.BasicChessGame
import voidchess.common.board.getFigure
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.Pawn
import voidchess.common.integration.HumanPlayer
import voidchess.common.integration.TableAdapter
import voidchess.ui.swing.ChessboardComponent
import voidchess.ui.swing.PosType
import javax.swing.JOptionPane


internal class SwingPlayerImpl(
    private val ui: ChessboardComponent,
    private val game: BasicChessGame,
    private val tableAdapter: TableAdapter
) : BoardUiListener, HumanPlayer {

    private lateinit var panelEnable: EnableUI
    private var mouseHoverWhileInactivePos: Position? = null
    private var from: Position? = null
    private var isMyTurn: Boolean = false
    private var isWhitePlayer: Boolean = true

    fun postConstruct(panelEnable: EnableUI) {
        this.panelEnable = panelEnable
    }

    override fun playFirstMove() {
        play()
    }

    override fun playAfter(opponentsMove: ExtendedMove) {
        game.move(opponentsMove.move)
        ui.repaintAfterMove(opponentsMove)
        play()
    }

    private fun play() {
        isMyTurn = true
        mouseMovedOver(mouseHoverWhileInactivePos)
    }

    fun move(move: Move) {
        game.move(move)
        dropMarkedPositions()
        isMyTurn = false
        val extendedMove = tableAdapter.moved(move)
        ui.repaintAfterMove(extendedMove)
    }

    override fun gameEnds() {
        isMyTurn = false
        dropMarkedPositions()
        panelEnable()
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
                move(move)
            }
        }
    }

    private fun askForPawnPromotionType(): PawnPromotion {
        val figs = arrayOf("Queen", "Knight", "Rook", "Bishop")
        val out = JOptionPane.showInputDialog(null,
            "Promote pawn to what type?",
            "pawn promotion",
            JOptionPane.QUESTION_MESSAGE, null,
            figs,
            "Queen") as String

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
        tableAdapter.resign()
    }

    override fun startSelected(chess960Index: Int) {
        game.initGame(StartConfig.Chess960Config(chess960Index))
        ui.startNewGame()
        tableAdapter.startGame(StartConfig.Chess960Config(chess960Index), isWhitePlayer)
    }
}

typealias EnableUI = ()->Unit
package voidchess.player.human

import voidchess.board.CentralChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.ui.ChessboardComponent
import voidchess.ui.PosType
import voidchess.ui.TableInterface

import javax.swing.*


class HumanPlayer(
        private val table: TableInterface,
        private var isWhitePlayer: Boolean,
        private val ui: ChessboardComponent,
        private val game: CentralChessGame
) : HumanPlayerInterface {
    private var mouseHoverWhileInactivePos: Position? = null
    private var from: Position? = null
    private var isMyTurn: Boolean = false

    init {
        ui.setPlayer(this)
    }

    override fun play() {
        isMyTurn = true
        mouseMovedOver(mouseHoverWhileInactivePos)
    }

    fun move(move: Move) {
        dropMarkedPositions()
        isMyTurn = false
        table.move(move)
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
                move(Move[lockedFrom, pos])
            }
        }
    }

    override fun askForPawnPromotionType(pawnPosition: Position): PawnPromotion {
        val figs = arrayOf("Queen", "Rook", "Bishop", "Knight")
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

    override fun gameStarts() {
        isMyTurn = isWhitePlayer
    }

    override fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean) {
        isMyTurn = false
        dropMarkedPositions()
    }

    override fun gaveCheck() {
    }

    private fun dropMarkedPositions() {
        from = null
        ui.unmarkPosition(PosType.HOVER_FROM)
        ui.unmarkPosition(PosType.SELECT_FROM)
        ui.unmarkPosition(PosType.HOVER_TO)
    }

    override fun setColor(isWhite: Boolean) {
        isWhitePlayer = isWhite
        ui.setViewPoint(isWhite)
    }
}
package voidchess.player

import voidchess.board.ChessGameInterface
import voidchess.helper.Move
import voidchess.helper.PawnPromotion
import voidchess.helper.Position
import voidchess.ui.ChessboardUI
import voidchess.ui.TableInterface

import javax.swing.*


class HumanPlayer(
        private val table: TableInterface,
        private var isWhitePlayer: Boolean,
        private val ui: ChessboardUI,
        private val game: ChessGameInterface
) : HumanPlayerInterface {
    private var from: Position? = null
    private var isMyTurn: Boolean = false

    init {
        ui.setPlayer(this)
    }

    override fun play() {
        isMyTurn = true
    }

    fun move(move: Move) {
        dropMarkedPositions()
        isMyTurn = false
        table.move(move)
    }

    override fun mouseMovedOver(pos: Position?) {
        if (!isMyTurn) return

        val lockedFrom = from
        if( pos==null) {
            ui.markPosition(null, lockedFrom==null)
            return
        }

        if (lockedFrom == null) {
            if (game.isSelectable(pos, isWhitePlayer)) {
                ui.markPosition(pos, true)
            } else {
                ui.markPosition(null, true)
            }
        } else {
            if (game.isMoveable(lockedFrom, pos, isWhitePlayer)) {
                ui.markPosition(pos, false)
            } else {
                ui.markPosition(null, false)
            }
        }
    }

    override fun mouseClickedOn(pos: Position) {
        if (!isMyTurn) return

        val lockedFrom = from
        if (lockedFrom == null) {
            if (game.isSelectable(pos, isWhitePlayer)) {
                from = pos
            }
        } else {
            if (game.isMoveable(lockedFrom, pos, isWhitePlayer)) {
                move(Move.get(lockedFrom, pos))
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

    override fun setIsPlaying(isPlaying: Boolean) {
        isMyTurn = isPlaying && isWhitePlayer

        if (!isPlaying) {
            //removes selected fields even if game was aborted
            dropMarkedPositions()
        }
    }

    private fun dropMarkedPositions() {
        from = null
        ui.markPosition(null, true)
        ui.markPosition(null, false)
    }

    override fun setColor(isWhite: Boolean) {
        isWhitePlayer = isWhite
    }
}

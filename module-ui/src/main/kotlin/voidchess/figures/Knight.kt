package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.collections.ArrayList
import kotlin.math.abs


class Knight(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition,
    FigureType.KNIGHT, false, false) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val horizontalDifference = abs(position.row - toPos.row)
        val verticalDifference = abs(position.column - toPos.column)

        if((horizontalDifference != 2 || verticalDifference != 1) && (horizontalDifference != 1 || verticalDifference != 2)) {
            return false
        }

        val figure = game.getFigureOrNull(toPos)
        return figure == null || hasDifferentColor(figure)
    }

    override fun getReachableMoves(game: BasicChessBoard): Collection<Move> = ArrayList<Move>(8).apply {
        forEachReachablePos(game) {
            add(Move[position, it])
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            if(isAccessible(game, pos)) informOf(pos)
        }
    }
}

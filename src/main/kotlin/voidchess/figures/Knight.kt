package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.move.Move
import voidchess.board.move.Position


class Knight(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.KNIGHT, false, false) {

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val horizontalDifference = Math.abs(position.row - to.row)
        val verticalDifference = Math.abs(position.column - to.column)

        if (horizontalDifference + verticalDifference != 3 || horizontalDifference == 0 || verticalDifference == 0) {
            return false
        }

        val figure = game.getFigureOrNull(to)
        return figure == null || hasDifferentColor(figure)
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move.get(position, it))
        }
    }

    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessGameInterface): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            if(isAccessible(game, pos)) informOf(pos)
        }
    }

    private fun isAccessible(game: BasicChessGameInterface, position: Position): Boolean {
        val figure = game.getFigureOrNull(position)
        return if (figure == null) true else hasDifferentColor(figure)
    }
}

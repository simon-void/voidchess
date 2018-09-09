package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.Move
import voidchess.helper.Position

/**
 * @author stephan
 */
class Knight(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.KNIGHT) {

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val horizontalDifference = Math.abs(position.row - to.row)
        val verticalDifference = Math.abs(position.column - to.column)

        if (horizontalDifference + verticalDifference != 3 || horizontalDifference == 0 || verticalDifference == 0) {
            return false
        }

        val figure = game.getFigure(to)
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
        posOffset(position, 2, 1)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, 2, -1)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, -2, 1)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, -2, -1)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, 1, 2)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, 1, -2)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, -1, 2)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
        posOffset(position, -1, -2)?.let {pos: Position ->
            if(isAccessable(game, pos)) informOf(pos)
        }
    }

    private fun posOffset(from: Position, rowOffset: Int, columnOffset: Int): Position? {
        val toRow = from.row + rowOffset
        if(toRow !in 0..7) return null
        val toColumn = from.column + columnOffset
        if(toColumn !in 0..7) return null

        return Position.get(toRow, toColumn)
    }

    private fun isAccessable(game: BasicChessGameInterface, position: Position): Boolean {
        val figure = game.getFigure(position)
        return if (figure == null) true else hasDifferentColor(figure)
    }
}

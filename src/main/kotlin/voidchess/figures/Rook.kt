package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.Direction
import voidchess.helper.Move
import voidchess.helper.Position


class Rook : CastlingFigure {

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.ROOK)
    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int) : super(isWhite, startPosition, stepsTaken, FigureType.ROOK)

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(to)

        if (direction == null || direction.isDiagonal) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(to)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
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
}

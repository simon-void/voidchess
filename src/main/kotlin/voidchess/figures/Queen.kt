package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Queen(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.QUEEN, true, true) {

    override fun isReachable(toPos: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(toPos) ?: return false

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: SimpleChessBoardInterface, checkLine: CheckLine, result: MutableList<Move>) {
        // a queen can intersect a check at up to three positions. but the overhead to check this upper bound is probably to high
        for(checkInterceptPos in checkLine) {
            addMoveIfReachable(checkInterceptPos, game, result)
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: SimpleChessBoardInterface, boundLine: BoundLine, result: MutableList<Move>) {
        for(posBetweenThisAndAttacker in boundLine.possibleMovesToAttacker) {
            result.add(Move[position, posBetweenThisAndAttacker])
        }
        for(posBetweenThisAndKing in boundLine.possibleMovesToKing) {
            result.add(Move[position, posBetweenThisAndKing])
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

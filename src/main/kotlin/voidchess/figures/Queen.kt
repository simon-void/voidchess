package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Queen(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.QUEEN, true, true) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos) ?: return false

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessBoard, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableList<Move>) {
        // a queen can intersect a check at up to three positions. but the overhead to check this upper bound is probably to high
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            addMoveIfReachable(checkInterceptPos, game, result)
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableList<Move>) {
        boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
            result.add(Move[position, posBetweenThisAndAttacker])
        }
        boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
            result.add(Move[position, posBetweenThisAndKing])
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }
}

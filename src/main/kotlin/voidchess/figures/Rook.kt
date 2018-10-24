package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Rook : CastlingFigure {

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.ROOK)
    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int) : super(isWhite, startPosition, stepsTaken, FigureType.ROOK)

    override fun isReachable(toPos: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isDiagonal) {
            return false
        }

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
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: SimpleChessBoardInterface, checkLine: CheckLine, result: MutableList<Move>) {
        when {
            checkLine.hasSingleInterceptPos -> {
                addMoveIfReachable(checkLine.attackerPos, game, result)
            }
            checkLine.isDiagonalCheck -> {
                var hasAlreadyAddedAPosition = false
                for(diagonalPos in checkLine) {
                    if(addMoveIfReachable(diagonalPos, game, result)) {
                        // a rook can only intersect with a diagonal attacker at max two points
                        if(hasAlreadyAddedAPosition) return
                        else hasAlreadyAddedAPosition = true
                    }
                }
            }
            else -> { // isStraightCheck!
                for(straightPos in checkLine) {
                    if(addMoveIfReachable(straightPos, game, result)) {
                        // a rook can only intersect with a straight attacker at one point
                        return
                    }
                }
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: SimpleChessBoardInterface, boundLine: BoundLine, result: MutableList<Move>) {
        if(boundLine.boundFigureToAttackerDirection.isStraight) {
            for(posBetweenThisAndAttacker in boundLine.possibleMovesToAttacker) {
                result.add(Move[position, posBetweenThisAndAttacker])
            }
            for(posBetweenThisAndKing in boundLine.possibleMovesToKing) {
                result.add(Move[position, posBetweenThisAndKing])
            }
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

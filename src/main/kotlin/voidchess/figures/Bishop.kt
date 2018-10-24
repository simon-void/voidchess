package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Bishop(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.BISHOP, true, false) {

    override fun isReachable(toPos: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isStraight) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
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
        when {
            checkLine.hasSingleInterceptPos -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    addMoveIfReachable(checkLine.attackerPos, game, result)
                }
            }
            checkLine.isDiagonalCheck -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    for(diagonalPos in checkLine) {
                        if(addMoveIfReachable(diagonalPos, game, result)) {
                            // a bishop can only intersect with a diagonal attacker at a single point
                            return
                        }
                    }
                }
            }
            else -> { // isStraightCheck!
                var hasAlreadyAddedAPosition = false
                for(straightPos in checkLine) {
                    if(position.hasSameColor(straightPos)) {
                        if(addMoveIfReachable(straightPos, game, result)) {
                            // a bishop can only intersect with a straight attacker at max two points
                            if(hasAlreadyAddedAPosition) return
                            else hasAlreadyAddedAPosition = true
                        }
                    }
                }
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: SimpleChessBoardInterface, boundLine: BoundLine, result: MutableList<Move>) {
        if(boundLine.boundFigureToAttackerDirection.isDiagonal) {
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
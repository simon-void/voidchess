package voidchess.common.figures

import voidchess.common.board.ChessBoard
import voidchess.common.board.StaticChessBoard
import voidchess.common.inner.board.check.BoundLine
import voidchess.common.inner.board.check.CheckLine
import voidchess.common.board.getKing
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Bishop(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.BISHOP, true, false) {

    override fun isReachable(toPos: Position, game: StaticChessBoard): Boolean {
        if(!position.hasSameColor(toPos)) {
            return false
        }
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isStraight) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    private inline fun forEachReachableTakeableOrCheckEndPos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP_RIGHT, informOf)
        forReachableTakeableEndPos(game, Direction.UP_LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN_RIGHT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun forReachableMoves(game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableTakingMoves(game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachableTakeableOrCheckEndPos(game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKing(!isWhite).position
        val currentPos = position
        if(!currentPos.hasSameColor(opponentKingPos)) {
            return
        }
        fun checkReachOf(row: Int, column:Int) {
            if(Position.inBounds(row, column)) {
                val checkPos = Position[row, column]
                if(isReachable(checkPos, game)) {
                    if(game.simulateSimplifiedMove(this, checkPos) { boardAfterMove ->
                            isReachable(opponentKingPos, boardAfterMove)}
                    ) {
                        informOf(Move[currentPos, checkPos])
                    }
                }
            }
        }
        val halfSum = (currentPos.row + currentPos.column + opponentKingPos.row + opponentKingPos.column) shr 1
        checkReachOf(halfSum-currentPos.column, halfSum-currentPos.row)
        checkReachOf(halfSum-opponentKingPos.column, halfSum-opponentKingPos.row)
    }

    override fun forPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    addMoveIfReachable(checkLine.attackerPos, game, informOf)
                }
            }
            checkLine.isDiagonalCheck -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    checkLine.posProgression.forEachReachablePos { diagonalPos->
                        if(addMoveIfReachable(diagonalPos, game, informOf)) {
                            // a bishop can only intersect with a diagonal attacker at a single point
                            return
                        }
                    }
                }
            }
            else -> { // isStraightCheck!
                var hasAlreadyAddedAPosition = false
                checkLine.posProgression.forEachReachablePos { straightPos->
                    if(position.hasSameColor(straightPos)) {
                        if(addMoveIfReachable(straightPos, game, informOf)) {
                            // a bishop can only intersect with a straight attacker at max two points
                            if(hasAlreadyAddedAPosition) return
                            else hasAlreadyAddedAPosition = true
                        }
                    }
                }
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) {
        if(boundLine.boundFigureToAttackerDirection.isDiagonal) {
            boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
                informOf(Move[position, posBetweenThisAndAttacker])
            }
            boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
                informOf(Move[position, posBetweenThisAndKing])
            }
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: StaticChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }
}
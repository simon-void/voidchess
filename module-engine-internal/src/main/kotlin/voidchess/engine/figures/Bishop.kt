package voidchess.engine.figures

import voidchess.engine.board.BasicChessBoard
import voidchess.engine.board.ChessBoard
import voidchess.engine.board.check.BoundLine
import voidchess.engine.board.check.CheckLine
import voidchess.engine.board.getKing
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


internal class Bishop(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.BISHOP, true, false) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
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

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    private inline fun forEachReachableTakeableOrCheckEndPos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP_RIGHT, informOf)
        forReachableTakeableEndPos(game, Direction.UP_LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN_RIGHT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessBoard, result: MutableCollection<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getReachableTakingMoves(game: BasicChessBoard, result: MutableCollection<Move>) {
        forEachReachableTakeableOrCheckEndPos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getReachableCheckingMoves(game: ChessBoard, result: MutableCollection<Move>) {
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
                        result.add(Move[currentPos, checkPos])
                    }
                }
            }
        }
        val halfSum = (currentPos.row + currentPos.column + opponentKingPos.row + opponentKingPos.column) shr 1
        checkReachOf(halfSum-currentPos.column, halfSum-currentPos.row)
        checkReachOf(halfSum-opponentKingPos.column, halfSum-opponentKingPos.row)
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    addMoveIfReachable(checkLine.attackerPos, game, result)
                }
            }
            checkLine.isDiagonalCheck -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    checkLine.posProgression.forEachReachablePos { diagonalPos->
                        if(addMoveIfReachable(diagonalPos, game, result)) {
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

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>) {
        if(boundLine.boundFigureToAttackerDirection.isDiagonal) {
            boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
                result.add(Move[position, posBetweenThisAndAttacker])
            }
            boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
                result.add(Move[position, posBetweenThisAndKing])
            }
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
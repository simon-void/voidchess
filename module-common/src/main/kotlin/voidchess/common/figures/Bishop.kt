package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKingPos
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Bishop(isWhite: Boolean) : Figure(
    isWhite = isWhite,
    type = FigureType.BISHOP,
) {

    override fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        if(!position.hasSameColor(toPos)) {
            return false
        }
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isStraight) {
            return false
        }

        forEachReachablePos(position, game, direction) { reachablePos ->
            if (reachablePos.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(position, game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(position, game, Direction.UP_LEFT, informOf)
        forEachReachablePos(position, game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(position, game, Direction.DOWN_LEFT, informOf)
    }

    private inline fun forEachReachableTakeableOrCheckEndPos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(position, game, Direction.UP_RIGHT, informOf)
        forReachableTakeableEndPos(position, game, Direction.UP_LEFT, informOf)
        forReachableTakeableEndPos(position, game, Direction.DOWN_RIGHT, informOf)
        forReachableTakeableEndPos(position, game, Direction.DOWN_LEFT, informOf)
    }

    override fun forReachableMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableTakingMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachableTakeableOrCheckEndPos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(position: Position, game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKingPos(!isWhite)
        if(!position.hasSameColor(opponentKingPos)) {
            return
        }
        fun checkReachOf(row: Int, column:Int) {
            if(Position.inBounds(row, column)) {
                val checkPos = Position[row, column]
                if(isReachable(position, checkPos, game)) {
                    if(game.simulateSimplifiedMove(this, checkPos) { boardAfterMove ->
                            isReachable(position, opponentKingPos, boardAfterMove)}
                    ) {
                        informOf(Move[position, checkPos])
                    }
                }
            }
        }
        val halfSum = (position.row + position.column + opponentKingPos.row + opponentKingPos.column) shr 1
        checkReachOf(halfSum - position.column, halfSum - position.row)
        checkReachOf(halfSum-opponentKingPos.column, halfSum-opponentKingPos.row)
    }

    override fun forPossibleMovesWhileUnboundAndCheck(
        position: Position,
        game: ChessBoard,
        checkLine: CheckLine,
        informOf: MoveInformer
    ) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    addMoveIfReachable(position, checkLine.attackerPos, game, informOf)
                }
            }
            checkLine.isDiagonalCheck -> {
                if(position.hasSameColor(checkLine.attackerPos)) {
                    checkLine.posProgression.forEachReachablePos { diagonalPos->
                        if(addMoveIfReachable(position, diagonalPos, game, informOf)) {
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
                        if(addMoveIfReachable(position, straightPos, game, informOf)) {
                            // a bishop can only intersect with a straight attacker at max two points
                            if(hasAlreadyAddedAPosition) return
                            else hasAlreadyAddedAPosition = true
                        }
                    }
                }
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(
        position: Position,
        game: ChessBoard,
        boundLine: BoundLine,
        informOf: MoveInformer
    ) {
        if(boundLine.boundFigureToAttackerDirection.isDiagonal) {
            boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
                informOf(Move[position, posBetweenThisAndAttacker])
            }
            boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
                informOf(Move[position, posBetweenThisAndKing])
            }
        }
    }

    override fun isSelectable(position: Position, game: ChessBoard): Boolean {
        forEachReachablePos(position, game) { reachablePos ->
            if (!isBound(position, reachablePos, game)) return true
        }
        return false
    }

    override fun countReachableMoves(position: Position, game: StaticChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(position, game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }
}
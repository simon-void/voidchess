package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKingPos
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Rook(isWhite: Boolean) : CastlingFigure(
    isWhite = isWhite,
    type = FigureType.ROOK,
) {

    override fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isDiagonal) {
            return false
        }

        forEachReachablePos(position, game, direction) { reachablePos ->
            if (reachablePos.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(position, game, Direction.UP, informOf)
        forEachReachablePos(position, game, Direction.LEFT, informOf)
        forEachReachablePos(position, game, Direction.DOWN, informOf)
        forEachReachablePos(position, game, Direction.RIGHT, informOf)
    }

    private inline fun forEachReachableTakeableEndPos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(position, game, Direction.UP, informOf)
        forReachableTakeableEndPos(position, game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(position, game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(position, game, Direction.RIGHT, informOf)
    }

    override fun forReachableMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableTakingMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachableTakeableEndPos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(position: Position, game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKingPos(!isWhite)
        val possiblePos1 = Position[position.row, opponentKingPos.column]
        val possiblePos2 = Position[opponentKingPos.row, position.column]
        // check position 1
        if (isReachable(position, possiblePos1, game)) {
            if (game.simulateSimplifiedMove(this, possiblePos1) { boardAfterMove ->
                    isReachable(position, opponentKingPos, boardAfterMove)}
            ) {
                informOf(Move[position, possiblePos1])
            }
        }
        // check position 2
        if (isReachable(position, possiblePos2, game)) {
            if (game.simulateSimplifiedMove(this, possiblePos2) { boardAfterMove ->
                    isReachable(position, opponentKingPos, boardAfterMove)}
            ) {
                informOf(Move[position, possiblePos2])
            }
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(position: Position, game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                addMoveIfReachable(position, checkLine.attackerPos, game, informOf)
            }
            checkLine.isDiagonalCheck -> {
                var hasAlreadyAddedAPosition = false
                checkLine.posProgression.forEachReachablePos {diagonalPos->
                    if(addMoveIfReachable(position, diagonalPos, game, informOf)) {
                        // a rook can only intersect with a diagonal attacker at max two points
                        if(hasAlreadyAddedAPosition) return
                        else hasAlreadyAddedAPosition = true
                    }
                }
            }
            else -> { // isStraightCheck!
                checkLine.posProgression.forEachReachablePos {straightPos->
                    if(addMoveIfReachable(position, straightPos, game, informOf)) {
                        // a rook can only intersect with a straight attacker at one point
                        return
                    }
                }
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(position: Position, game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) {
        if(boundLine.boundFigureToAttackerDirection.isStraight) {
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

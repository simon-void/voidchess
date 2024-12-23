package voidchess.common.figures

import voidchess.common.board.ChessBoard
import voidchess.common.board.StaticChessBoard
import voidchess.common.inner.board.check.BoundLine
import voidchess.common.inner.board.check.CheckLine
import voidchess.common.board.getKing
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Rook : CastlingFigure {

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.ROOK)
    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int) : super(isWhite, startPosition, stepsTaken, FigureType.ROOK)

    override fun isReachable(toPos: Position, game: StaticChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isDiagonal) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
    }

    private inline fun forEachReachableTakeableEndPos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP, informOf)
        forReachableTakeableEndPos(game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(game, Direction.RIGHT, informOf)
    }

    override fun forReachableMoves(game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableTakingMoves(game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachableTakeableEndPos(game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKing(!isWhite).position
        val currentPos = position
        val possiblePos1 = Position[currentPos.row, opponentKingPos.column]
        val possiblePos2 = Position[opponentKingPos.row, currentPos.column]
        // check position 1
        if (isReachable(possiblePos1, game)) {
            if (game.simulateSimplifiedMove(this, possiblePos1) { boardAfterMove ->
                    isReachable(opponentKingPos, boardAfterMove)}
            ) {
                informOf(Move[currentPos, possiblePos1])
            }
        }
        // check position 2
        if (isReachable(possiblePos2, game)) {
            if (game.simulateSimplifiedMove(this, possiblePos2) { boardAfterMove ->
                    isReachable(opponentKingPos, boardAfterMove)}
            ) {
                informOf(Move[currentPos, possiblePos2])
            }
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                addMoveIfReachable(checkLine.attackerPos, game, informOf)
            }
            checkLine.isDiagonalCheck -> {
                var hasAlreadyAddedAPosition = false
                checkLine.posProgression.forEachReachablePos {diagonalPos->
                    if(addMoveIfReachable(diagonalPos, game, informOf)) {
                        // a rook can only intersect with a diagonal attacker at max two points
                        if(hasAlreadyAddedAPosition) return
                        else hasAlreadyAddedAPosition = true
                    }
                }
            }
            else -> { // isStraightCheck!
                checkLine.posProgression.forEachReachablePos {straightPos->
                    if(addMoveIfReachable(straightPos, game, informOf)) {
                        // a rook can only intersect with a straight attacker at one point
                        return
                    }
                }
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) {
        if(boundLine.boundFigureToAttackerDirection.isStraight) {
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

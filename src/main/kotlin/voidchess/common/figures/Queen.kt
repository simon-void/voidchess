package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKing
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Queen(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.QUEEN, true, true) {

    override fun isReachable(toPos: Position, game: StaticChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos) ?: return false

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
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    private inline fun forEachReachableTakeableEndPos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP, informOf)
        forReachableTakeableEndPos(game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(game, Direction.RIGHT, informOf)
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
        forEachReachableTakeableEndPos(game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKing(!isWhite).position
        val currentPos = position
        forEachReachablePos(game) { checkingPos ->
            if (isReachable(checkingPos, game)) {
                if(game.simulateSimplifiedMove(this, checkingPos) { boardAfterMove ->
                        isReachable(opponentKingPos, boardAfterMove)}
                ) {
                    informOf(Move[currentPos, checkingPos])
                }
            }
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) {
        // a queen can intersect a check at up to three positions. but the overhead to check this upper bound is probably to high
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            addMoveIfReachable(checkInterceptPos, game, informOf)
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) {
        boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
            informOf(Move[position, posBetweenThisAndAttacker])
        }
        boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
            informOf(Move[position, posBetweenThisAndKing])
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

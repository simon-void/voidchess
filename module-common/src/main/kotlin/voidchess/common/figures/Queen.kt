package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKingPos
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class Queen(isWhite: Boolean) : Figure(
    isWhite = isWhite,
    type = FigureType.QUEEN,
) {

    override fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos) ?: return false

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
        forEachReachablePos(position, game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(position, game, Direction.UP_LEFT, informOf)
        forEachReachablePos(position, game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(position, game, Direction.DOWN_LEFT, informOf)
    }

    private inline fun forEachReachableTakeableEndPos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(position, game, Direction.UP, informOf)
        forReachableTakeableEndPos(position, game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(position, game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(position, game, Direction.RIGHT, informOf)
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
        forEachReachableTakeableEndPos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableCheckingMoves(position: Position, game: ChessBoard, informOf: MoveInformer) {
        val opponentKingPos = game.getKingPos(!isWhite)
        forEachReachablePos(position, game) { checkingPos ->
            if (isReachable(position, checkingPos, game)) {
                if(game.simulateSimplifiedMove(this, checkingPos) { boardAfterMove ->
                        isReachable(position, opponentKingPos, boardAfterMove)}
                ) {
                    informOf(Move[position, checkingPos])
                }
            }
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(position: Position, game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) {
        // a queen can intersect a check at up to three positions. but the overhead to check this upper bound is probably to high
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            addMoveIfReachable(position, checkInterceptPos, game, informOf)
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(position: Position, game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) {
        boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
            informOf(Move[position, posBetweenThisAndAttacker])
        }
        boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
            informOf(Move[position, posBetweenThisAndKing])
        }
    }

    override fun isSelectable(position: Position, game: ChessBoard): Boolean {
        forEachReachablePos(position, game) {
            if (!isBound(position, it, game)) return true
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

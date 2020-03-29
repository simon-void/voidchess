package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.getKing
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


internal class Queen(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.QUEEN, true, true) {

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

    private inline fun forEachReachableTakeableEndPos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP, informOf)
        forReachableTakeableEndPos(game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(game, Direction.RIGHT, informOf)
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
        forEachReachableTakeableEndPos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getReachableCheckingMoves(game: ChessBoard, result: MutableCollection<Move>) {
        val opponentKingPos = game.getKing(!isWhite).position
        val currentPos = position
        forEachReachablePos(game) {
            if(isReachable(it, game)) {
                val figureTaken = game.move(this, it)
                if(isReachable(opponentKingPos, game)) {
                    result.add(Move[currentPos, it])
                }
                game.undoMove(this, currentPos, figureTaken)
            }
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>) {
        // a queen can intersect a check at up to three positions. but the overhead to check this upper bound is probably to high
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            addMoveIfReachable(checkInterceptPos, game, result)
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>) {
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

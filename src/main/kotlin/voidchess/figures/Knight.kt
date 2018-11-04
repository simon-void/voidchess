package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Move
import voidchess.board.move.Position


class Knight(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.KNIGHT, false, false) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val horizontalDifference = Math.abs(position.row - toPos.row)
        val verticalDifference = Math.abs(position.column - toPos.column)

        if (horizontalDifference + verticalDifference != 3 || horizontalDifference == 0 || verticalDifference == 0) {
            return false
        }

        val figure = game.getFigureOrNull(toPos)
        return figure == null || hasDifferentColor(figure)
    }

    override fun getReachableMoves(game: BasicChessBoard, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableList<Move>) {
        // the accessibility of the target field doesn't need to be checked because
        // all checkInterceptPositions are guaranteed to be either empty
        // or to contain the attacker (who has a different color)
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            val horizontalDifference = Math.abs(position.row - checkInterceptPos.row)
            val verticalDifference = Math.abs(position.column - checkInterceptPos.column)
            if(horizontalDifference+verticalDifference==3 && horizontalDifference!=0 && verticalDifference!=0) {
                result.add(Move[position, checkInterceptPos])
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableList<Move>) {
        // a bound move can't move at all!
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

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            if(isAccessible(game, pos)) informOf(pos)
        }
    }
}

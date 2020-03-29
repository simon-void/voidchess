package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import java.util.*


class Rook : CastlingFigure {

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition,
        FigureType.ROOK
    )
    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int) : super(isWhite, startPosition, stepsTaken,
        FigureType.ROOK
    )

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isDiagonal) {
            return false
        }

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
    }

    override fun getReachableMoves(game: BasicChessBoard): Collection<Move> = ArrayList<Move>(14).apply {
        forEachReachablePos(game) {
            add(Move[position, it])
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }
}

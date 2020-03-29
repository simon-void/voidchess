package voidchess.board.move

import voidchess.common.board.move.Move
import voidchess.figures.Figure


class ExtendedMove(val move: Move,
                   val figureTaken: Figure?,
                   val enpassantPawnOrCastlingRook: Figure?,
                   val colorOfMove: Boolean,
                   val isCastling: Boolean,
                   val isEnPassant: Boolean,
                   val isPawnTransformation: Boolean) {

    @get:JvmName("wasFigureTaken")
    val wasFigureTaken = figureTaken != null || isEnPassant

    override fun toString() = move.toString()
}

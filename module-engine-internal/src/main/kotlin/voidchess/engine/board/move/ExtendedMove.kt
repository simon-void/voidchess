package voidchess.engine.board.move

import voidchess.common.board.move.Move
import voidchess.engine.figures.Figure


internal class ExtendedMove(val move: Move,
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

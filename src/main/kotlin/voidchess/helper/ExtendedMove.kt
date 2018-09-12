package voidchess.helper

import voidchess.figures.Figure

/**
 * @author stephan
 */
class ExtendedMove(val move: Move,
                   val figureTaken: Figure?,
                   val enpassentPawnOrRochadeRock: Figure?,
                   val colorOfMove: Boolean,
                   val isRochade: Boolean,
                   val isEnpassent: Boolean,
                   val isPawnTransformation: Boolean) {

    @get:JvmName("wasFigureTaken")
    val wasFigureTaken = figureTaken != null || isEnpassent

    override fun toString() = move.toString()
}

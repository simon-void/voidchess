package voidchess.helper

import voidchess.figures.Figure

/**
 * @author stephan
 */
class ExtendedMove(val move: Move,
                   val figure: Figure?,
                   val enpassentPawnOrRochadeRock: Figure?,
                   val colorOfMove: Boolean,
                   val isRochade: Boolean,
                   val isEnpassent: Boolean,
                   val isPawnTransformation: Boolean) {

    @get:JvmName("hasHitFigure")
    val hasHitFigure = figure != null || isEnpassent

    override fun toString() = move.toString()
}

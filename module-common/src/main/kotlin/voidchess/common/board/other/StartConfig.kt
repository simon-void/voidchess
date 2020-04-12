package voidchess.common.board.other

import voidchess.common.board.move.Position
import voidchess.common.figures.getFigureByString

sealed class StartConfig(
    val doesWhitePlayerStart: Boolean = true,
    val numberOfMovesWithoutHit: Int = 0,
    val figureCount: Int = 32,
    val pawnDoubleJumpToPosInPreviousMove: Position? = null
) {
    val hasHitFigureInPreviousMove: Boolean get() = numberOfMovesWithoutHit == 0 && figureCount!=32

    object ClassicConfig : StartConfig() {
        const val chess960Index = 518
        override fun toString() = "ClassicConfig"
    }

    class Chess960Config(val chess960Index: Int): StartConfig() {
        init {
            require(chess960Index in 0..959)
        }
        override fun toString() = "Chess960Config($chess960Index)"
    }

    class ManualConfig(
        doesWhitePlayerStart: Boolean,
        numberOfMovesWithoutHit: Int,
        val figureStates: List<String>
    ): StartConfig(
        doesWhitePlayerStart = doesWhitePlayerStart,
        numberOfMovesWithoutHit = numberOfMovesWithoutHit,
        figureCount = figureStates.size,
        pawnDoubleJumpToPosInPreviousMove = figureStates.filter { it.startsWith("Pawn") && it.endsWith("true") }
            .let { doubleJumpingPawnStates ->
                require(doubleJumpingPawnStates.size < 2) {
                    "at most one pawn can have double jumped in the previous move, not ${doubleJumpingPawnStates.size}, states: " +
                    doubleJumpingPawnStates.joinToString()
                }
                doubleJumpingPawnStates.firstOrNull()?.let {
                    val pawn = getFigureByString(it)
                    require(pawn.isWhite!=doesWhitePlayerStart)
                    pawn
                }
            }?.position
    ) {
        override fun toString() = "ManualConfig(whiteStarts: $doesWhitePlayerStart, noHitCount: $numberOfMovesWithoutHit, figures: ${figureStates.joinToString()})"
    }
}
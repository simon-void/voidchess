package voidchess.common.board.other

import voidchess.common.board.move.Position
import voidchess.common.figures.getFigureByString
import kotlin.math.floor

@JvmInline
value class Chess960Index(val value: Int) {
    init {
        require(value in 0..959) { "chess960 index must lie in range 0<=i<960 but was: $value" }
    }

    val isClassic: Boolean get() = value == CLASSIC_INDEX
    val isNotClassic: Boolean get() = value != CLASSIC_INDEX

    fun toStartConfig() = StartConfig.Chess960Config(this)

    companion object {
        private const val CLASSIC_INDEX = 518

        val classic: Chess960Index = Chess960Index(CLASSIC_INDEX)
        val min: Chess960Index = Chess960Index(0)
        val max: Chess960Index = Chess960Index(959)
        fun random(): Chess960Index = Chess960Index(floor(Math.random() * 960).toInt())
    }
}

sealed class StartConfig(
    val doesWhitePlayerStart: Boolean = true,
    val numberOfMovesWithoutHit: Int = 0,
    val figureCount: Int = 32,
    val pawnDoubleJumpToPosInPreviousMove: Position? = null
) {
    val hasHitFigureInPreviousMove: Boolean get() = numberOfMovesWithoutHit == 0 && figureCount!=32

    open class Chess960Config(val chess960Index: Chess960Index): StartConfig() {
        override fun toString() = "Chess960Config($chess960Index)"
    }

    data object ClassicConfig : Chess960Config(Chess960Index.classic)

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
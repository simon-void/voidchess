package voidchess.common.board

sealed class StartConfig(
    val doesWhitePlayerStart: Boolean,
    val numberOfMovesWithoutHit: Int,
    val figureCount: Int
) {
    object ClassicConfig: StartConfig(true, 0, 32) {
        const val chess960Index = 518
        override fun toString() = "ClassicConfig"
    }

    class Chess960Config(val chess960Index: Int): StartConfig(true, 0, 32) {
        override fun toString() = "Chess960Config(chess960Index: $chess960Index)"
    }

    class ManualConfig(
        doesWhitePlayerStart: Boolean,
        numberOfMovesWithoutHit: Int,
        val figureStates: List<String>
    ): StartConfig(doesWhitePlayerStart, numberOfMovesWithoutHit, figureStates.size) {
        override fun toString() = "ManualConfig(whiteStarts: $doesWhitePlayerStart, noHitCount: $numberOfMovesWithoutHit, figures: ${figureStates.joinToString()})"
    }
}
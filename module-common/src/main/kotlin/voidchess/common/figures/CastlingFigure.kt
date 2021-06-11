package voidchess.common.figures

sealed class CastlingFigure(
    isWhite: Boolean,
    type: FigureType,
    private var stepsTaken: Int = 0
) : Figure(
        isWhite,
        type,
) {

    override fun figureMoved() {
        stepsTaken++
    }

    override fun undoMove() {
        stepsTaken--
    }

    override fun canCastle() = stepsTaken == 0

    override fun toStringBuilder() = super.toStringBuilder().apply{
        append('-')
        append(stepsTaken)
    }
}

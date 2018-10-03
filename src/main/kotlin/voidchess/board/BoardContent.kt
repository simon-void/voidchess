package voidchess.board

import voidchess.figures.Figure

sealed class BoardContent {
    abstract val isFreeArea: Boolean
    abstract val figure: Figure

    companion object {
        @JvmStatic
        fun get(optionalFigure: Figure?): BoardContent {
            return if(optionalFigure==null) {
                NoFigure
            } else {
                OneFigure(optionalFigure)
            }
        }
    }

    private object NoFigure: BoardContent() {
        override val isFreeArea = true
        override val figure
            get() = throw NullPointerException()
    }

    private class OneFigure(override val figure: Figure): BoardContent() {
        override val isFreeArea = false
    }
}
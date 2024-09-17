package voidchess.common.integration

import voidchess.common.engine.Evaluation


interface ComputerPlayerUI {
    fun reset()
    fun showThoughts(show: Boolean)
    fun setProgress(computedMoves: Int, totalMoves: Int)
    fun setValue(value: Evaluation)
    fun setBubbleText(msg: String?)
    fun setThumb(thumb: Thumb)
}

enum class Thumb {
    UP, DOWN, NO
}
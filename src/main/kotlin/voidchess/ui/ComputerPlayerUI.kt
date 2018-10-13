package voidchess.ui

import voidchess.player.ki.evaluation.Evaluated


interface ComputerPlayerUI {
    fun reset()
    fun showThoughts(show: Boolean)
    fun setProgress(computedMoves: Int, totalMoves: Int)
    fun setValue(value: Evaluated)
    fun setBubbleText(msg: String?)
    fun setThumb(thumb: Thumb)
}

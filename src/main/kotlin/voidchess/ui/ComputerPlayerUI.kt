package voidchess.ui

import voidchess.player.ki.evaluation.Evaluation


interface ComputerPlayerUI {
    fun reset()
    fun showThoughts(show: Boolean)
    fun setProgress(computedMoves: Int, totalMoves: Int)
    fun setValue(value: Evaluation)
    fun setBubbleText(msg: String?)
    fun setThumb(thumb: Thumb)
}

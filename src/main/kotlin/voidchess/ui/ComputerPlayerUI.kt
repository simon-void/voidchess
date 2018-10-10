package voidchess.ui

import voidchess.helper.Move
import voidchess.player.ki.evaluation.Evaluated

import javax.swing.*


interface ComputerPlayerUI {
    fun reset()
    fun showThoughts(show: Boolean)
    fun setProgress(computedMoves: Int, totalMoves: Int)
    fun setValue(value: Evaluated)
    fun setBubbleText(msg: String?)
    fun setThumb(thumb: Thumb)
}

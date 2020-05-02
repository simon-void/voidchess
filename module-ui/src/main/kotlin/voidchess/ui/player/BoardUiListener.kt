package voidchess.ui.player

import voidchess.common.board.move.Position


internal interface BoardUiListener {
    fun mouseMovedOver(pos: Position?)
    fun mouseClickedOn(pos: Position)

    fun switchPlayerSelected()
    fun resignSelected()
    fun startSelected(chess960Index: Int)
}

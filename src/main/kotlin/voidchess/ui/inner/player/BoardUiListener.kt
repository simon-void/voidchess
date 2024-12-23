package voidchess.ui.inner.player

import voidchess.common.board.move.Position
import voidchess.common.board.other.Chess960Index

internal interface BoardUiListener {
    fun mouseMovedOver(pos: Position?)
    fun mouseClickedOn(pos: Position)

    fun switchPlayerSelected()
    fun resignSelected()
    fun startSelected(chess960Index: Chess960Index)
}
package voidchess.player

import voidchess.helper.Position


interface HumanPlayerInterface : PlayerInterface {
    fun mouseMovedOver(pos: Position?)
    fun mouseClickedOn(pos: Position)
}

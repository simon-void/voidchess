package voidchess.board

import voidchess.helper.ExtendedMove

interface LastMoveProvider {
    fun getLastMove(): ExtendedMove?
}

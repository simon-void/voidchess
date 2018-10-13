package voidchess.board.move

import voidchess.board.move.ExtendedMove

interface LastMoveProvider {
    fun getLastMove(): ExtendedMove?
}

package voidchess.common.integration

import voidchess.common.board.move.ExtendedMove

interface HumanPlayer {
    fun playFirstMove()
    fun playAfter(opponentsMove: ExtendedMove)
    fun gameEnds()
}
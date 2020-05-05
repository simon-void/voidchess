package voidchess.united.player

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.MoveResult


internal interface Player {
    fun gameEnds(endOption: MoveResult, lastMoveByWhite: Boolean)
    fun gaveCheck()
    fun makeAMove(opponentsMove: ExtendedMove)
    fun makeFirstMove()
}

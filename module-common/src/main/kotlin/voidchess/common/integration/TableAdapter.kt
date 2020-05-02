package voidchess.common.integration

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig

interface TableAdapter {
    fun startGame(startConfig: StartConfig, humanIsWhite: Boolean)
    fun moved(move: Move): ExtendedMove
    fun resign()
}
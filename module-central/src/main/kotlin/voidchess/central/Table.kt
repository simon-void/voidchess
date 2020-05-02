package voidchess.central

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.other.StartConfig


interface Table {
    fun startGame(startConfig: StartConfig, humanIsWhite: Boolean)
    fun stopGame(endOption: MoveResult)
    fun move(move: Move): ExtendedMove
}

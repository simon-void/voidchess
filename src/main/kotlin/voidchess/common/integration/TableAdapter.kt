package voidchess.common.integration

import voidchess.common.board.move.ComputerMoveResult
import voidchess.common.board.move.HumanMoveResult
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig

interface TableAdapter {
    fun computerStartsGame(startConfig: StartConfig): ColdPromise<ComputerMoveResult>
    fun humanStartsGame(startConfig: StartConfig)
    fun move(move: Move): HumanMoveResult
    fun resign()
}
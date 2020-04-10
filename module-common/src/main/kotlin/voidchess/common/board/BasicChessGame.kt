package voidchess.common.board

import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.StartConfig


interface BasicChessGame : StaticChessBoard {

    fun initGame(startConfig: StartConfig)
    fun isSelectable(pos: Position): Boolean
    fun isMovable(from: Position, to: Position): Boolean
    /**
     * @return true if the game is over
     */
    fun move(move: Move): Boolean
    fun getLatestExtendedMove(): ExtendedMove
    fun useSupervisor(supervisor: ChessGameSupervisor)
}

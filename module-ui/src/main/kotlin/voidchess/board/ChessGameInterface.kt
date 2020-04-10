package voidchess.board

import voidchess.common.board.BasicChessBoard
import voidchess.common.board.StartConfig
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor


interface ChessGameInterface : BasicChessBoard {

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    val startConfig: StartConfig
    val isWhiteTurn: Boolean
    val hasHitFigure: Boolean

    /**
     * @return all the moves played so far in a string representation
     */
    fun getCompleteHistory(): String
    fun initGame(chess960: Int)
    fun countFigures(): Int
    fun isCheck(isWhiteInCheck: Boolean): Boolean
    fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean
    fun isMovable(from: Position, to: Position, whitePlayer: Boolean): Boolean
    fun move(move: Move): MoveResult
    fun getLatestExtendedMove(): ExtendedMove
    fun suspendInteractiveSupervisor(): ChessGameSupervisor
    fun useSupervisor(supervisor: ChessGameSupervisor)
}

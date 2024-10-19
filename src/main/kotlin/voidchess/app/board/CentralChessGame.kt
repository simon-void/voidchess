package voidchess.app.board

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType
import voidchess.common.board.move.Position


internal interface CentralChessGame : StaticChessBoard {

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    val startConfig: StartConfig
    val isWhiteTurn: Boolean
    val hasHitFigure: Boolean
    val isCheck: Boolean

    /**
     * @return all the moves played so far in a string representation
     */
    fun getCompleteHistory(): String
    fun initGame(newConfig: StartConfig)
    fun isSelectable(pos: Position): Boolean
    fun isMovable(from: Position, to: Position): Boolean
    fun move(move: Move): MoveResultType
    fun getLatestExtendedMove(): ExtendedMove
}

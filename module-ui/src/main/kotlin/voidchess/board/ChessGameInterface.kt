package voidchess.board

import voidchess.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.StartConfig
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position


interface ChessGameInterface : BasicChessBoard {

    val startConfig: StartConfig
    val isWhiteTurn: Boolean
    val hasHitFigure: Boolean

    /**
     * @return all the moves played so far in a string representation
     */
    fun getCompleteHistory(): String
    fun getLastExtendedMove(): ExtendedMove
    fun initGame(chess960: Int)
    fun countFigures(): Int
    fun isCheck(isWhiteInCheck: Boolean): Boolean
    fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean
    fun isMovable(from: Position, to: Position, whitePlayer: Boolean): Boolean
    fun move(move: Move): MoveResult
    fun suspendInteractiveSupervisor(): ChessGameSupervisor
    fun useSupervisor(supervisor: ChessGameSupervisor)
    fun undo()
}

fun <T> ChessGameInterface.withMove(move: Move, workWithGameAfterMove: (ChessGameInterface)->T): T {
    move(move)
    return workWithGameAfterMove(this).apply {
        undo()
    }
}
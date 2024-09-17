package voidchess.engine.board

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType


internal interface EngineChessGame : StaticChessBoard {

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    val startConfig: StartConfig
    val isWhiteTurn: Boolean
    val latestExtendedMove: ExtendedMove
    val isCheck: Boolean
    /**
     * @return all the moves played so far in a string representation
     */
    val completeHistory: String
    val shortTermHistory: String

    fun <T> withMove(move: Move, workWithGameAfterMove: (MoveResultType)->T): T
    fun getAllMoves(): ArrayList<Move>
    fun countAllMoves(): MoveCounter
    fun countReachableMoves(): MoveCounter

    fun copyGame(numberOfInstances: Int): List<EngineChessGame>
}

data class MoveCounter(
    val movesByWhite: Int,
    val movesByBlack: Int
)
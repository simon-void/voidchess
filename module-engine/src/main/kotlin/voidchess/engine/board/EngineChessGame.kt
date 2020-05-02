package voidchess.engine.board

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult


internal interface EngineChessGame : StaticChessBoard {

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
    val completeHistory: String
    val shortTermHistory: String

    fun <T> withMove(move: Move, workWithGameAfterMove: (MoveResult)->T): T
    fun getAllMoves(): ArrayList<Move>
    fun getCriticalMoves(): ArrayList<Move>
    fun getTakingMoves(): ArrayList<Move>
    fun countReachableMoves(): MoveCounter
    fun countAllMoves(): MoveCounter

    fun copyGame(numberOfInstances: Int): List<EngineChessGame>
}

data class MoveCounter(
    val movesByWhite: Int,
    val movesByBlack: Int
)
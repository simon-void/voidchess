package voidchess.engine.board

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position


internal interface ChessGameInterface : StaticChessBoard {

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
    fun initGame(chess960: Int)
    fun copyGame(neededInstances: Int): List<ChessGameInterface>
    fun isMovable(from: Position, to: Position): Boolean
    fun move(move: Move): MoveResult
    fun getAllMoves(): Collection<Move>
    fun getCriticalMoves(): Collection<Move>
    fun getTakingMoves(): Collection<Move>
    /**
     * @return a pair of ints with the first int denoting the reachable sum of moves of all white figures
     * while the second int is the same for all black figures.
     */
    fun countReachableMoves(): Pair<Int, Int>
    fun undo()
}

internal inline fun <T> ChessGameInterface.withMove(move: Move, workWithGameAfterMove: (ChessGameInterface)->T): T {
    move(move)
    return workWithGameAfterMove(this).apply {
        undo()
    }
}
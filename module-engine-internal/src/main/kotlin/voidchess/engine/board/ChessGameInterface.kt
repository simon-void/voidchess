package voidchess.engine.board

import voidchess.common.board.BasicChessBoard
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor


internal interface ChessGameInterface : BasicChessBoard {

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
    fun countFigures(): Int
    fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean
    fun isMovable(from: Position, to: Position, whitePlayer: Boolean): Boolean
    fun move(move: Move): MoveResult
    fun getAllMoves(): Collection<Move>
    fun getCriticalMoves(): Collection<Move>
    fun getTakingMoves(): Collection<Move>
    /**
     * @return a pair of ints with the first int denoting the reachable sum of moves of all white figures
     * while the second int is the same for all black figures.
     */
    fun countReachableMoves(): Pair<Int, Int>
    fun suspendInteractiveSupervisor(): ChessGameSupervisor
    fun useSupervisor(supervisor: ChessGameSupervisor)
    fun undo()
}

internal fun <T> ChessGameInterface.withMove(move: Move, workWithGameAfterMove: (ChessGameInterface)->T): T {
    move(move)
    return workWithGameAfterMove(this).apply {
        undo()
    }
}
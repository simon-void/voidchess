package voidchess.engine.board

import voidchess.common.board.StartConfig
import voidchess.engine.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.engine.board.move.MoveResult
import voidchess.common.board.move.Position


internal interface ChessGameInterface : BasicChessBoard {

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    val isStandardGame: Boolean get() = startConfig.isClassicStartConfig
    val startConfig: StartConfig
    val isWhiteTurn: Boolean

    /**
     * @return all the moves played so far in a string representation
     */
    fun getCompleteHistory(): String
    fun getLastExtendedMove(): ExtendedMove
    fun initGame(chess960: Int)
    fun copyGame(neededInstances: Int): List<ChessGameInterface>
    fun countFigures(): Int
    fun hasHitFigure(): Boolean
    fun isCheck(isWhiteInCheck: Boolean): Boolean
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
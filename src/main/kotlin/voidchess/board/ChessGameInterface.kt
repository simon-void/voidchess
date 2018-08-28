package voidchess.board

import voidchess.helper.ChessGameSupervisor
import voidchess.helper.Move
import voidchess.helper.Position

/**
 * @author stephan
 */
interface ChessGameInterface : BasicChessGameInterface {

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    fun isStandardGame(): Boolean
    fun isWhiteTurn(): Boolean

    /**
     * @return all the moves played so far in a string representation
     */
    fun getCompleteHistory(): String
    fun initGame(chess960: Int)
    fun copyGame(neededInstances: Int): List<ChessGameInterface>
    fun countFigures(): Int
    fun hasHitFigure(): Boolean
    fun isCheck(isWhite: Boolean): Boolean
    fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean
    fun isMoveable(from: Position, to: Position, whitePlayer: Boolean): Boolean
    fun move(move: Move): MoveResult
    fun getPossibleMoves(possibleMoves: List<Move>)
    fun countReachableMoves(isWhite: Boolean): Int
    fun suspendInteractiveSupervisor(): ChessGameSupervisor
    fun useSupervisor(supervisor: ChessGameSupervisor)
    fun undo()
}
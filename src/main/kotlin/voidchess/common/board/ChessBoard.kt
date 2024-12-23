package voidchess.common.board

import voidchess.common.board.move.Move
import voidchess.common.inner.board.check.AttackLines
import voidchess.common.board.move.ExtendedMove
import voidchess.common.figures.Figure
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig


interface ChessBoard : StaticChessBoard {

    val isWhiteTurn: Boolean

    fun init(startConfig: StartConfig)

    /**@return true if a figure got hit */
    fun move(move: Move): ExtendedMove
    /**@return true if a figure got hit */
    fun undo(): Boolean
    /** "normal" moves only, no special cases (castling, enpassant, pawn promotion)
     *  the changed board is only accessible as query parameter */
    fun simulateSimplifiedMove(figure: Figure, warpTo: Position, query: (boardAfterMove: StaticChessBoard) -> Boolean): Boolean

    fun getCachedAttackLines(): AttackLines

    fun movesPlayed(): List<Move>
    fun historyToString(numberOfHalfMoves: Int?=null): String
}

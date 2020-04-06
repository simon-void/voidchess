package voidchess.engine.board

import voidchess.common.board.move.Move
import voidchess.engine.board.check.AttackLines
import voidchess.engine.figures.Figure
import voidchess.common.board.move.Position


internal interface ChessBoard : BasicChessBoard {

    fun init()
    fun init(chess960: Int)
    fun init(des: String)

    /**@return true if a figure got hit */
    fun move(move: Move, supervisor: ChessGameSupervisor): Boolean
    /**@return true if a figure got hit */
    fun undo(): Boolean
    /** "normal" moves only, no special cases (castling, enpassant, pawn promotion)
     *  the changed board is only accessible as query parameter */
    fun simulateSimplifiedMove(figure: Figure, warpTo: Position, query: (boardAfterMove: BasicChessBoard) -> Boolean): Boolean

    fun isCheck(isWhite: Boolean): Boolean
    fun getCachedAttackLines(isWhite: Boolean): AttackLines

    fun movesPlayed(): List<Move>
    fun historyToString(numberOfHalfMoves: Int?=null): String
}

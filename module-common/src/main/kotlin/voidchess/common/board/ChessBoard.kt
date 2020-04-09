package voidchess.common.board

import voidchess.common.board.move.Move
import voidchess.common.board.check.AttackLines
import voidchess.common.figures.Figure
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor


interface ChessBoard : BasicChessBoard {

    val isWhiteTurn: Boolean

    fun init(startConfig: StartConfig)

    /**@return true if a figure got hit */
    fun move(move: Move, supervisor: ChessGameSupervisor): Boolean
    /**@return true if a figure got hit */
    fun undo(): Boolean
    /** "normal" moves only, no special cases (castling, enpassant, pawn promotion)
     *  the changed board is only accessible as query parameter */
    fun simulateSimplifiedMove(figure: Figure, warpTo: Position, query: (boardAfterMove: BasicChessBoard) -> Boolean): Boolean

    fun getAttackLines(isWhite: Boolean): AttackLines

    fun movesPlayed(): List<Move>
    fun historyToString(numberOfHalfMoves: Int?=null): String
}

package voidchess.engine.board

import voidchess.common.board.StartConfig
import voidchess.common.board.move.Move
import voidchess.engine.board.check.AttackLines
import voidchess.engine.figures.Figure
import voidchess.common.board.move.Position
import voidchess.engine.board.other.ChessGameSupervisor


internal interface ChessBoard : BasicChessBoard {

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

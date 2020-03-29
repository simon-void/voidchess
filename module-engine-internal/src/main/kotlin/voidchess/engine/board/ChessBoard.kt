package voidchess.engine.board

import voidchess.engine.board.check.AttackLines
import voidchess.engine.figures.Figure
import voidchess.common.board.move.Position


internal interface ChessBoard : BasicChessBoard {

    fun init()
    fun init(chess960: Int)
    fun init(des: String)
    fun setFigure(pos: Position, figure: Figure)
    fun clearFigure(pos: Position)
    // moves figure, returns the figure that was taken
    // "normal" moves only, no special cases (castling, enpassant, pawn promotion)
    fun move(figure: Figure, to: Position): Figure?
    fun undoMove(figure: Figure, from: Position, figureTaken: Figure?)
    fun isCheck(isWhite: Boolean): Boolean
    fun getCachedAttackLines(isWhite: Boolean): AttackLines
}

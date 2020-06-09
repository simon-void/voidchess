package voidchess.common.board.move

import voidchess.common.figures.Figure
import voidchess.common.figures.FigureType
import voidchess.common.figures.Pawn


sealed class ExtendedMove(
    val move: Move,
    val hasHitFigure: Boolean
) {
    class Normal(move: Move, val figureTaken: Figure?, val movingFigureType: FigureType): ExtendedMove(move, figureTaken!=null)
    class PawnDoubleJump(move: Move, val pawn: Pawn): ExtendedMove(move, false)
    class Castling(kingPosToRookPos: Move, val kingMove: Move, val rookMove: Move): ExtendedMove(kingPosToRookPos, false)
    class Enpassant(pawnMove: Move, val pawnTaken: Figure): ExtendedMove(pawnMove, true)
    class Promotion(pawnPromotionMove: Move, val pawnPromoted: Figure, val figureTaken: Figure?): ExtendedMove(pawnPromotionMove, figureTaken!=null)
}

fun ExtendedMove.isPawnMove(): Boolean = when(this) {
    is ExtendedMove.Normal -> this.movingFigureType==FigureType.PAWN
    is ExtendedMove.Castling -> false
    else -> true
}
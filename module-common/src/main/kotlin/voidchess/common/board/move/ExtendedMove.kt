package voidchess.common.board.move

import voidchess.common.figures.Figure
import voidchess.common.figures.Pawn


sealed class ExtendedMove(
    val move: Move,
    val hasHitFigure: Boolean
) {
    class Normal(move: Move, val figureTaken: Figure?): ExtendedMove(move, figureTaken!=null)
    class PawnDoubleJump(move: Move, val pawn: Pawn): ExtendedMove(move, false)
    class Castling(kingPosToRookPos: Move, val kingMove: Move, val rookMove: Move): ExtendedMove(kingPosToRookPos, false)
    class Enpassant(pawnMove: Move, val pawnTaken: Figure): ExtendedMove(pawnMove, true)
    class Promotion(pawnPromotionMove: Move, val pawnPromoted: Figure, val figureTaken: Figure?): ExtendedMove(pawnPromotionMove, figureTaken!=null)
}

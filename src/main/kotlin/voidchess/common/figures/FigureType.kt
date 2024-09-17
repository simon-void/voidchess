package voidchess.common.figures


enum class FigureType(
    @JvmField
    val index: Int,
    val label: String,
) {
    PAWN(1, "Pawn"),
    KNIGHT(2, "Knight"),
    BISHOP(3, "Bishop"),
    ROOK(4, "Rook"),
    QUEEN(5, "Queen"),
    KING(6, "King");
}
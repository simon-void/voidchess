package voidchess.figures


enum class FigureType constructor(
        val index: Int,
        val label: String
) {
    PAWN(1, "Pawn"),
    ROOK(2, "Rook"),
    BISHOP(3, "Bishop"),
    KNIGHT(4, "Knight"),
    QUEEN(5, "Queen"),
    KING(6, "King");

}
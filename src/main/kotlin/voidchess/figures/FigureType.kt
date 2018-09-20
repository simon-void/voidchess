package voidchess.figures

import voidchess.image.ImageType

/**
 * @author stephan
 */
enum class FigureType constructor(
        val index: Int,
        val label: String,
        private val whiteImageType: ImageType,
        private val blackImageType: ImageType
) {
    PAWN(1, "Pawn", ImageType.W_PAWN, ImageType.B_PAWN),
    ROOK(2, "Rook", ImageType.W_ROOK, ImageType.B_ROOK),
    BISHOP(3, "Bishop", ImageType.W_BISHOP, ImageType.B_BISHOP),
    KNIGHT(4, "Knight", ImageType.W_KNIGHT, ImageType.B_KNIGHT),
    QUEEN(5, "Queen", ImageType.W_QUEEN, ImageType.B_QUEEN),
    KING(6, "King", ImageType.W_KING, ImageType.B_KING);

    fun getImageType(isWhite: Boolean) = if (isWhite) {
        whiteImageType
    } else {
        blackImageType
    }
}
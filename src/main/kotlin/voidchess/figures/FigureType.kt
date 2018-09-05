package voidchess.figures

import voidchess.image.ImageType

/**
 * @author stephan
 */
enum class FigureType private constructor(
        val index: Int,
        val label: String,
        val whiteImageType: ImageType?,
        val blackImageType: ImageType?
) {
    PAWN(1, "Pawn", ImageType.W_PAWN, ImageType.B_PAWN),
    ROCK(2, "Rock", ImageType.W_ROCK, ImageType.B_ROCK),
    BISHOP(3, "Bishop", ImageType.W_BISHOP, ImageType.B_BISHOP),
    KNIGHT(4, "Knight", ImageType.W_KNIGHT, ImageType.B_KNIGHT),
    QUEEN(5, "Queen", ImageType.W_QUEEN, ImageType.B_QUEEN),
    KING(6, "King", ImageType.W_KING, ImageType.B_KING),
    MOCK(7, "MockFigure", null, null);

    fun getImageType(isWhite: Boolean) = if (isWhite) {
        whiteImageType!!
    } else {
        blackImageType!!
    }
}
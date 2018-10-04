package voidchess.figures

import voidchess.helper.Position

/**
 * @author stephan
 */
class FigureFactory {

    fun getKing(pos: Position, isWhite: Boolean) = King(isWhite, pos)
    fun getQueen(pos: Position, isWhite: Boolean) = Queen(isWhite, pos)
    fun getBishop(pos: Position, isWhite: Boolean) = Bishop(isWhite, pos)
    fun getKnight(pos: Position, isWhite: Boolean) = Knight(isWhite, pos)
    fun getRook(pos: Position, isWhite: Boolean)= Rook(isWhite, pos)
    fun getPawn(pos: Position, isWhite: Boolean) = Pawn(isWhite, pos)
    private fun getKing(pos: Position, isWhite: Boolean, stepsTaken: Int, didCastling: Boolean) = King(isWhite, pos, stepsTaken, didCastling)
    private fun getRook(pos: Position, isWhite: Boolean, stepsTaken: Int) = Rook(isWhite, pos, stepsTaken)
    private fun getPawn(pos: Position, isWhite: Boolean, canBeHitByEnpassent: Boolean) = Pawn(isWhite, pos, canBeHitByEnpassent)

    fun getFigureByString(description: String): Figure {
        val st = description.split('-').iterator()
        val type = st.next()
        val isWhite = st.next() == "white"
        val pos = Position.byCode(st.next())

        if (type == "Knight") return getKnight(pos, isWhite)
        if (type == "Bishop") return getBishop(pos, isWhite)
        if (type == "Queen") return getQueen(pos, isWhite)

        if (type == "Pawn") {
            val readyForCastlingOrEnpassent = st.next().toBoolean()
            return getPawn(pos, isWhite, readyForCastlingOrEnpassent)
        }

        val stepsTaken = st.next().toInt()
        if (type == "Rook") return getRook(pos, isWhite, stepsTaken)

        val didCastling = st.hasNext() && st.next().toBoolean()
        if (type == "King") return getKing(pos, isWhite, stepsTaken, didCastling)

        throw IllegalArgumentException("figure description misformated: $description")
    }
}

package voidchess.figures

import voidchess.helper.Position

import java.util.StringTokenizer

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
    private fun getKing(pos: Position, isWhite: Boolean, stepsTaken: Int, didRochade: Boolean) = King(isWhite, pos, stepsTaken, didRochade)
    private fun getRook(pos: Position, isWhite: Boolean, stepsTaken: Int) = Rook(isWhite, pos, stepsTaken)
    private fun getPawn(pos: Position, isWhite: Boolean, canBeHitByEnpasent: Boolean) = Pawn(isWhite, pos, canBeHitByEnpasent)

    fun getFigureByString(description: String): Figure {
        val st = StringTokenizer(description, "-", false)
        val type = st.nextToken()
        val isWhite = st.nextToken() == "white"
        val pos = Position.byCode(st.nextToken())

        if (type == "Knight") return getKnight(pos, isWhite)
        if (type == "Bishop") return getBishop(pos, isWhite)
        if (type == "Queen") return getQueen(pos, isWhite)

        if (type == "Pawn") {
            val readyForRochadeOrEnpasent = st.nextToken().toBoolean()
            return getPawn(pos, isWhite, readyForRochadeOrEnpasent)
        }

        val stepsTaken = st.nextToken().toInt()
        if (type == "Rook") return getRook(pos, isWhite, stepsTaken)

        val didRochade = st.hasMoreTokens() && st.nextToken().toBoolean()
        if (type == "King") return getKing(pos, isWhite, stepsTaken, didRochade)

        throw IllegalArgumentException("figure description misformated: $description")
    }
}

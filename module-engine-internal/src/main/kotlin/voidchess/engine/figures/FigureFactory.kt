package voidchess.engine.figures

import voidchess.common.board.move.Position


internal fun getKing(pos: Position, isWhite: Boolean) = King(isWhite, pos)
internal fun getQueen(pos: Position, isWhite: Boolean) = Queen(isWhite, pos)
internal fun getBishop(pos: Position, isWhite: Boolean) = Bishop(isWhite, pos)
internal fun getKnight(pos: Position, isWhite: Boolean) = Knight(isWhite, pos)
internal fun getRook(pos: Position, isWhite: Boolean) = Rook(isWhite, pos)
internal fun getPawn(pos: Position, isWhite: Boolean) = Pawn(isWhite, pos)

internal fun getFigureByString(description: String): Figure {
    fun getKing(pos: Position, isWhite: Boolean, stepsTaken: Int, didCastling: Boolean) =
        King(isWhite, pos, stepsTaken, didCastling)

    fun getRook(pos: Position, isWhite: Boolean, stepsTaken: Int) = Rook(isWhite, pos, stepsTaken)
    fun getPawn(pos: Position, isWhite: Boolean, canBeHitByEnpassent: Boolean) = Pawn(isWhite, pos, canBeHitByEnpassent)

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

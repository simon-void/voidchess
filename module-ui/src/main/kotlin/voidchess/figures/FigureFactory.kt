package voidchess.figures

import voidchess.common.board.move.Position


fun getQueen(pos: Position, isWhite: Boolean) = Queen(isWhite, pos)
fun getBishop(pos: Position, isWhite: Boolean) = Bishop(isWhite, pos)
fun getKnight(pos: Position, isWhite: Boolean) = Knight(isWhite, pos)
fun getRook(pos: Position, isWhite: Boolean) = Rook(isWhite, pos)

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
        return Pawn(isWhite, pos, readyForCastlingOrEnpassent)
    }

    val stepsTaken = st.next().toInt()
    if (type == "Rook") return Rook(isWhite, pos, stepsTaken)

    val didCastling = st.hasNext() && st.next().toBoolean()
    if (type == "King") return King(
        isWhite,
        pos,
        stepsTaken,
        didCastling
    )

    throw IllegalArgumentException("figure description misformated: $description")
}

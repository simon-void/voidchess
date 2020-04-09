package voidchess.common.figures

import voidchess.common.board.move.Position


fun getFigureByString(description: String): Figure {

    val st = description.split('-').iterator()
    val type = st.next()
    val isWhite = st.next() == "white"
    val pos = Position.byCode(st.next())

    when (type) {
        "Knight" -> return Knight(isWhite, pos)
        "Bishop" -> return Bishop(isWhite, pos)
        "Queen" -> return Queen(isWhite, pos)
        "Pawn" -> {
            val readyForCastlingOrEnpassant = st.next().toBoolean()
            return Pawn(isWhite, pos, readyForCastlingOrEnpassant)
        }
    }

    val stepsTaken = st.next().toInt()
    return when( type ) {
        "Rook" -> Rook(isWhite, pos, stepsTaken)
        "King" -> {
            val didCastling = st.hasNext() && st.next().toBoolean()
            King(isWhite, pos, stepsTaken, didCastling)
        }
        else -> throw IllegalArgumentException("figure description misformatted: $description")
    }
}

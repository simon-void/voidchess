package voidchess.common.board.move


class Move private constructor(
    val from: Position,
    val to: Position,
    val pawnPromotionType: PawnPromotion?
) {
    private val index = getMoveIndex(from.index, to.index)

    fun equalsMove(move: Move) = this === move //index == move.index
    override fun equals(other: Any?) = this === other //other is Move && index == other.index
    override fun hashCode() = index
    override fun toString(): String {
        val char = when(pawnPromotionType) {
            null -> '-'
            PawnPromotion.QUEEN -> 'Q'
            PawnPromotion.ROOK -> 'R'
            PawnPromotion.KNIGHT -> 'K'
            PawnPromotion.BISHOP -> 'B'
        }
        return "$from$char$to"
    }

    companion object {
        private val moves = Array(64 * 64) {
            // optimized from: reverse (toIndex * 64) + fromIndex
            // val fromIndex = it % 64
            // val toIndex = it / 64
            val fromIndex = it and 63
            val toIndex = it shr 6
            return@Array Move(
                Position.byIndex(fromIndex),
                Position.byIndex(toIndex),
                null
            )
        }

        operator fun get(from: Position, to: Position) = moves[getMoveIndex(
            from.index,
            to.index
        )]

        operator fun get(from: Position, to: Position, pawnPromotion: PawnPromotion) = Move(from, to, pawnPromotion)

        fun byCode(code: String): Move {
            val fromColumn = code[0].toInt() - 97
            val fromRow = code[1].toInt() - 49
            val toColumn = code[3].toInt() - 97
            val toRow = code[4].toInt() - 49
            val pawnPromotion: PawnPromotion = when(val char = code[2]) {
                '-' -> return get(Position[fromRow, fromColumn], Position[toRow, toColumn])
                'Q' -> PawnPromotion.QUEEN
                'K' -> PawnPromotion.KNIGHT
                'R' -> PawnPromotion.ROOK
                'B' -> PawnPromotion.BISHOP
                else -> throw IllegalArgumentException("unexpected separation character '$char'. Allowed values: -RKBQ")
            }

            return get(
                Position[fromRow, fromColumn],
                Position[toRow, toColumn],
                pawnPromotion
            )
        }

        fun byCheckedCode(code: String): Move {
            require(isValid(code)) {"moveCode $code isn't valid"}
            return byCode(code)
        }

        private val moveRegex = """^[a-h][1-8][-RKBQ][a-h][1-8]$""".toRegex()
        private val pawnPromotionMoveRegex = """^(a2[RKBQ]a1)|(b2[RKBQ]b1)|(c2[RKBQ]c1)|(d2[RKBQ]d1)|(e2[RKBQ]e1)|(f2[RKBQ]f1)|(g2[RKBQ]g1)|(h2[RKBQ]h1)|(a7[RKBQ]a8)|(b7[RKBQ]b8)|(c7[RKBQ]c8)|(d7[RKBQ]d8)|(e7[RKBQ]e8)|(f7[RKBQ]f8)|(g7[RKBQ]g8)|(h7[RKBQ]h8)$""".toRegex()
        private fun isValid(code: String): Boolean {
            if (code.length != 5 || !moveRegex.matches(code)) {
                return false
            }
            return code[2]=='-' || pawnPromotionMoveRegex matches code
        }

        // optimized from: (toIndex * 64) + fromIndex
        private fun getMoveIndex(fromIndex: Int, toIndex: Int): Int =  (toIndex shl 6) or fromIndex
    }
}

package voidchess.board.move


class Move private constructor(@JvmField val from: Position, @JvmField val to: Position): Comparable<Move> {
    private val index = getMoveIndex(from.index, to.index)

    fun equalsMove(move: Move) = this === move //index == move.index
    override fun equals(other: Any?) = this === other //other is Move && index == other.index
    override fun toString() = "$from-$to"
    override fun hashCode() = index

    override fun compareTo(other: Move) = index.compareTo(other.index)

    companion object {
        @JvmStatic
        private val moves = Array(64 * 64) {
            // optimized from: reverse (toIndex * 64) + fromIndex
            // val fromIndex = it % 64
            // val toIndex = it / 64
            val fromIndex = it and 63
            val toIndex = it shr 6
            return@Array Move(
                Position.byIndex(fromIndex),
                Position.byIndex(toIndex)
            )
        }

        @JvmStatic
        operator fun get(from: Position, to: Position) = moves[getMoveIndex(
            from.index,
            to.index
        )]

        @JvmStatic
        fun byCode(code: String): Move {
            val fromColumn = code[0].toInt() - 97
            val fromRow = code[1].toInt() - 49
            val toColumn = code[3].toInt() - 97
            val toRow = code[4].toInt() - 49

            return get(
                Position[fromRow, fromColumn],
                Position[toRow, toColumn]
            )
        }

        @JvmStatic
        fun isValid(code: String?): Boolean {
            if (code == null || code.length != 5 || code[2] != '-') {
                return false
            }

            val fromColumn = code[0].toInt() - 97
            val fromRow = code[1].toInt() - 49
            val toColumn = code[3].toInt() - 97
            val toRow = code[4].toInt() - 49

            return fromColumn in 0..7 &&
                    fromRow in 0..7 &&
                    toColumn in 0..7 &&
                    toRow in 0..7
        }

        // optimized from: (toIndex * 64) + fromIndex
        @JvmStatic
        private fun getMoveIndex(fromIndex: Int, toIndex: Int) =  (toIndex shl 6) or fromIndex
    }
}

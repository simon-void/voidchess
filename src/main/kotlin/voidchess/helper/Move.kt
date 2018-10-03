package voidchess.helper

/**
 * @author stephan
 */
class Move private constructor(val from: Position, val to: Position) {
    private val index = getMoveIndex(from.index, to.index)

    fun equalsMove(move: Move) = this === move //index == move.index
    override fun equals(other: Any?) = this === other //other is Move && index == other.index
    override fun toString() = "$from-$to"
    override fun hashCode() = index

    companion object {
        private val moves = Array(64 * 64) {
            val fromIndex = it % 64
            val toIndex = it / 64
            return@Array Move(
                    Position.byIndex(fromIndex),
                    Position.byIndex(toIndex)
            )
        }

        @JvmStatic
        operator fun get(from: Position, to: Position) = moves[getMoveIndex(from.index, to.index)]

        @JvmStatic
        fun byCode(code: String): Move {
            val fromColumn = code[0].toInt() - 97
            val fromRow = code[1].toInt() - 49
            val toColumn = code[3].toInt() - 97
            val toRow = code[4].toInt() - 49

            return get(
                    Position.get(fromRow, fromColumn),
                    Position.get(toRow, toColumn)
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

        // optimized from: fromIndex + toIndex * 64
        private fun getMoveIndex(fromIndex: Int, toIndex: Int) = fromIndex + (toIndex shl 6)
    }
}

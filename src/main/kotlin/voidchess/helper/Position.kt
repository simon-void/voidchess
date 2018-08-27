package voidchess.helper

/**
 * @author stephan
 */
class Position private constructor(val row: Int, val column: Int) {
    val index = getIndex(row, column)

    fun equalsPosition(pos: Position) = index == pos.index
    override fun equals(other: Any?) = other is Position && index == other.index
    override fun toString() = "${(column + 97).toChar()}${row+1}"
    override fun hashCode() = index

    companion object {
        private val positions = Array(64) {
            val row = it % 8
            val column = it / 8
            return@Array Position(row,column)
        }

        fun get(code: String): Position {
            assert(code.length == 2) { "argument is supposed to be something like \"a8\"" }

            val column = code[0].toInt() - 97
            val row = code[1].toInt() - 49

            return get(row, column)
        }

        fun get(row: Int, column: Int): Position {
            assert(inBounds(row, column)) {
                return@assert "position arguments row $row and column $column are not in range 0..7"
            }
            return positions[getIndex(row, column)]
        }

        // optimized from: row + column * 8
        private fun getIndex(row: Int, column: Int) = row + (column shl 3)

        fun inBounds(row: Int, column: Int): Boolean = row in 0..7 && column in 0..7
    }
}
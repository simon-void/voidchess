package voidchess.common.board.move

import kotlin.math.abs
import kotlin.math.max


class Position private constructor(@JvmField val row: Int, @JvmField val column: Int) {
    @JvmField val index = getIndex(row, column)
    private val isWhiteField = (row+column)%2==1

    fun equalsPosition(pos: Position) = this === pos //index == pos.index
    fun notEqualsPosition(pos: Position) = this !== pos
    override fun equals(other: Any?) = this === other //other is Position && index == other.index
    override fun toString() = "${(column + 97).toChar()}${row + 1}"
    override fun hashCode() = index

    fun isStraightTo(pos: Position) = row == pos.row || column == pos.column
    fun isDiagonalTo(pos: Position) = abs(row - pos.row) == abs(column - pos.column)
    fun isStraightOrDiagonalTo(pos: Position) = isStraightTo(pos) || isDiagonalTo(pos)
    fun hasSameColor(pos: Position) = isWhiteField==pos.isWhiteField

    fun step(direction: Direction): Position? {

        when (direction) {
            Direction.UP -> if (row == 7) return null
            Direction.DOWN -> if (row == 0) return null
            Direction.LEFT -> if (column == 0) return null
            Direction.RIGHT -> if (column == 7) return null
            Direction.UP_RIGHT -> if (row == 7 || column == 7) return null
            Direction.UP_LEFT -> if (row == 7 || column == 0) return null
            Direction.DOWN_RIGHT -> if (row == 0 || column == 7) return null
            Direction.DOWN_LEFT -> if (row == 0 || column == 0) return null
        }

        return byIndex(index + direction.posIndexDiff)
    }

    /**
     * returns a direction if to lies on a straight line or diagonal to this, else null.
     */
    fun getDirectionTo(to: Position): Direction? {
        if (this === to) return null

        val rowDifference = to.row - row
        val columnDifference = to.column - column

        if (rowDifference == 0) {
            return if (columnDifference > 0) {
                Direction.RIGHT
            } else {
                Direction.LEFT
            }
        }

        if (columnDifference == 0) {
            return if (rowDifference > 0) {
                Direction.UP
            } else {
                Direction.DOWN
            }
        }

        // if fromPos and toPos aren't diagonal
        if (abs(rowDifference) != abs(columnDifference)) {
            return null
        }

        return if (rowDifference > 0) {
            if (columnDifference > 0) {
                Direction.UP_RIGHT
            } else {
                Direction.UP_LEFT
            }
        } else {
            if (columnDifference > 0) {
                Direction.DOWN_RIGHT
            } else {
                Direction.DOWN_LEFT
            }
        }
    }

    /**
     * the number of moves a king would need to move from this position to [other]
     */
    fun distanceTo(other: Position) = max(abs(row-other.row), abs(column-other.column))

    fun offset(rowOffset: Int, columnOffset: Int): Position? {
        val toRow = row + rowOffset
        if (toRow !in 0..7) return null
        val toColumn = column + columnOffset
        if (toColumn !in 0..7) return null

        return Position[toRow, toColumn]
    }

    inline fun forEachKnightPos(informOf: (Position) -> Unit) {
        offset(2, 1)?.let { pos: Position ->
            informOf(pos)
        }
        offset(2, -1)?.let { pos: Position ->
            informOf(pos)
        }
        offset(-2, 1)?.let { pos: Position ->
            informOf(pos)
        }
        offset(-2, -1)?.let { pos: Position ->
            informOf(pos)
        }
        offset(1, 2)?.let { pos: Position ->
            informOf(pos)
        }
        offset(1, -2)?.let { pos: Position ->
            informOf(pos)
        }
        offset(-1, 2)?.let { pos: Position ->
            informOf(pos)
        }
        offset(-1, -2)?.let { pos: Position ->
            informOf(pos)
        }
    }

    inline fun forEachPosInLine(direction: Direction, shouldBreak: (Position) -> Boolean) {
        var oldPos = this
        while (true) {
            val newPos = oldPos.step(direction)
            if (newPos == null || shouldBreak(newPos)) break
            oldPos = newPos
        }
    }

    companion object {
        @JvmStatic
        private val positions = Array(64) {
            // optimized from: reverse (row * 8) + column
            // val column = it % 8
            // val row = it / 8
            val column = it and 7
            val row = it shr 3
            return@Array Position(row, column)
        }

        @JvmStatic
        fun byCode(code: String): Position {
            assert(code.length == 2) { "argument is supposed to be something like \"a8\"" }

            val column = code[0].code - 97
            val row = code[1].code - 49

            return get(row, column)
        }

        @JvmStatic
        fun byIndex(index: Int) = positions[index]

        @JvmStatic
        operator fun get(row: Int, column: Int): Position {
            assert(inBounds(row, column)) {
                return@assert "position arguments row $row and column $column are not in range 0..7"
            }
            return positions[getIndex(
                row,
                column
            )]
        }

        // optimized from: (row * 8) + column
        @JvmStatic
        private fun getIndex(row: Int, column: Int) = (row shl 3) or column

        @JvmStatic
        fun inBounds(row: Int, column: Int): Boolean = row in 0..7 && column in 0..7
    }
}

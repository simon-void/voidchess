package voidchess.helper

/**
 * @author stephan
 */
class Position private constructor(val row: Int, val column: Int) {
    val index = getIndex(row, column)

    fun equalsPosition(pos: Position) = this === pos //index == pos.index
    fun notEqualsPosition(pos: Position) = this !== pos
    override fun equals(other: Any?) = this === other //other is Position && index == other.index
    override fun toString() = "${(column + 97).toChar()}${row + 1}"
    override fun hashCode() = index

    fun isStraightTo(pos: Position) = row == pos.row || column == pos.column
    fun isDiagonalTo(pos: Position) = Math.abs(row - pos.row) == Math.abs(column - pos.column)
    fun isStraightOrDiagonalTo(pos: Position) = isStraightTo(pos) || isDiagonalTo(pos)

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
            if (columnDifference > 0) {
                return Direction.RIGHT
            } else {
                return Direction.LEFT
            }
        }

        if (columnDifference == 0) {
            if (rowDifference > 0) {
                return Direction.UP
            } else {
                return Direction.DOWN
            }
        }

        // if fromPos and toPos aren't diagonal
        if (Math.abs(rowDifference) != Math.abs(columnDifference)) {
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

    inline fun forEachMiddlePosInLine(other: Position, informOf: (Position) -> Unit) {
        val directionTo = getDirectionTo(other)
        if(directionTo==null) throw IllegalArgumentException("there is no line between $this and $other")

        var oldPos = this
        while (true) {
            val middlePos = oldPos.step(directionTo)!!
            if (middlePos.notEqualsPosition(other)) {
                informOf(middlePos)
            } else {
                break
            }
            oldPos = middlePos
        }
    }

    companion object {
        private val positions = Array(64) {
            val column = it % 8
            val row = it / 8
            return@Array Position(row, column)
        }

        @JvmStatic
        fun byCode(code: String): Position {
            assert(code.length == 2) { "argument is supposed to be something like \"a8\"" }

            val column = code[0].toInt() - 97
            val row = code[1].toInt() - 49

            return get(row, column)
        }

        @JvmStatic
        fun byIndex(index: Int) = positions[index]

        @JvmStatic
        operator fun get(row: Int, column: Int): Position {
            assert(inBounds(row, column)) {
                return@assert "position arguments row $row and column $column are not in range 0..7"
            }
            return positions[getIndex(row, column)]
        }

        // optimized from: column + row * 8
        private fun getIndex(row: Int, column: Int) = column + (row shl 3)

        @JvmStatic
        fun inBounds(row: Int, column: Int): Boolean = row in 0..7 && column in 0..7
    }
}

enum class Direction(val posIndexDiff: Int, val isDiagonal: Boolean) {
    UP_LEFT(7, true), UP(8, false), UP_RIGHT(9, true),
    LEFT(-1, false), /*  current                           */ RIGHT(1, false),
    DOWN_LEFT(-9, true), DOWN(-8, false), DOWN_RIGHT(-7, true);

    val isHorizontalOrVertical = !isDiagonal

    companion object {
        val straightDirs = listOf(UP, LEFT, RIGHT, DOWN)
        val diagonalDirs = listOf(UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT)
        fun getDiagonal(upOrDown: Direction, leftOrRight: Direction): Direction = when (upOrDown) {
            UP -> when (leftOrRight) {
                LEFT -> UP_LEFT
                RIGHT -> UP_RIGHT
                else -> throw IllegalArgumentException("leftOrRight parameter isn't LEFT or RIGHT but $leftOrRight")
            }
            DOWN -> when (leftOrRight) {
                LEFT -> DOWN_LEFT
                RIGHT -> DOWN_RIGHT
                else -> throw IllegalArgumentException("leftOrRight parameter isn't LEFT or RIGHT but $leftOrRight")
            }
            else -> throw IllegalArgumentException("upOrDown parameter isn't UP or DOWN but $upOrDown")
        }
    }
}
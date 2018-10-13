package voidchess.board.move

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
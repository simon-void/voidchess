package voidchess.common.board.move

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.move.Direction
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class DirectionTest {
    @Test
    fun testIsDiagonal() {

        assertEquals(4, Direction.diagonalDirs.size, "number of diagonal directions")
        Direction.diagonalDirs.forEach { diagonalDir ->
            assertTrue("should be diagonal but isn't: $diagonalDir") { diagonalDir.isDiagonal }
            assertFalse("shouldn't be straight but is: $diagonalDir") { diagonalDir.isStraight }
        }

        assertEquals(4, Direction.straightDirs.size, "number of straight directions")
        Direction.straightDirs.forEach { straightDir ->
            assertTrue("should be straight but isn't: $straightDir") { straightDir.isStraight }
            assertFalse("shouldn't be diagonal but is: $straightDir") { straightDir.isDiagonal }
        }
    }

    @Test(dataProvider = "getReverseDataProvider")
    fun testReverse(dir: Direction, reverseDir: Direction) {
        assertEquals(reverseDir, dir.reverse, "the reverse of $dir is $reverseDir not ${dir.reverse}")
        assertEquals(dir, reverseDir.reverse, "the reverse of $reverseDir is $dir not ${reverseDir.reverse}")
    }

    @DataProvider
    fun getReverseDataProvider() :Array<Array<Any>> = arrayOf(
            arrayOf<Any>(Direction.UP, Direction.DOWN),
            arrayOf<Any>(Direction.LEFT, Direction.RIGHT),
            arrayOf<Any>(Direction.UP_LEFT, Direction.DOWN_RIGHT),
            arrayOf<Any>(Direction.UP_RIGHT, Direction.DOWN_LEFT)
    )

    @Test
    fun testGetDiagonalSucceeds() {
        assertEquals(
            Direction.UP_LEFT, Direction.getDiagonal(
            Direction.UP, Direction.LEFT), "the diagonal of up and left")
        assertEquals(
            Direction.UP_RIGHT, Direction.getDiagonal(
            Direction.UP, Direction.RIGHT), "the diagonal of up and right")
        assertEquals(
            Direction.DOWN_RIGHT, Direction.getDiagonal(
            Direction.DOWN, Direction.RIGHT), "the diagonal of down and right")
        assertEquals(
            Direction.DOWN_LEFT, Direction.getDiagonal(
            Direction.DOWN, Direction.LEFT), "the diagonal of down and left")
    }

    @Test
    fun testGetDiagonalFails() {
        for(notUpOrDown in listOf(Direction.UP_LEFT, Direction.UP_RIGHT, Direction.RIGHT, Direction.DOWN_RIGHT, Direction.DOWN_LEFT, Direction.LEFT)) {
            for(anyDir in Direction.values()) {
                try {
                    Direction.getDiagonal(notUpOrDown, anyDir)
                    fail("illegal parameters for getDiagonal didn't throw exception! upOrDown: $notUpOrDown, leftOrRight: $anyDir")
                }catch (e: IllegalArgumentException) {}
            }
        }

        for(upOrDown in listOf(Direction.UP, Direction.DOWN)) {
            for(notLeftOrRight in listOf(Direction.UP_LEFT, Direction.UP, Direction.UP_RIGHT, Direction.DOWN_RIGHT, Direction.DOWN, Direction.DOWN_LEFT)) {
                try {
                    Direction.getDiagonal(upOrDown, notLeftOrRight)
                    fail("illegal parameters for getDiagonal didn't throw exception! upOrDown: $upOrDown, leftOrRight: $notLeftOrRight")
                }catch (e: IllegalArgumentException) {}
            }
        }
    }
}
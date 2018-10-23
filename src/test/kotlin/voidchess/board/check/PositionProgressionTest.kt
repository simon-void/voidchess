package voidchess.board.check

import org.testng.annotations.Test
import voidchess.board.move.Direction
import voidchess.board.move.Position

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider

class PositionProgressionTest {
    @Test(dataProvider = "getIteratorData")
    fun testIterator(startPos: String, size: Int, direction: Direction, expectedPositionCodes: List<String>) {
        val expectedPositions = expectedPositionCodes.map { code: String -> Position.byCode(code) }
        val posProgression = PositionProgression(Position.byCode(startPos), size, direction)
        assertEquals(posProgression.size, expectedPositions.size)
        assertEquals(posProgression.isEmpty, expectedPositions.isEmpty(), "isEmpty")

        val posIter = posProgression.iterator()
        var index = 0

        while (posIter.hasNext()) {
            val actualPos = posIter.next()
            assertEquals(actualPos, expectedPositions[index], "position index: $index")
            index++
        }
        assertEquals(index, expectedPositions.size)
    }

    @DataProvider
    fun getIteratorData() :Array<Array<Any>> = arrayOf(
            arrayOf("a1", 7, Direction.UP, listOf("a1", "a2", "a3", "a4", "a5", "a6", "a7")),
            arrayOf("h7", 1, Direction.DOWN_RIGHT, listOf("h7")),
            arrayOf("e4", 0, Direction.LEFT, listOf<Position>())
    )
}

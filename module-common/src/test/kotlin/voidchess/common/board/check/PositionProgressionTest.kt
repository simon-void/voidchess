package voidchess.common.board.check

import org.testng.Assert
import org.testng.annotations.Test
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import voidchess.common.board.move.PositionProgression
import voidchess.common.toList

internal class PositionProgressionTest {
    @Test(dataProvider = "getForEachReachablePosData")
    fun testForEachReachablePos(startPos: String, size: Int, direction: Direction, expectedPositionCodes: List<String>) {
        val expectedPositions = expectedPositionCodes.map { code: String -> Position.byCode(code) }
        val posProgression = PositionProgression(
            Position.byCode(startPos),
            size,
            direction
        )
        assertEquals(posProgression.size, expectedPositions.size)
        assertEquals(posProgression.isEmpty, expectedPositions.isEmpty(), "isEmpty")

        val posIter = posProgression.toList().iterator()
        var index = 0

        while (posIter.hasNext()) {
            val actualPos = posIter.next()
            assertEquals(actualPos, expectedPositions[index], "position index: $index")
            index++
        }
        assertEquals(index, expectedPositions.size)
    }

    @DataProvider
    fun getForEachReachablePosData() :Array<Array<Any>> = arrayOf(
            arrayOf("a1", 7, Direction.UP, listOf("a1", "a2", "a3", "a4", "a5", "a6", "a7")),
            arrayOf("h7", 1, Direction.DOWN_RIGHT, listOf("h7")),
            arrayOf("e4", 0, Direction.LEFT, listOf<Position>())
    )

    @Test(dataProvider = "getContainsData")
    fun testContainsData(positionProgression: PositionProgression, containsPosCodes: Set<String>, doesNotContainPosCodes: Set<String>) {
        val shouldNotContainButDoes = mutableListOf<String>()
        val shouldContainButDoesNot = mutableListOf<String>()
        for(containsPosCode in containsPosCodes) {
            val containsPosCodePos = Position.byCode(containsPosCode)
            if(!positionProgression.contains(containsPosCodePos)) {
                shouldContainButDoesNot.add(containsPosCode)
            }
        }
        for(doesNotContainPosCode in doesNotContainPosCodes) {
            val doesNotContainPos = Position.byCode(doesNotContainPosCode)
            if(positionProgression.contains(doesNotContainPos)) {
                shouldNotContainButDoes.add(doesNotContainPosCode)
            }
        }

        Assert.assertTrue(shouldNotContainButDoes.isEmpty() && shouldContainButDoesNot.isEmpty(),
                "$positionProgression should contain but doesn't $shouldContainButDoesNot and shouldn't contain but does $shouldNotContainButDoes.")
    }

    @DataProvider
    fun getContainsData() :Array<Array<Any>> = arrayOf(
            arrayOf(
                PositionProgression(
                    Position.byCode("e4"),
                    0,
                    Direction.UP
                ), setOf<String>(), setOf("d3", "d4", "d5", "e3", "e4", "e5", "f3", "f4", "f5")),
            arrayOf(
                PositionProgression(
                    Position.byCode("e4"),
                    1,
                    Direction.UP
                ), setOf("e4"), setOf("d3", "d4", "d5", "e3", "e5", "f3", "f4", "f5")),
            arrayOf(
                PositionProgression(
                    Position.byCode("e4"),
                    2,
                    Direction.UP
                ), setOf("e4", "e5"), setOf("d3", "d4", "d5", "e3", "e6", "f3", "f4", "f5")),
            arrayOf(
                PositionProgression(
                    Position.byCode("c3"),
                    3,
                    Direction.DOWN_LEFT
                ), setOf("c3", "a1"), setOf("d4")),
            arrayOf(
                PositionProgression(
                    Position.byCode("h1"),
                    3,
                    Direction.LEFT
                ), setOf("h1", "f1"), setOf("e1")),
            arrayOf(
                PositionProgression(
                    Position.byCode("e7"),
                    4,
                    Direction.RIGHT
                ), setOf("e7", "h7"), setOf("d7", "e6", "e8"))
    )
}

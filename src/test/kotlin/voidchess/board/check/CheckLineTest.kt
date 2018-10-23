package voidchess.board.check

import org.testng.annotations.Test
import voidchess.board.move.Direction
import voidchess.board.move.Position

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider

class CheckLineTest {
    @Test(dataProvider = "getTestActualCheckLineIteratorData")
    fun testActualCheckLineIterator(kingPosCode: String, attackerPosCode: String, expectedPositionCodes: List<String>) {
        val expectedPositions = expectedPositionCodes.map { code: String -> Position.byCode(code) }
        val kingPos = Position.byCode(kingPosCode)
        val attackerPos = Position.byCode(attackerPosCode)
        assertEquals(expectedPositionCodes.size, kingPos.distanceTo(attackerPos))

        val actualCheckLine = getActualCheckLine(kingPosCode, attackerPosCode)

        val posIter = actualCheckLine.iterator()
        var index = 0

        while (posIter.hasNext()) {
            val actualPos = posIter.next()
            assertEquals(actualPos, expectedPositions[index], "position index: $index")
            index++
        }
        assertEquals(index, expectedPositions.size)
    }

    @DataProvider
    fun getTestActualCheckLineIteratorData() :Array<Array<Any>> = arrayOf(
            arrayOf("f4", "b8", listOf("b8", "c7", "d6", "e5")),
            arrayOf("h7", "f5", listOf("f5", "g6")),
            arrayOf("h7", "h8", listOf("h8"))
    )

    @Test(dataProvider = "getTestDoesNotKeepKingInCheckIfHeMovesToData")
    fun testDoesNotKeepKingInCheckIfHeMovesTo(checkLine: CheckLine, inCheckDirections: List<Direction>) {
        for(direction in Direction.values()) {
            val actualNotInCheck = checkLine.doesNotKeepKingInCheckIfHeMovesTo(direction)
            assertEquals(actualNotInCheck, !inCheckDirections.contains(direction), "no check in direction $direction")
        }
    }

    @DataProvider
    fun getTestDoesNotKeepKingInCheckIfHeMovesToData() :Array<Array<Any>> = arrayOf(
            arrayOf<Any>(KnightOrPawnCheck(Position.byCode("g3")), listOf<Direction>()),
            arrayOf<Any>(getActualCheckLine("g3", "h3"), listOf(Direction.LEFT)),
            arrayOf<Any>(getActualCheckLine("g3", "h2"), listOf(Direction.UP_LEFT)),
            arrayOf<Any>(getActualCheckLine("g3", "g1"), listOf(Direction.DOWN, Direction.UP))
    )

    private fun getActualCheckLine(kingPosCode: String, attackerPosCode: String): ActualCheckLine {
        val kingPos = Position.byCode(kingPosCode)
        val attackerPos = Position.byCode(attackerPosCode)

        return ActualCheckLine(kingPos, attackerPos, kingPos.getDirectionTo(attackerPos)!!)
    }
}

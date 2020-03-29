package voidchess.engine.board.check

import org.testng.annotations.Test
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import voidchess.toList


internal class CheckLineTest {
    @Test(dataProvider = "getTestActualCheckLineIteratorData")
    fun testActualCheckLineIterator(kingPosCode: String, attackerPosCode: String, expectedPositionCodes: List<String>) {
        val expectedPositions = expectedPositionCodes.map { code: String -> Position.byCode(code) }
        val kingPos = Position.byCode(kingPosCode)
        val attackerPos = Position.byCode(attackerPosCode)
        assertEquals(expectedPositionCodes.size, kingPos.distanceTo(attackerPos))

        val actualCheckLine = getActualCheckLine(kingPosCode, attackerPosCode)

        val posIter = actualCheckLine.posProgression.toList().iterator()
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
    fun testKeepsKingInCheckIfHeMovesTo(checkLine: CheckLine, inCheckDirections: List<Direction>) {
        for(direction in Direction.values()) {
            val actualInCheck = checkLine.keepsKingInCheckIfHeMovesTo(direction)
            assertEquals(actualInCheck, inCheckDirections.contains(direction), "no check in direction $direction")
        }
    }

    @DataProvider
    fun getTestDoesNotKeepKingInCheckIfHeMovesToData() :Array<Array<Any>> = arrayOf(
            arrayOf(PawnCheck(Position.byCode("g3"), Position.byCode("f2")), listOf<Direction>()),
            arrayOf(KnightCheck(Position.byCode("g3"), Position.byCode("f1")), listOf<Direction>()),
            arrayOf(getActualCheckLine("g3", "h3"), listOf(Direction.LEFT)),
            arrayOf(getActualCheckLine("g3", "h2"), listOf(Direction.UP_LEFT)),
            arrayOf(getActualCheckLine("g3", "g1"), listOf(Direction.DOWN, Direction.UP))
    )

    private fun getActualCheckLine(kingPosCode: String, attackerPosCode: String): ActualCheckLine {
        val kingPos = Position.byCode(kingPosCode)
        val attackerPos = Position.byCode(attackerPosCode)

        return ActualCheckLine(kingPos, attackerPos, kingPos.getDirectionTo(attackerPos)!!)
    }
}

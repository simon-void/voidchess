package voidchess.common.board.move

import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test


class PositionTest {
    @DataProvider(name = "getInvalidGetRowColumnData")
    private fun invalidGetRowColumnData(): Array<Array<Any>> =
            arrayOf(
                    arrayOf<Any>(-1, 0),
                    arrayOf<Any>(0, -1),
                    arrayOf<Any>(8, 0),
                    arrayOf<Any>(0, 8)
            )

    // middle of board -> all steps exists
    // and now the corners -> where not all directions stay on the board
    @DataProvider(name = "getTestStepData")
    private fun testStepData(): Array<Array<Any?>> = arrayOf(
            getPositionDirectionOptionalPosition("e4", Direction.DOWN_LEFT, "d3"),
            getPositionDirectionOptionalPosition("e4", Direction.DOWN, "e3"),
            getPositionDirectionOptionalPosition("e4", Direction.DOWN_RIGHT, "f3"),
            getPositionDirectionOptionalPosition("e4", Direction.LEFT, "d4"),
            getPositionDirectionOptionalPosition("e4", Direction.RIGHT, "f4"),
            getPositionDirectionOptionalPosition("e4", Direction.UP_LEFT, "d5"),
            getPositionDirectionOptionalPosition("e4", Direction.UP, "e5"),
            getPositionDirectionOptionalPosition("e4", Direction.UP_RIGHT, "f5"),
            getPositionDirectionOptionalPosition("a1", Direction.DOWN_LEFT, null),
            getPositionDirectionOptionalPosition("a1", Direction.DOWN, null),
            getPositionDirectionOptionalPosition("a1", Direction.DOWN_RIGHT, null),
            getPositionDirectionOptionalPosition("a1", Direction.LEFT, null),
            getPositionDirectionOptionalPosition("a1", Direction.RIGHT, "b1"),
            getPositionDirectionOptionalPosition("a1", Direction.UP_LEFT, null),
            getPositionDirectionOptionalPosition("a1", Direction.UP, "a2"),
            getPositionDirectionOptionalPosition("a1", Direction.UP_RIGHT, "b2"),
            getPositionDirectionOptionalPosition("a8", Direction.DOWN_LEFT, null),
            getPositionDirectionOptionalPosition("a8", Direction.DOWN, "a7"),
            getPositionDirectionOptionalPosition("a8", Direction.DOWN_RIGHT, "b7"),
            getPositionDirectionOptionalPosition("a8", Direction.LEFT, null),
            getPositionDirectionOptionalPosition("a8", Direction.RIGHT, "b8"),
            getPositionDirectionOptionalPosition("a8", Direction.UP_LEFT, null),
            getPositionDirectionOptionalPosition("a8", Direction.UP, null),
            getPositionDirectionOptionalPosition("a8", Direction.UP_RIGHT, null),
            getPositionDirectionOptionalPosition("h1", Direction.DOWN_LEFT, null),
            getPositionDirectionOptionalPosition("h1", Direction.DOWN, null),
            getPositionDirectionOptionalPosition("h1", Direction.DOWN_RIGHT, null),
            getPositionDirectionOptionalPosition("h1", Direction.LEFT, "g1"),
            getPositionDirectionOptionalPosition("h1", Direction.RIGHT, null),
            getPositionDirectionOptionalPosition("h1", Direction.UP_LEFT, "g2"),
            getPositionDirectionOptionalPosition("h1", Direction.UP, "h2"),
            getPositionDirectionOptionalPosition("h1", Direction.UP_RIGHT, null),
            getPositionDirectionOptionalPosition("h8", Direction.DOWN_LEFT, "g7"),
            getPositionDirectionOptionalPosition("h8", Direction.DOWN, "h7"),
            getPositionDirectionOptionalPosition("h8", Direction.DOWN_RIGHT, null),
            getPositionDirectionOptionalPosition("h8", Direction.LEFT, "g8"),
            getPositionDirectionOptionalPosition("h8", Direction.RIGHT, null),
            getPositionDirectionOptionalPosition("h8", Direction.UP_LEFT, null),
            getPositionDirectionOptionalPosition("h8", Direction.UP, null),
            getPositionDirectionOptionalPosition("h8", Direction.UP_RIGHT, null)
    )

    @DataProvider(name = "getTestDistanceToData")
    private fun testDistanceToData(): Array<Array<Any>> =
            arrayOf(
                    arrayOf("a1", "h8", 7),
                    arrayOf("b3", "c8", 5),
                    arrayOf("b7", "h3", 6)
            )

    @Test
    fun testGet() {
        val pos1 = Position[0, 7]
        assertTrue(pos1.row == 0)
        assertTrue(pos1.column == 7)

        val pos2 = Position.byCode("a8")
        assertTrue(pos2.column == 0)
        assertTrue(pos2.row == 7)
    }

    @Test(dataProvider = "getInvalidGetRowColumnData")
    fun testInvalidGetByIntsFails(row: Int, column: Int) {
        try {
            Position[row, column]
            fail("test should have failed on row:$row, column:$column but didn't")
        } catch (e: AssertionError) {
            // expected to fail
        } catch (e: ArrayIndexOutOfBoundsException) {
        }

    }

    @Test
    fun testIndex() {
        val pos1 = Position.byCode("a1")
        assertEquals(pos1.index, 0)
        val pos2 = Position.byCode("h8")
        assertEquals(pos2.index, 63)
    }

    @Test
    fun testIndexConversion() {
        for (i in 0..63) {
            val posByIndex = Position.byIndex(i)
            val posByRowColumn = Position[posByIndex.row, posByIndex.column]
            assertEquals(posByRowColumn.index, i, "recomputed index")
        }
    }

    @Test
    fun testToString() {
        val pos1 = Position[0, 0]
        assertEquals(pos1.toString(), "a1")
        val pos2 = Position[7, 0]
        assertEquals(pos2.toString(), "a8")
        val pos3 = Position[7, 7]
        assertEquals(pos3.toString(), "h8")
    }

    @Test
    fun testEquals() {
        val pos1 = Position.byCode("d4")
        val pos2 = Position.byCode("e6")
        val pos3 = Position.byCode("d4")

        assertFalse(pos1.equalsPosition(pos2))
        assertFalse(pos2.equalsPosition(pos3))
        assertTrue(pos1.equalsPosition(pos3))
    }

    @Test
    fun testInBounds() {
        assertFalse(Position.inBounds(-1, 4))
        assertFalse(Position.inBounds(1, 8))
        assertFalse(Position.inBounds(8, 4))
        assertFalse(Position.inBounds(1, -1))

        assertTrue(Position.inBounds(0, 0))
        assertTrue(Position.inBounds(0, 7))
        assertTrue(Position.inBounds(7, 0))
        assertTrue(Position.inBounds(7, 7))
        assertTrue(Position.inBounds(3, 4))
    }

    @Test
    fun testIsDiagonalTo() {
        val pos1 = Position.byCode("a1")
        val pos2 = Position.byCode("a8")
        val pos3 = Position.byCode("h1")
        val pos4 = Position.byCode("h8")

        assertTrue(pos1.isDiagonalTo(pos4))
        assertTrue(pos4.isDiagonalTo(pos1))
        assertTrue(pos2.isDiagonalTo(pos3))
        assertTrue(pos3.isDiagonalTo(pos2))

        assertFalse(pos1.isDiagonalTo(pos2))
        assertFalse(pos1.isDiagonalTo(pos3))
        assertFalse(pos2.isDiagonalTo(pos1))
        assertFalse(pos2.isDiagonalTo(pos4))
        assertFalse(pos3.isDiagonalTo(pos1))
        assertFalse(pos3.isDiagonalTo(pos4))
        assertFalse(pos4.isDiagonalTo(pos2))
        assertFalse(pos4.isDiagonalTo(pos3))
    }

    @Test
    fun testIsStraightTo() {
        val pos1 = Position.byCode("a1")
        val pos2 = Position.byCode("a8")
        val pos3 = Position.byCode("h1")
        val pos4 = Position.byCode("h8")

        assertTrue(pos1.isStraightTo(pos2))
        assertTrue(pos1.isStraightTo(pos3))
        assertTrue(pos2.isStraightTo(pos1))
        assertTrue(pos2.isStraightTo(pos4))
        assertTrue(pos3.isStraightTo(pos1))
        assertTrue(pos3.isStraightTo(pos4))
        assertTrue(pos4.isStraightTo(pos2))
        assertTrue(pos4.isStraightTo(pos3))

        assertFalse(pos1.isStraightTo(pos4))
        assertFalse(pos2.isStraightTo(pos3))
        assertFalse(pos3.isStraightTo(pos2))
        assertFalse(pos4.isStraightTo(pos1))
    }

    @Test
    fun testIsStraightOrDiagonalTo() {
        val pos1 = Position.byCode("a1")
        val pos2 = Position.byCode("a8")
        val pos3 = Position.byCode("h1")
        val pos4 = Position.byCode("h8")

        val pos5 = Position.byCode("b3")

        assertTrue(pos1.isStraightOrDiagonalTo(pos2))
        assertTrue(pos1.isStraightOrDiagonalTo(pos3))
        assertTrue(pos1.isStraightOrDiagonalTo(pos4))
        assertTrue(pos2.isStraightOrDiagonalTo(pos1))
        assertTrue(pos2.isStraightOrDiagonalTo(pos3))
        assertTrue(pos2.isStraightOrDiagonalTo(pos4))
        assertTrue(pos3.isStraightOrDiagonalTo(pos1))
        assertTrue(pos3.isStraightOrDiagonalTo(pos2))
        assertTrue(pos3.isStraightOrDiagonalTo(pos4))
        assertTrue(pos4.isStraightOrDiagonalTo(pos1))
        assertTrue(pos4.isStraightOrDiagonalTo(pos2))
        assertTrue(pos4.isStraightOrDiagonalTo(pos3))

        assertFalse(pos1.isStraightOrDiagonalTo(pos5))
        assertFalse(pos2.isStraightOrDiagonalTo(pos5))
        assertFalse(pos3.isStraightOrDiagonalTo(pos5))
        assertFalse(pos4.isStraightOrDiagonalTo(pos5))
        assertFalse(pos5.isStraightOrDiagonalTo(pos1))
        assertFalse(pos5.isStraightOrDiagonalTo(pos2))
        assertFalse(pos5.isStraightOrDiagonalTo(pos3))
        assertFalse(pos5.isStraightOrDiagonalTo(pos4))
    }

    @Test
    fun testPositionByCode() {
        val a1 = Position.byCode("a1")
        assertEquals(a1.toString(), "a1")
        assertEquals(a1.column, 0, "column")
        assertEquals(a1.row, 0, "row")

        val b3 = Position.byCode("b3")
        assertEquals(b3.toString(), "b3")
        assertEquals(b3.column, 1, "column")
        assertEquals(b3.row, 2, "row")
    }

    @Test
    fun testPositionByIndex() {
        val b3 = Position.byCode("b3")
        val actualPos = Position.byIndex(b3.index)
        assertEquals(actualPos, b3)
    }

    @Test(dataProvider = "getTestStepData")
    fun testStep(pos: Position, direction: Direction, expectedPos: Position?) {
        val actualPos = pos.step(direction)
        assertEquals(actualPos, expectedPos)
    }

    private fun getPositionDirectionOptionalPosition(posCode: String, direction: Direction, expectedPosCode: String?): Array<Any?> {
        val pos = Position.byCode(posCode)
        val newPos = expectedPosCode?.let { Position.byCode(it)}
        return arrayOf(pos, direction, newPos)
    }

    @Test(dataProvider = "getTestDistanceToData")
    fun testDistanceTo(posCode1: String, posCode2: String, expectedDistance: Int) {
        val pos1 = Position.byCode(posCode1)
        val pos2 = Position.byCode(posCode2)

        assertEquals(pos1.distanceTo(pos2), expectedDistance, "distance")
        assertEquals(pos2.distanceTo(pos1), expectedDistance, "reverse distance")
    }
}

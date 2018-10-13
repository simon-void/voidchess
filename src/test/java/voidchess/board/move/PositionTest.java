package voidchess.board.move;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.move.Direction;
import voidchess.board.move.Position;

import static org.testng.Assert.*;


public class PositionTest {
    @Test
    public void testGet() {
        Position pos1 = Position.get(0, 7);
        assertTrue(pos1.row == 0);
        assertTrue(pos1.column == 7);

        Position pos2 = Position.byCode("a8");
        assertTrue(pos2.column == 0);
        assertTrue(pos2.row == 7);
    }

    @Test(dataProvider = "getInvalidGetRowColumnData")
    public void testInvalidGetByIntsFails(int row, int column) {
        try {
            Position.get(row, column);
            fail("test should have failed on row:" + row + ", column:" + column + " but didn't");
        } catch (AssertionError | ArrayIndexOutOfBoundsException e) {
            // expected to fail
        }
    }

    @DataProvider
    private Object[][] getInvalidGetRowColumnData() {
        return new Object[][]{
                new Object[]{-1, 0},
                new Object[]{0, -1},
                new Object[]{8, 0},
                new Object[]{0, 8},
        };
    }

    @Test
    public void testIndex() {
        Position pos1 = Position.byCode("a1");
        assertEquals(pos1.getIndex(), 0);
        Position pos2 = Position.byCode("h8");
        assertEquals(pos2.getIndex(), 63);
    }

    @Test
    public void testToString() {
        Position pos1 = Position.get(0, 0);
        assertEquals(pos1.toString(), "a1");
        Position pos2 = Position.get(7, 0);
        assertEquals(pos2.toString(), "a8");
        Position pos3 = Position.get(7, 7);
        assertEquals(pos3.toString(), "h8");
    }

    @Test
    public void testEquals() {
        Position pos1 = Position.byCode("d4");
        Position pos2 = Position.byCode("e6");
        Position pos3 = Position.byCode("d4");

        assertFalse(pos1.equalsPosition(pos2));
        assertFalse(pos2.equalsPosition(pos3));
        assertTrue(pos1.equalsPosition(pos3));
    }

    @Test
    public void testInBounds() {
        assertFalse(Position.inBounds(-1, 4));
        assertFalse(Position.inBounds(1, 8));
        assertFalse(Position.inBounds(8, 4));
        assertFalse(Position.inBounds(1, -1));

        assertTrue(Position.inBounds(0, 0));
        assertTrue(Position.inBounds(0, 7));
        assertTrue(Position.inBounds(7, 0));
        assertTrue(Position.inBounds(7, 7));
        assertTrue(Position.inBounds(3, 4));
    }

    @Test
    public void testIsDiagonalTo() {
        Position pos1 = Position.byCode("a1");
        Position pos2 = Position.byCode("a8");
        Position pos3 = Position.byCode("h1");
        Position pos4 = Position.byCode("h8");

        assertTrue(pos1.isDiagonalTo(pos4));
        assertTrue(pos4.isDiagonalTo(pos1));
        assertTrue(pos2.isDiagonalTo(pos3));
        assertTrue(pos3.isDiagonalTo(pos2));

        assertFalse(pos1.isDiagonalTo(pos2));
        assertFalse(pos1.isDiagonalTo(pos3));
        assertFalse(pos2.isDiagonalTo(pos1));
        assertFalse(pos2.isDiagonalTo(pos4));
        assertFalse(pos3.isDiagonalTo(pos1));
        assertFalse(pos3.isDiagonalTo(pos4));
        assertFalse(pos4.isDiagonalTo(pos2));
        assertFalse(pos4.isDiagonalTo(pos3));
    }

    @Test
    public void testIsStraightTo() {
        Position pos1 = Position.byCode("a1");
        Position pos2 = Position.byCode("a8");
        Position pos3 = Position.byCode("h1");
        Position pos4 = Position.byCode("h8");

        assertTrue(pos1.isStraightTo(pos2));
        assertTrue(pos1.isStraightTo(pos3));
        assertTrue(pos2.isStraightTo(pos1));
        assertTrue(pos2.isStraightTo(pos4));
        assertTrue(pos3.isStraightTo(pos1));
        assertTrue(pos3.isStraightTo(pos4));
        assertTrue(pos4.isStraightTo(pos2));
        assertTrue(pos4.isStraightTo(pos3));

        assertFalse(pos1.isStraightTo(pos4));
        assertFalse(pos2.isStraightTo(pos3));
        assertFalse(pos3.isStraightTo(pos2));
        assertFalse(pos4.isStraightTo(pos1));
    }

    @Test
    public void testIsStraightOrDiagonalTo() {
        Position pos1 = Position.byCode("a1");
        Position pos2 = Position.byCode("a8");
        Position pos3 = Position.byCode("h1");
        Position pos4 = Position.byCode("h8");

        Position pos5 = Position.byCode("b3");

        assertTrue(pos1.isStraightOrDiagonalTo(pos2));
        assertTrue(pos1.isStraightOrDiagonalTo(pos3));
        assertTrue(pos1.isStraightOrDiagonalTo(pos4));
        assertTrue(pos2.isStraightOrDiagonalTo(pos1));
        assertTrue(pos2.isStraightOrDiagonalTo(pos3));
        assertTrue(pos2.isStraightOrDiagonalTo(pos4));
        assertTrue(pos3.isStraightOrDiagonalTo(pos1));
        assertTrue(pos3.isStraightOrDiagonalTo(pos2));
        assertTrue(pos3.isStraightOrDiagonalTo(pos4));
        assertTrue(pos4.isStraightOrDiagonalTo(pos1));
        assertTrue(pos4.isStraightOrDiagonalTo(pos2));
        assertTrue(pos4.isStraightOrDiagonalTo(pos3));

        assertFalse(pos1.isStraightOrDiagonalTo(pos5));
        assertFalse(pos2.isStraightOrDiagonalTo(pos5));
        assertFalse(pos3.isStraightOrDiagonalTo(pos5));
        assertFalse(pos4.isStraightOrDiagonalTo(pos5));
        assertFalse(pos5.isStraightOrDiagonalTo(pos1));
        assertFalse(pos5.isStraightOrDiagonalTo(pos2));
        assertFalse(pos5.isStraightOrDiagonalTo(pos3));
        assertFalse(pos5.isStraightOrDiagonalTo(pos4));
    }

    @Test
    public void testPositionByCode() {
        Position a1 = Position.byCode("a1");
        assertEquals(a1.toString(), "a1");
        assertEquals(a1.column, 0, "column");
        assertEquals(a1.row, 0, "row");

        Position b3 = Position.byCode("b3");
        assertEquals(b3.toString(), "b3");
        assertEquals(b3.column, 1, "column");
        assertEquals(b3.row, 2, "row");
    }

    @Test
    public void testPositionByIndex() {
        Position b3 = Position.byCode("b3");
        Position actualPos = Position.byIndex(b3.getIndex());
        assertEquals(actualPos, b3);
    }

    @Test(dataProvider = "getTestStepData")
    public void testStep(Position pos, Direction direction, Position expectedPos) {
        Position actualPos = pos.step(direction);
        assertEquals(actualPos, expectedPos);
    }

    @DataProvider
    public Object[][] getTestStepData() {
        return new Object[][]{
                // middle of board -> all steps exists
                getPositionDirectionOptionalPosition("e4", Direction.DOWN_LEFT, "d3"),
                getPositionDirectionOptionalPosition("e4", Direction.DOWN, "e3"),
                getPositionDirectionOptionalPosition("e4", Direction.DOWN_RIGHT, "f3"),
                getPositionDirectionOptionalPosition("e4", Direction.LEFT, "d4"),
                getPositionDirectionOptionalPosition("e4", Direction.RIGHT, "f4"),
                getPositionDirectionOptionalPosition("e4", Direction.UP_LEFT, "d5"),
                getPositionDirectionOptionalPosition("e4", Direction.UP, "e5"),
                getPositionDirectionOptionalPosition("e4", Direction.UP_RIGHT, "f5"),
                // and now the corners -> where not all directions stay on the board
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
                getPositionDirectionOptionalPosition("h8", Direction.UP_RIGHT, null),
        };
    }

    private Object[] getPositionDirectionOptionalPosition(String posCode, Direction direction, String expectedPosCode) {
        Position pos = Position.byCode(posCode);
        Position newPos = expectedPosCode == null ? null : Position.byCode(expectedPosCode);
        return new Object[]{pos, direction, newPos};
    }
}

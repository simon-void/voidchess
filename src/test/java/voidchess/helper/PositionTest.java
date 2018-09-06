package voidchess.helper;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class PositionTest {
    @Test
    public void testGet() {
        Position pos1 = Position.Companion.get(0, 7);
        assertTrue(pos1.getRow() == 0);
        assertTrue(pos1.getColumn() == 7);

        Position pos2 = Position.Companion.byCode("a8");
        assertTrue(pos2.getColumn() == 0);
        assertTrue(pos2.getRow() == 7);
    }

    @Test(dataProvider = "getInvalidGetRowColumnData")
    public void testInvalidGetByIntsFails(int row, int column) {
        try {
            Position.Companion.get(row, column);
            fail("test should have failed on row:" + row + ", column:" + column + " but didn't");
        } catch (AssertionError | ArrayIndexOutOfBoundsException e) {
            // expected to fail
        }
    }

    @DataProvider
    private Object[][] getInvalidGetRowColumnData() {
        return new Object[][]{
            new Object[] {-1, 0},
            new Object[] {0, -1},
            new Object[] {8, 0},
            new Object[] {0, 8},
        };
    }

    @Test
    public void testIndex() {
        Position pos1 = Position.Companion.byCode("a1");
        assertEquals(pos1.getIndex(), 0);
        Position pos2 = Position.Companion.byCode("h8");
        assertEquals(pos2.getIndex(), 63);
    }

    @Test
    public void testToString() {
        Position pos1 = Position.Companion.get(0, 0);
        assertEquals("a1", pos1.toString());
        Position pos2 = Position.Companion.get(7, 0);
        assertEquals("a8", pos2.toString());
        Position pos3 = Position.Companion.get(7, 7);
        assertEquals("h8", pos3.toString());
    }

    @Test
    public void testEquals() {
        Position pos1 = Position.Companion.byCode("d4");
        Position pos2 = Position.Companion.byCode("e6");
        Position pos3 = Position.Companion.byCode("d4");

        assertFalse(pos1.equalsPosition(pos2));
        assertFalse(pos2.equalsPosition(pos3));
        assertTrue(pos1.equalsPosition(pos3));
    }

    @Test
    public void testInBounds() {
        assertFalse(Position.Companion.inBounds(-1, 4));
        assertFalse(Position.Companion.inBounds(1, 8));
        assertFalse(Position.Companion.inBounds(8, 4));
        assertFalse(Position.Companion.inBounds(1, -1));

        assertTrue(Position.Companion.inBounds(0, 0));
        assertTrue(Position.Companion.inBounds(0, 7));
        assertTrue(Position.Companion.inBounds(7, 0));
        assertTrue(Position.Companion.inBounds(7, 7));
        assertTrue(Position.Companion.inBounds(3, 4));
    }

    @Test
    public void testIsDiagonalTo() {
        Position pos1 = Position.Companion.byCode("a1");
        Position pos2 = Position.Companion.byCode("a8");
        Position pos3 = Position.Companion.byCode("h1");
        Position pos4 = Position.Companion.byCode("h8");

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
        Position pos1 = Position.Companion.byCode("a1");
        Position pos2 = Position.Companion.byCode("a8");
        Position pos3 = Position.Companion.byCode("h1");
        Position pos4 = Position.Companion.byCode("h8");

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
        Position pos1 = Position.Companion.byCode("a1");
        Position pos2 = Position.Companion.byCode("a8");
        Position pos3 = Position.Companion.byCode("h1");
        Position pos4 = Position.Companion.byCode("h8");

        Position pos5 = Position.Companion.byCode("b3");

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
}

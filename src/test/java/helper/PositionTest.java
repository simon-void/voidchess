package helper;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class PositionTest {
    @Test
    public void testConstructor() {
        Position pos1 = Position.get(0, 7);
        assertTrue(pos1.column == 7);
        assertTrue(pos1.row == 0);

        try {
            Position.get(-1, 0);
            fail("invalid position");
        } catch (AssertionError e) {
        }
        try {
            Position.get(8, 0);
            fail("invalid position");
        } catch (AssertionError e) {
        }

        Position pos2 = Position.get("a8");
        assertTrue(pos2.column == 0);
        assertTrue(pos2.row == 7);
    }

    @Test
    public void testToString() {
        Position pos1 = Position.get(0, 0);
        assertEquals("a1", pos1.toString());
        Position pos2 = Position.get(7, 0);
        assertEquals("a8", pos2.toString());
        Position pos3 = Position.get(7, 7);
        assertEquals("h8", pos3.toString());
    }

    @Test
    public void testEquals() {
        Position pos1 = Position.get("d4");
        Position pos2 = Position.get("e6");
        Position pos3 = Position.get("d4");

        assertFalse(pos1.equalsPosition(pos2));
        assertFalse(pos2.equalsPosition(pos3));
        assertTrue(pos1.equalsPosition(pos3));
    }

    @Test
    public void testNotInBounds() {
        assertTrue(Position.notInBounds(-1, 4));
        assertTrue(Position.notInBounds(1, 8));
        assertTrue(Position.notInBounds(8, 4));
        assertTrue(Position.notInBounds(1, -1));

        assertFalse(Position.notInBounds(0, 7));
        assertFalse(Position.notInBounds(7, 0));
        assertFalse(Position.notInBounds(0, 0));
        assertFalse(Position.notInBounds(7, 7));
        assertFalse(Position.notInBounds(3, 4));
    }
}

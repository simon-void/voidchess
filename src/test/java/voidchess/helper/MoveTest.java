package voidchess.helper;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class MoveTest {
    @Test
    public void testToString() {
        Move move = Move.get(Position.Companion.get("a1"), Position.Companion.get("h8"));
        assertEquals(move.toString(), "a1-h8");
    }

    @Test
    public void testConstructors() {
        Move.get(Position.Companion.get("b2"), Position.Companion.get("b3"));
        Move.get("b2-b3");
    }

    @Test
    public void testEquals() {
        Move move1 = Move.get("a1-a2");
        Move move2 = Move.get("a1-a2");
        Move move3 = Move.get("a1-a3");

        assertTrue(move1.equalsMove(move2));
        assertFalse(move1.equalsMove(move3));
    }
}

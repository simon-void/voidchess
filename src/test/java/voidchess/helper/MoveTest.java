package voidchess.helper;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class MoveTest {
    @Test
    public void testToString() {
        Move move = Move.Companion.get(Position.Companion.byCode("a1"), Position.Companion.byCode("h8"));
        assertEquals(move.toString(), "a1-h8");
    }

    @Test
    public void testConstructors() {
        Move.Companion.get(Position.Companion.byCode("b2"), Position.Companion.byCode("b3"));
        Move.Companion.byCode("b2-b3");
    }

    @Test
    public void testEquals() {
        Move move1 = Move.Companion.byCode("a1-a2");
        Move move2 = Move.Companion.byCode("a1-a2");
        Move move3 = Move.Companion.byCode("a1-a3");

        assertTrue(move1.equalsMove(move2));
        assertFalse(move1.equalsMove(move3));
    }
}

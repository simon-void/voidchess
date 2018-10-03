package voidchess.helper;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class MoveTest {
    @Test
    public void testToString() {
        Move move = Move.get(Position.byCode("a1"), Position.byCode("h8"));
        assertEquals(move.toString(), "a1-h8");
    }

    @Test
    public void testConstructors() {
        Move.get(Position.byCode("b2"), Position.byCode("b3"));
        Move.byCode("b2-b3");
    }

    @Test
    public void testEquals() {
        Move move1 = Move.byCode("a1-a2");
        Move move2 = Move.byCode("a1-a2");
        Move move3 = Move.byCode("a1-a3");

        assertTrue(move1.equalsMove(move2));
        assertFalse(move1.equalsMove(move3));
    }
}

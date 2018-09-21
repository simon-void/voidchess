package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.helper.Move;
import voidchess.helper.Position;

import static org.testng.Assert.assertEquals;

public class CastlingFigureTest {
    @Test
    public void testGetInitialPosition() {
        Position initial = Position.Companion.byCode("e1");
        Position second = Position.Companion.byCode("d1");

        King king = new King(true, initial);

        assertEquals(king.getInitialPosition(), initial);
        king.figureMoved(Move.Companion.get(initial, second));
        assertEquals(king.getInitialPosition(), initial);
    }
}

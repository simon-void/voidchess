package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.move.Move;
import voidchess.board.move.Position;

import static org.testng.Assert.assertEquals;

public class CastlingFigureTest {
    @Test
    public void testGetInitialPosition() {
        Position initial = Position.byCode("e1");
        Position second = Position.byCode("d1");

        King king = new King(true, initial);

        assertEquals(king.getInitialPosition(), initial);
        king.figureMoved(Move.get(initial, second));
        assertEquals(king.getInitialPosition(), initial);
    }
}

package voidchess.helper;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import voidchess.figures.Pawn;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author stephan
 */
public class ExtendedMoveTest {
    private ExtendedMove extendedMove;

    @BeforeMethod
    protected void setUp() {
        extendedMove =
                new ExtendedMove(
                        Move.Companion.get(
                                Position.Companion.byCode("a1"),
                                Position.Companion.byCode("d1")
                        ),
                        null,
                        null,
                        true,
                        false,
                        false,
                        false);
    }

    @Test
    public void testFrom() {
        assertTrue(extendedMove.getMove().getFrom().equalsPosition(Position.Companion.byCode("a1")));
    }

    @Test
    public void testTo() {
        assertTrue(extendedMove.getMove().getTo().equalsPosition(Position.Companion.byCode("d1")));
    }

    @Test
    public void testGetFigure() {
        assertTrue(extendedMove.getFigure() == null);
    }

    @Test
    public void testGetColorOfMove() {
        assertTrue(extendedMove.getColorOfMove());
    }

    @Test
    public void testIsRochade() {
        assertFalse(extendedMove.isRochade());
    }

    @Test
    public void testIsEnpassent() {
        assertFalse(extendedMove.isEnpassent());
    }

    @Test
    public void testHasHitFigure() {
        assertFalse(extendedMove.hasHitFigure());
        ExtendedMove hitPawn =
                new ExtendedMove(
                        Move.Companion.get(
                                Position.Companion.byCode("b5"),
                                Position.Companion.byCode("a6")
                        ),
                        null,
                        new Pawn(false, Position.Companion.byCode("a5"), true),
                        true,
                        false,
                        true,
                        false
                );
        assertTrue(hitPawn.hasHitFigure());
        ExtendedMove hitFigure =
                new ExtendedMove(
                        Move.Companion.get(
                                Position.Companion.byCode("b5"),
                                Position.Companion.byCode("a6")
                        ),
                        new Pawn(false, Position.Companion.byCode("a6"), false),
                        null,
                        true,
                        false,
                        true,
                        false);
        assertTrue(hitFigure.hasHitFigure());
    }
}

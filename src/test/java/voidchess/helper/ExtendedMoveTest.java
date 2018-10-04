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
                        Move.get(
                                Position.byCode("a1"),
                                Position.byCode("d1")
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
        assertTrue(extendedMove.getMove().from.equalsPosition(Position.byCode("a1")));
    }

    @Test
    public void testTo() {
        assertTrue(extendedMove.getMove().to.equalsPosition(Position.byCode("d1")));
    }

    @Test
    public void testGetFigure() {
        assertTrue(extendedMove.getFigureTaken() == null);
    }

    @Test
    public void testGetColorOfMove() {
        assertTrue(extendedMove.getColorOfMove());
    }

    @Test
    public void testIsCastling() {
        assertFalse(extendedMove.isCastling());
    }

    @Test
    public void testIsEnpassent() {
        assertFalse(extendedMove.isEnpassent());
    }

    @Test
    public void testHasHitFigure() {
        assertFalse(extendedMove.wasFigureTaken());
        ExtendedMove hitPawn =
                new ExtendedMove(
                        Move.get(
                                Position.byCode("b5"),
                                Position.byCode("a6")
                        ),
                        null,
                        new Pawn(false, Position.byCode("a5"), true),
                        true,
                        false,
                        true,
                        false
                );
        assertTrue(hitPawn.wasFigureTaken());
        ExtendedMove hitFigure =
                new ExtendedMove(
                        Move.get(
                                Position.byCode("b5"),
                                Position.byCode("a6")
                        ),
                        new Pawn(false, Position.byCode("a6"), false),
                        null,
                        true,
                        false,
                        true,
                        false);
        assertTrue(hitFigure.wasFigureTaken());
    }
}

package helper;

import figures.Pawn;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
                        Position.get("a1"),
                        Position.get("d1"),
                        null,
                        null,
                        true,
                        false,
                        false,
                        false);
    }

    @Test
    public void testFrom() {
        assertTrue(extendedMove.from.equalsPosition(Position.get("a1")));
    }

    @Test
    public void testTo() {
        assertTrue(extendedMove.to.equalsPosition(Position.get("d1")));
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
                        Position.get("b5"),
                        Position.get("a6"),
                        null,
                        new Pawn(false, Position.get("a5"), true),
                        true,
                        false,
                        true,
                        false
                );
        assertTrue(hitPawn.hasHitFigure());
        ExtendedMove hitFigure =
                new ExtendedMove(
                        Position.get("b5"),
                        Position.get("a6"),
                        new Pawn(false, Position.get("a6"), false),
                        null,
                        true,
                        false,
                        true,
                        false);
        assertTrue(hitFigure.hasHitFigure());
    }
}

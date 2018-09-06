package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by stephan on 26.07.2015.
 */
public class EvaluatedAsDrawTest {
    @Test
    public void testIsDraw() {
        Evaluated value = EvaluatedAsDraw.INSTANCE;
        assertFalse(value.isValue());
        assertTrue(value.isDraw());
        assertFalse(value.isCheckmate());
    }
}

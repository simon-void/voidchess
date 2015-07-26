package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by stephan on 26.07.2015.
 */
public class EvaluatedAsMattTest {
    @Test
    public void testIsMatt() {
        Evaluated value = new EvaluatedAsMatt(1, true);
        assertFalse(value.isValue());
        assertFalse(value.isDraw());
        assertTrue(value.isMatt());
    }
}

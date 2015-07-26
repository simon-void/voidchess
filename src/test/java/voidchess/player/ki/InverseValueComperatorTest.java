package voidchess.player.ki;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import voidchess.player.ki.evaluation.EvaluatedAsValue;

import static org.testng.Assert.assertTrue;

/**
 * @author stephan
 */
public class InverseValueComperatorTest {
    private InverseValueComperator comp;

    @BeforeMethod
    public void setUp() {
        comp = new InverseValueComperator();
    }

    @Test
    public void testCompare() {
        EvaluatedAsValue first = new EvaluatedAsValue(1.2f);
        EvaluatedAsValue second = new EvaluatedAsValue(-2.3f);

        assertTrue(comp.compare(first, second) == -1);
        assertTrue(comp.compare(second, first) == 1);
        assertTrue(comp.compare(first, first) == 0);
    }
}

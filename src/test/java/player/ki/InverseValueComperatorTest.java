package player.ki;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
        Float first = new Float(1.2);
        Float second = new Float(-2.3);

        assertTrue(comp.compare(first, second) == -1);
        assertTrue(comp.compare(second, first) == 1);
        assertTrue(comp.compare(first, first) == 0);
    }
}

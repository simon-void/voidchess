/*
 * Created on 03.10.2006
 */

package voidchess.player.ki;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedAsDraw;
import voidchess.player.ki.evaluation.EvaluatedAsMatt;
import voidchess.player.ki.evaluation.EvaluatedAsValue;

import static org.testng.Assert.*;

public class EvaluadedTest {
    @Test
    public void testChessvalueToString() {
        Evaluated value = new EvaluatedAsValue(-1);
        assertEquals(value.toString(), "-1,00");
        value = new EvaluatedAsValue(5.49999999999f);
        assertEquals(value.toString(), "5,50");

        value = EvaluatedAsDraw.INSTANCE;
        assertEquals(value.toString(), "0,00");

        value = new EvaluatedAsMatt(2, false);
        assertEquals(value.toString(), "Ich bin matt in 2.");

        value = new EvaluatedAsMatt(12, true);
        assertEquals(value.toString(), "Du bist matt in 12.");
    }

    @Test
    public void testIsCloseToByCombined() {
        assertTrue(
                new EvaluatedAsValue(-0.25f).isCloseToByCombined(
                        new EvaluatedAsValue(0.25f)
                )
        );
        assertTrue(
                EvaluatedAsDraw.INSTANCE.isCloseToByCombined(
                        new EvaluatedAsValue(0.5f)
                )
        );
        assertFalse(
                new EvaluatedAsValue(-0.3f).isCloseToByCombined(
                        new EvaluatedAsValue(0.3f)
                )
        );
        assertFalse(
                new EvaluatedAsValue(-0.2f).isCloseToByCombined(
                        new EvaluatedAsValue(0.5f)
                )
        );
        assertFalse(
                new EvaluatedAsMatt(1, false).isCloseToByCombined(
                        new EvaluatedAsMatt(2, false)
                )
        );
        assertFalse(
                new EvaluatedAsMatt(1, false).isCloseToByCombined(
                        new EvaluatedAsMatt(1, true)
                )
        );
        assertTrue(
                new EvaluatedAsMatt(1, false).isCloseToByCombined(
                        new EvaluatedAsMatt(1, false)
                )
        );
        assertFalse(
                EvaluatedAsDraw.INSTANCE.isCloseToByCombined(
                        new EvaluatedAsMatt(1, false)
                )
        );
    }

    @Test
    public void testIsCloseToByPrimary() {
        assertTrue(
                new EvaluatedAsValue(-0.65f).isCloseToByPrimary(
                        new EvaluatedAsValue(0.25f)
                )
        );
        assertTrue(
                new EvaluatedAsValue(0f).isCloseToByPrimary(
                        new EvaluatedAsValue(1f)
                )
        );
        assertTrue(
                EvaluatedAsDraw.INSTANCE.isCloseToByPrimary(
                        new EvaluatedAsValue(0.5f)
                )
        );
        assertFalse(
                new EvaluatedAsValue(-0.3f).isCloseToByPrimary(
                        new EvaluatedAsValue(0.8f)
                )
        );
        assertFalse(
                new EvaluatedAsValue(-0.6f).isCloseToByPrimary(
                        new EvaluatedAsValue(0.5f)
                )
        );
        assertFalse(
                new EvaluatedAsMatt(1, false).isCloseToByPrimary(
                        new EvaluatedAsMatt(2, false)
                )
        );
        assertFalse(
                new EvaluatedAsMatt(1, false).isCloseToByPrimary(
                        new EvaluatedAsMatt(1, true)
                )
        );
        assertTrue(
                new EvaluatedAsMatt(1, false).isCloseToByPrimary(
                        new EvaluatedAsMatt(1, false)
                )
        );
        assertFalse(
                EvaluatedAsDraw.INSTANCE.isCloseToByPrimary(
                        new EvaluatedAsMatt(1, false)
                )
        );
    }

    @Test(dataProvider = "mattEvaluatedProvider")
    public void assertIsEvaluatedAsMatt(Evaluated mattValue) {
        assertTrue(mattValue.isMatt(), "this should be a matt evaluation:"+mattValue);
    }

    @Test(dataProvider = "notMattEvaluatedProvider")
    public void assertIsNotEvaluatedAsMatt(Evaluated notMattValue) {
        assertFalse(notMattValue.isMatt(), "this shouldn't be a matt evaluation");
    }

    @DataProvider(name="mattEvaluatedProvider")
    public Object[][] getMattEvaluated() {
        return new Object[][] {
                new Object[] {new EvaluatedAsMatt(1, true)},
                new Object[] {new EvaluatedAsMatt(20, true)},
                new Object[] {new EvaluatedAsMatt(1, false)},
                new Object[] {new EvaluatedAsMatt(20, false)},
        };
    }

    @DataProvider(name="notMattEvaluatedProvider")
    public Object[][] getNotMattEvaluated() {
        return new Object[][] {
                new Object[] {EvaluatedAsDraw.INSTANCE},
                new Object[] {new EvaluatedAsValue(0f)},
                new Object[] {new EvaluatedAsValue(1f)},
                new Object[] {new EvaluatedAsValue(-1f)},
                new Object[] {new EvaluatedAsValue(100f)},
                new Object[] {new EvaluatedAsValue(-100f)},
                //fromValue should safe-guard against too big values being interpreted as
                new Object[] {new EvaluatedAsValue(1000000000f)},
                new Object[] {new EvaluatedAsValue(-1000000000f)},
        };
    }
}

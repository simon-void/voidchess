/*
 * Created on 03.10.2006
 */

package voidchess.player.ki;

import org.testng.annotations.Test;
import voidchess.player.ki.evaluation.*;

import static org.testng.Assert.*;

public class EvaluadedTest {
    @Test
    public void testChessvalueToString() {
        Evaluated value = new Ongoing(-1);
        assertEquals(value.toString().replace('.', ','), "-1,00");
        value = new Ongoing(5.49999999999f);
        assertEquals(value.toString().replace('.', ','), "5,50");

        value = Draw.INSTANCE;
        assertEquals(value.toString().replace('.', ','), "0,00");

        value = new CheckmateSelf(2);
        assertEquals(value.toString(), "checkmate in 2");

        value = new CheckmateOther(12);
        assertEquals(value.toString(), "checkmate in 12");
    }

    @Test
    public void testIsCloseToByCombined() {
        assertTrue(
                new Ongoing(-0.25f).isCloseToByCombined(
                        new Ongoing(0.25f)
                )
        );
        assertTrue(
                Draw.INSTANCE.isCloseToByCombined(
                        new Ongoing(0.5f)
                )
        );
        assertFalse(
                new Ongoing(-0.3f).isCloseToByCombined(
                        new Ongoing(0.3f)
                )
        );
        assertFalse(
                new Ongoing(-0.2f).isCloseToByCombined(
                        new Ongoing(0.5f)
                )
        );
        assertFalse(
                new CheckmateSelf(1).isCloseToByCombined(
                        new CheckmateSelf(2)
                )
        );
        assertFalse(
                new CheckmateSelf(1).isCloseToByCombined(
                        new CheckmateOther(1)
                )
        );
        assertTrue(
                new CheckmateSelf(1).isCloseToByCombined(
                        new CheckmateSelf(1)
                )
        );
        assertFalse(
                Draw.INSTANCE.isCloseToByCombined(
                        new CheckmateSelf(1)
                )
        );
    }

    @Test
    public void testIsCloseToByPrimary() {
        assertTrue(
                new Ongoing(-0.65f).isCloseToByPrimary(
                        new Ongoing(0.25f)
                )
        );
        assertTrue(
                new Ongoing(0f).isCloseToByPrimary(
                        new Ongoing(1f)
                )
        );
        assertTrue(
                Draw.INSTANCE.isCloseToByPrimary(
                        new Ongoing(0.5f)
                )
        );
        assertFalse(
                new Ongoing(-0.3f).isCloseToByPrimary(
                        new Ongoing(0.8f)
                )
        );
        assertFalse(
                new Ongoing(-0.6f).isCloseToByPrimary(
                        new Ongoing(0.5f)
                )
        );
        assertFalse(
                new CheckmateSelf(1).isCloseToByPrimary(
                        new CheckmateSelf(2)
                )
        );
        assertFalse(
                new CheckmateSelf(1).isCloseToByPrimary(
                        new CheckmateOther(1)
                )
        );
        assertTrue(
                new CheckmateSelf(1).isCloseToByPrimary(
                        new CheckmateSelf(1)
                )
        );
        assertFalse(
                Draw.INSTANCE.isCloseToByPrimary(
                        new CheckmateSelf(1)
                )
        );
    }


    @Test
    public void testDrawCompareTo() {
        assertTrue(Draw.INSTANCE.compareTo(Draw.INSTANCE) ==0);
        assertTrue(Draw.INSTANCE.compareTo(new Ongoing(0f)) <0);
        assertTrue(Draw.INSTANCE.compareTo(new Ongoing(-0.0000000000000000000001f))>0);
    }
}

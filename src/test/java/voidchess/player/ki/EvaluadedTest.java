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
        value = new Ongoing(5.49999999999);
        assertEquals(value.toString().replace('.', ','), "5,50");

        value = Draw.INSTANCE;
        assertEquals(value.toString(), "draw");

        value = new CheckmateSelf(2);
        assertEquals(value.toString(), "checkmate in 2");

        value = new CheckmateOther(12);
        assertEquals(value.toString(), "checkmate in 12");
    }

    @Test
    public void testIsCloseToByCombined() {
        assertTrue(
                new Ongoing(-0.25).isCloseToByCombined(
                        new Ongoing(0.25)
                )
        );
        assertTrue(
                Draw.INSTANCE.isCloseToByCombined(
                        new Ongoing(0.5)
                )
        );
        assertFalse(
                new Ongoing(-0.3).isCloseToByCombined(
                        new Ongoing(0.3)
                )
        );
        assertFalse(
                new Ongoing(-0.2).isCloseToByCombined(
                        new Ongoing(0.5)
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
                new Ongoing(-0.65).isCloseToByPrimary(
                        new Ongoing(0.25)
                )
        );
        assertTrue(
                new Ongoing(0).isCloseToByPrimary(
                        new Ongoing(1)
                )
        );
        assertTrue(
                Draw.INSTANCE.isCloseToByPrimary(
                        new Ongoing(0.5)
                )
        );
        assertFalse(
                new Ongoing(-0.3).isCloseToByPrimary(
                        new Ongoing(0.8)
                )
        );
        assertFalse(
                new Ongoing(-0.6).isCloseToByPrimary(
                        new Ongoing(0.5)
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
        assertTrue(Draw.INSTANCE.compareTo(new Ongoing(0.0)) <0);
        assertTrue(Draw.INSTANCE.compareTo(new Ongoing(-0.0000000000000000000001))>0);
    }
}

package voidchess.player.ki;

import org.testng.annotations.Test;
import voidchess.player.ki.evaluation.*;

import java.text.DecimalFormat;

import static org.testng.Assert.*;

public class EvaluatedTest {
    @Test
    public void testEvaluatedToString() {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMinimumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        Evaluated value = new Ongoing(-1);
        assertEquals(value.toString().replace('.', ','), formatter.format(-1.0));
        value = new Ongoing(5.49999999999);
        assertEquals(value.toString().replace('.', ','), formatter.format(5.5));

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
                new Ongoing(-0.85, 0.6).isCloseToByCombined(
                        new Ongoing(1.25, -1.0)
                )
        );
        assertTrue(
                new Ongoing(0.0, 0.0).isCloseToByCombined(
                        new Ongoing(0.5, 0.0)
                )
        );
        assertTrue(
                Draw.INSTANCE.isCloseToByCombined(
                        new Ongoing(-1.5, 1.0)
                )
        );
        assertFalse(
                new Ongoing(0.0, -0.3).isCloseToByCombined(
                        new Ongoing(0.0, 0.3)
                )
        );
        assertFalse(
                new Ongoing(0.2, -0.5).isCloseToByCombined(
                        new Ongoing(0.1, 0.3)
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

    @Test
    public void testCheckmateSelfCompareTo() {
        assertTrue(new CheckmateSelf(5).compareTo(Draw.INSTANCE) <0);
        assertTrue(new CheckmateSelf(5).compareTo(new Ongoing(-10.0)) <0);
        assertTrue(new CheckmateSelf(5).compareTo(new CheckmateOther(3)) <0);
        assertTrue(new CheckmateSelf(5).compareTo(new CheckmateSelf(3))  >0);
        assertTrue(new CheckmateSelf(5).compareTo(new CheckmateSelf(5)) ==0);

        CheckmateSelf selfCM1 = new CheckmateSelf(5);
        CheckmateSelf selfCM2 = new CheckmateSelf(5);
        selfCM1.setSecondaryEvaluation(3.0);
        selfCM2.setSecondaryEvaluation(2.0);
        assertTrue(selfCM1.compareTo(selfCM2) >0);
    }
}

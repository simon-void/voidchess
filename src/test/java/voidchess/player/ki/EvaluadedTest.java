/*
 * Created on 03.10.2006
 */

package voidchess.player.ki;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class EvaluadedTest {
    @Test
    public void testChessvalueToString() {
        Evaluaded value = Evaluaded.fromValues(-1, 0);
        assertEquals("-1,00", value.toString());
        value = Evaluaded.fromValues(5.49999999999f, 0);
        assertEquals("5,50", value.toString());

        value = Evaluaded.DRAW;
        assertEquals("0,00", value.toString());

        value = Evaluaded.getOtherPlayerIsMatt(2);
        assertEquals("ich bin matt in 2", value.toString());

        value = Evaluaded.getThisComputerPlayerIsMatt(12);
        assertEquals("du bist matt in 12", value.toString());
    }

    @Test
    public void testHasAlmostSameValue() {
        assertTrue(
                Evaluaded.fromValues(-0.25f, 0).hasAlmostSameValue(
                        Evaluaded.fromValues(0.25f, 0)
                )
        );
        assertTrue(
                Evaluaded.DRAW.hasAlmostSameValue(
                        Evaluaded.fromValues(0.5f, 0)
                )
        );
        assertFalse(
                Evaluaded.fromValues(-0.3f, 0).hasAlmostSameValue(
                        Evaluaded.fromValues(0.3f, 0)
                )
        );
        assertFalse(
                Evaluaded.fromValues(-0.2f, 0).hasAlmostSameValue(
                        Evaluaded.fromValues(0.5f, 0)
                )
        );
        assertFalse(
                Evaluaded.getThisComputerPlayerIsMatt(1).hasAlmostSameValue(
                        Evaluaded.getThisComputerPlayerIsMatt(2)
                )
        );
        assertFalse(
                Evaluaded.getThisComputerPlayerIsMatt(1).hasAlmostSameValue(
                        Evaluaded.getOtherPlayerIsMatt(1)
                )
        );
        assertTrue(
                Evaluaded.getThisComputerPlayerIsMatt(1).hasAlmostSameValue(
                        Evaluaded.getThisComputerPlayerIsMatt(1)
                )
        );
        assertFalse(
                Evaluaded.DRAW.hasAlmostSameValue(
                        Evaluaded.getThisComputerPlayerIsMatt(1)
                )
        );
    }
}

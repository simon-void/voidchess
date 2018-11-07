package voidchess.player.ki

import org.testng.annotations.Test
import voidchess.player.ki.evaluation.*

import java.text.DecimalFormat

import org.testng.Assert.*

class EvaluatedTest {
    @Test
    fun testEvaluatedToString() {
        val formatter = DecimalFormat()
        formatter.minimumFractionDigits = 2
        formatter.minimumFractionDigits = 2

        var value: Evaluated = Ongoing(-1.0)
        assertEquals(value.toString().replace('.', ','), formatter.format(-1.0))
        value = Ongoing(5.49999999999)
        assertEquals(value.toString().replace('.', ','), formatter.format(5.5))

        value = Draw
        assertEquals(value.toString(), "draw")

        value = CheckmateSelf(2)
        assertEquals(value.toString(), "checkmate in 2")

        value = CheckmateOther(12)
        assertEquals(value.toString(), "checkmate in 12")
    }

    @Test
    fun testIsCloseToByCombined() {
        assertTrue(
                Ongoing(-0.65, 0.6).isCloseToByCombined(
                        Ongoing(1.15, -1.0)
                )
        )
        assertTrue(
                Ongoing(0.0, 0.0).isCloseToByCombined(
                        Ongoing(0.2, 0.0)
                )
        )
        assertTrue(
                Draw.isCloseToByCombined(
                        Ongoing(-1.2, 1.0)
                )
        )
        assertFalse(
                Ongoing(0.0, -0.3).isCloseToByCombined(
                        Ongoing(0.0, 0.3)
                )
        )
        assertFalse(
                Ongoing(0.2, -0.5).isCloseToByCombined(
                        Ongoing(0.1, 0.3)
                )
        )
        assertFalse(
                CheckmateSelf(1).isCloseToByCombined(
                        CheckmateSelf(2)
                )
        )
        assertFalse(
                CheckmateSelf(1).isCloseToByCombined(
                        CheckmateOther(1)
                )
        )
        assertTrue(
                CheckmateSelf(1).isCloseToByCombined(
                        CheckmateSelf(1)
                )
        )
        assertFalse(
                Draw.isCloseToByCombined(
                        CheckmateSelf(1)
                )
        )
    }

    @Test
    fun testIsCloseToByPrimary() {
        assertTrue(
                Ongoing(-0.65).isCloseToByPrimary(
                        Ongoing(0.25)
                )
        )
        assertTrue(
                Ongoing(0.0).isCloseToByPrimary(
                        Ongoing(1.0)
                )
        )
        assertTrue(
                Draw.isCloseToByPrimary(
                        Ongoing(0.5)
                )
        )
        assertFalse(
                Ongoing(-0.3).isCloseToByPrimary(
                        Ongoing(0.8)
                )
        )
        assertFalse(
                Ongoing(-0.6).isCloseToByPrimary(
                        Ongoing(0.5)
                )
        )
        assertFalse(
                CheckmateSelf(1).isCloseToByPrimary(
                        CheckmateSelf(2)
                )
        )
        assertFalse(
                CheckmateSelf(1).isCloseToByPrimary(
                        CheckmateOther(1)
                )
        )
        assertTrue(
                CheckmateSelf(1).isCloseToByPrimary(
                        CheckmateSelf(1)
                )
        )
        assertFalse(
                Draw.isCloseToByPrimary(
                        CheckmateSelf(1)
                )
        )
    }


    @Test
    fun testDrawCompareTo() {
        assertTrue(Draw.compareTo(Draw) == 0)
        assertTrue(Draw < Ongoing(0.0))
        assertTrue(Draw > Ongoing(-0.0000000000000000000001))
    }

    @Test
    fun testCheckmateSelfCompareTo() {
        assertTrue(CheckmateSelf(5) < Draw)
        assertTrue(CheckmateSelf(5) < Ongoing(-10.0))
        assertTrue(CheckmateSelf(5) < CheckmateOther(3))
        assertTrue(CheckmateSelf(5) > CheckmateSelf(3))
        assertTrue(CheckmateSelf(5).compareTo(CheckmateSelf(5)) == 0)

        val selfCM1 = CheckmateSelf(5)
        val selfCM2 = CheckmateSelf(5)
        selfCM1.setSecondaryEvaluation(3.0)
        selfCM2.setSecondaryEvaluation(2.0)
        assertTrue(selfCM1 > selfCM2)
    }
}

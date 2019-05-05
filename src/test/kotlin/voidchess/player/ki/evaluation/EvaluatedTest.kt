package voidchess.player.ki.evaluation

import org.testng.annotations.Test

import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue


class EvaluatedTest {
    @Test
    fun testCloseTo() {
        val value1 = getValue(1.0, 5.0)
        val value2 = getValue(2.1, 3.9)
        val value3 = getValue(1.5, 0.0)

        assertFalse(value1.isCloseToByPrimary(value2))
        assertTrue(value1.isCloseToByPrimary(value3))
        assertTrue(value1.isCloseToByCombined(value2))
        assertFalse(value1.isCloseToByCombined(value3))

        assertFalse(value2.isCloseToByPrimary(value1))
        assertTrue(value2.isCloseToByPrimary(value3))
        assertTrue(value2.isCloseToByCombined(value1))
        assertFalse(value2.isCloseToByCombined(value3))

        assertTrue(value3.isCloseToByPrimary(value2))
        assertTrue(value3.isCloseToByPrimary(value1))
        assertFalse(value3.isCloseToByCombined(value2))
        assertFalse(value3.isCloseToByCombined(value1))
    }

    private fun getValue(primary: Double, secondary: Double): Ongoing {
        val value = Ongoing(primary)
        value.setSecondaryEvaluation(secondary)
        return value
    }
}

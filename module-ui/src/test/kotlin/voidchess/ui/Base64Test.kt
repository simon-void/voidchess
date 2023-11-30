package voidchess.ui

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.move.Move
import voidchess.ui.swing.toBase64
import kotlin.test.assertEquals

class Base64Test {
    @Test(dataProvider = "base64DataProvider")
    fun testBase64(moveCode: String, expectedBase64Code: String) {
        val move = Move.byCode(moveCode)
        val actualBase64Code = move.toBase64()
        assertEquals(expectedBase64Code, actualBase64Code)
    }

    @DataProvider
    fun base64DataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf("a1-h8", "A_"),
        arrayOf("h1-a8", "H4"),
        arrayOf("b2Qb1", "JBQ"),
        arrayOf("c2Kb1", "KBK"),
        arrayOf("d2Re1", "LER"),
        arrayOf("f2Bf1", "NFB"),
    )

    @Test
    fun convertMovesToBase64() {
        val moves = "a2-a4,g8-f6,a4-a5,b7-b5,a5-b6,g7-g6,b6-b7,f8-g7,b7Qa8,e8-h8"
        val movesAsBase64 = moves.split(",").joinToString("") { Move.byCode(it).toBase64() }
        assertEquals("IY-tYgxhgp2upx92x4Q8_", movesAsBase64)
    }
}
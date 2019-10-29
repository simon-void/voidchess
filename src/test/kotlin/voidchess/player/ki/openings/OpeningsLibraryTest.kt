package voidchess.player.ki.openings

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.move.Move


class OpeningsLibraryTest {

    val validOpeningsProvider: Array<Array<Any>>
        @DataProvider(name = "validOpeningsProvider")
        get() = arrayOf(
                arrayOf("e2-e4,c7-c5,g1-f3,d7-d6,d2-d4,c5-d4,f3-d4,g8-f6,b1-c3,a7-a6", 10),
                arrayOf("e2-e4, c7-c5,  b1-c3, b8-c6   ,g2-g3,g7-g6,f1-g2,f8-g7", 8),
                arrayOf("e2-e4", 1),
                arrayOf("", 0)
        )

    val invalidOpeningsProvider: Array<Array<Any>>
        @DataProvider(name = "invalidOpeningsProvider")
        get() = arrayOf(
                arrayOf<Any>("e4,e5"),
                arrayOf<Any>(","),
                arrayOf<Any>("e2_e4,e7_e5"),
                arrayOf<Any>("e2e4,e7e5"),
                arrayOf<Any>("e2-e4;e7-e5"),
                arrayOf<Any>("e2-e4 e7-e5"),
                arrayOf<Any>("e3-e4"),
                arrayOf<Any>("e2-e5,e7-e5"),
                arrayOf<Any>("e2-e4,d2-d4"),
                arrayOf<Any>("e2-e4,e6-e5"),
                arrayOf<Any>("e2-e4,e7-e4"),
                arrayOf<Any>("e2-e4,e7-e5,d7-d5"),
                arrayOf<Any>("e2-e4,e7-e5,g1-f3,d7-d6,d2-d4,e5-d4,b8-c6,f1-b5")
        )

    @Test(dataProvider = "validOpeningsProvider")
    fun testValidOpenings(validOpeningSequence: String, numberOfMoves: Int) {
        val moves = OpeningsLibrary.splitAndCheckOpeningSequence(validOpeningSequence)
        assertEquals(moves.size, numberOfMoves, "number of moves in opening")
        for (move in moves) {
            assertTrue(Move.isValid(move), "should be valid move: $move")
        }
    }

    @Test(dataProvider = "invalidOpeningsProvider", expectedExceptions = [IllegalArgumentException::class])
    fun testInValidOpenings(invalidOpeningSequence: String) {
        OpeningsLibrary.splitAndCheckOpeningSequence(invalidOpeningSequence)
    }
}

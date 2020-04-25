package voidchess.engine.openings

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import kotlin.test.assertNull


internal class OpeningsLibraryTest {
    @DataProvider(name = "validOpeningsProvider")
    fun getValidOpeningsProvider(): Array<Array<Any>> = arrayOf(
        arrayOf("e2-e4,c7-c5,g1-f3,d7-d6,d2-d4,c5-d4,f3-d4,g8-f6,b1-c3,a7-a6", 10),
        arrayOf("e2-e4, c7-c5,  b1-c3, b8-c6   ,g2-g3,g7-g6,f1-g2,f8-g7", 8),
        arrayOf("e2-e4", 1),
        arrayOf("e2-e4,(d7-d6)", 2),
        arrayOf("", 0)
    )

    @DataProvider(name = "invalidOpeningsProvider")
    fun getInvalidOpeningsProvider(): Array<Array<Any>> = arrayOf(
        arrayOf<Any>("e4,e5"),
        arrayOf<Any>(","),
        arrayOf<Any>("e2_e4,e7_e5"),
        arrayOf<Any>("e2e4,e7e5"),
        arrayOf<Any>("e2-e4;e7-e5"),
        arrayOf<Any>("e2-e4 e7-e5"),
        arrayOf<Any>("e3-e4"),
        arrayOf<Any>("e3-e4)"),
        arrayOf<Any>("(e3-e4"),
        arrayOf<Any>("e2-e5,e7-e5"),
        arrayOf<Any>("e2-e4,d2-d4"),
        arrayOf<Any>("e2-e4,e6-e5"),
        arrayOf<Any>("e2-e4,e7-e4"),
        arrayOf<Any>("e2-e4,e7-e5,d7-d5"),
        arrayOf<Any>("e2-e4,e7-e5,g1-f3,d7-d6,d2-d4,e5-d4,b8-c6,f1-b5")
    )

    @Test(dataProvider = "validOpeningsProvider")
    fun testValidOpenings(validOpeningSequence: String, numberOfTokens: Int) {
        val tokens = OpeningsLibrary.splitAndCheckOpeningSequence(validOpeningSequence)
        assertEquals(tokens.size, numberOfTokens, "number of moves in opening")
    }

    @Test(dataProvider = "invalidOpeningsProvider", expectedExceptions = [IllegalArgumentException::class])
    fun testInValidOpenings(invalidOpeningSequence: String) {
        OpeningsLibrary.splitAndCheckOpeningSequence(invalidOpeningSequence)
    }

    @Test
    fun testLookUpNextMove() {
        val openingsLibrary = OpeningsLibrary(listOf("e2-e4, e7-e5, (d2-d4), e5-d4"))

        var nextMove = openingsLibrary.lookUpNextMove(StartConfig.ClassicConfig, listOf(Move.byCode("e2-e4"))) { _, _->}
        checkNotNull(nextMove)
        assertEquals(nextMove.move, Move.byCode("e7-e5"))

        nextMove = openingsLibrary.lookUpNextMove(StartConfig.ClassicConfig, listOf("e2-e4", "e7-e5").map{Move.byCode(it)}) { _, _->}
        assertNull(nextMove)

        nextMove = openingsLibrary.lookUpNextMove(StartConfig.ClassicConfig, listOf("e2-e4", "e7-e5", "d2-d4").map{Move.byCode(it)}) { _, _->}
        checkNotNull(nextMove)
        assertEquals(nextMove.move, Move.byCode("e5-d4"))
    }
}

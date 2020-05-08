package voidchess.common.board.move

import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.DataProvider


class MoveTest {
    @Test(dataProvider = "getValidByCodeData")
    fun testByCodeToString(moveCode: String) {
        val move = Move.byCode(moveCode)
        assertEquals(move.toString(), moveCode)
    }

    @DataProvider
    fun getValidByCodeData(): Array<Array<String>> = arrayOf(
        arrayOf("a1-h8"),
        arrayOf("h1-a8"),
        arrayOf("h1-g8"), // even invalid game moves are valid moves instances (because the UI could use it to ask if this move is movable)
        arrayOf("d2-e4"),
        arrayOf("a2Qb1"),
        arrayOf("e2Re1"),
        arrayOf("h7Kg8"),
        arrayOf("c7Bc8")
    )

    @Test(dataProvider = "getInvalidByCodeData", expectedExceptions = [IllegalArgumentException::class])
    fun testInvalidByCode(invalidMoveCode: String) {
        Move.byCode(invalidMoveCode)
    }

    @DataProvider
    fun getInvalidByCodeData(): Array<Array<String>> = arrayOf(
        arrayOf("a1-a0"),
        arrayOf("h1-i1"),
        arrayOf("h1-h9"),
        arrayOf("d2Ke4"), // not a valid pawn promotion move
        arrayOf("a2Qc1"),
        arrayOf("e2Re8"),
        arrayOf("h7Kh1"),
        arrayOf("c7Bb1")
    )

    @Test
    fun testConstructors() {
        Move[Position.byCode("b2"), Position.byCode("b3")]
        Move.byCode("b2-b3")
    }

    @Test
    fun testEquals() {
        val move1 = Move.byCode("a1-a2")
        val move2 = Move.byCode("a1-a2")
        val move3 = Move.byCode("a1-a3")

        assertTrue(move1.equalsMove(move2))
        assertFalse(move1.equalsMove(move3))
    }
}

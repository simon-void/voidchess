package voidchess.common.board.move

import org.testng.annotations.Test

import org.testng.Assert.*
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class MoveTest {
    @Test
    fun testToString() {
        val move = Move[Position.byCode("a1"), Position.byCode("h8")]
        assertEquals(move.toString(), "a1-h8")
    }

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

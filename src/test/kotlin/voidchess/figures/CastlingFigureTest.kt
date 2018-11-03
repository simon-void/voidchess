package voidchess.figures

import org.testng.annotations.Test
import voidchess.board.move.Move
import voidchess.board.move.Position
import kotlin.test.assertEquals

class CastlingFigureTest {
    @Test
    fun testGetInitialPosition() {
        val initial = Position.byCode("e1")
        val second = Position.byCode("d1")

        val king = King(true, initial)

        assertEquals(initial, king.initialPosition)
        king.figureMoved(Move[initial, second])
        assertEquals(initial, king.initialPosition)
    }
}

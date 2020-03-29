package voidchess.board.move

import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import voidchess.figures.Pawn

import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue


class ExtendedMoveTest {
    private var extendedMove: ExtendedMove? = null

    @BeforeMethod
    private fun setUp() {
        extendedMove = ExtendedMove(
                Move[Position.byCode("a1"), Position.byCode("d1")],
                null, null,
                true,
                false,
                false,
                false)
    }

    @Test
    fun testFrom() {
        assertTrue(extendedMove!!.move.from.equalsPosition(Position.byCode("a1")))
    }

    @Test
    fun testTo() {
        assertTrue(extendedMove!!.move.to.equalsPosition(Position.byCode("d1")))
    }

    @Test
    fun testGetFigure() {
        assertTrue(extendedMove!!.figureTaken == null)
    }

    @Test
    fun testGetColorOfMove() {
        assertTrue(extendedMove!!.colorOfMove)
    }

    @Test
    fun testIsCastling() {
        assertFalse(extendedMove!!.isCastling)
    }

    @Test
    fun testIsEnpassent() {
        assertFalse(extendedMove!!.isEnPassant)
    }

    @Test
    fun testHasHitFigure() {
        assertFalse(extendedMove!!.wasFigureTaken)
        val hitPawn = ExtendedMove(
                Move[Position.byCode("b5"), Position.byCode("a6")], null,
                Pawn(false, Position.byCode("a5"), true),
                true,
                false,
                true,
                false
        )
        assertTrue(hitPawn.wasFigureTaken)
        val hitFigure = ExtendedMove(
                Move[Position.byCode("b5"), Position.byCode("a6")],
                Pawn(false, Position.byCode("a6"), false), null,
                true,
                false,
                true,
                false)
        assertTrue(hitFigure.wasFigureTaken)
    }
}

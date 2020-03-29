package voidchess.board.move

import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import voidchess.figures.Pawn

import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import voidchess.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


class ExtendedMoveTest {
    private var extendedMove: ExtendedMove? = null

    @BeforeMethod
    private fun setUp() {
        extendedMove = ExtendedMove(
            move = Move[Position.byCode(
                "a1"
            ), Position.byCode("d1")],
            figureTaken = null, enpassantPawnOrCastlingRook = null,
            colorOfMove = true,
            isCastling = false,
            isEnPassant = false,
            isPawnTransformation = false
        )
    }

    @Test
    fun testFrom() {
        assertTrue(extendedMove!!.move.from.equalsPosition(
            Position.byCode(
                "a1"
            )
        ))
    }

    @Test
    fun testTo() {
        assertTrue(extendedMove!!.move.to.equalsPosition(
            Position.byCode(
                "d1"
            )
        ))
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
            move = Move[Position.byCode(
                "b5"
            ), Position.byCode("a6")], figureTaken = null,
            enpassantPawnOrCastlingRook = Pawn(
                false,
                Position.byCode("a5"),
                true
            ),
            colorOfMove = true,
            isCastling = false,
            isEnPassant = true,
            isPawnTransformation = false
        )
        assertTrue(hitPawn.wasFigureTaken)
        val hitFigure = ExtendedMove(
            move = Move[Position.byCode(
                "b5"
            ), Position.byCode("a6")],
            figureTaken = Pawn(
                false,
                Position.byCode("a6"),
                false
            ), enpassantPawnOrCastlingRook = null,
            colorOfMove = true,
            isCastling = false,
            isEnPassant = true,
            isPawnTransformation = false
        )
        assertTrue(hitFigure.wasFigureTaken)
    }
}

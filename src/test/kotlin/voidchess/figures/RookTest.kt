package voidchess.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.SimpleArrayBoard
import voidchess.board.getFigure
import voidchess.board.move.Move
import voidchess.board.move.Position

import java.util.LinkedList

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class RookTest {
    @Test
    fun testIsReachable() {
        val des = "white 0 Pawn-white-c2-false Rook-white-c4-2 " + "Knight-black-g4 King-white-e1-0 King-black-e8-0"
        val game = ChessGame(des)

        val from = Position.byCode("c4")
        val to1 = Position.byCode("c3")
        val to2 = Position.byCode("g4")
        val to3 = Position.byCode("c2")
        val to4 = Position.byCode("h4")
        val to5 = Position.byCode("d5")

        val rook = Rook(true, from)
        assertTrue(rook.isReachable(to1, game))
        assertTrue(rook.isReachable(to2, game))
        assertFalse(rook.isReachable(to3, game))
        assertFalse(rook.isReachable(to4, game))
        assertFalse(rook.isReachable(to5, game))
        assertFalse(rook.isReachable(from, game))
    }

    @Test
    fun testUndoMove() {
        val rook = FigureFactory.getRook(Position.byCode("a1"), false)

        assertTrue(rook.canCastle())
        rook.figureMoved(Move.byCode("a1-b1"))
        assertFalse(rook.canCastle())
        rook.undoMove(Position.byCode("a1"))
        assertTrue(rook.canCastle())
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(des: String, rookPosCode: String, expectedNumberOfMoves: Int) {
        val game = SimpleArrayBoard(des)

        val rook = game.getFigure(Position.byCode(rookPosCode))
        val moveIter = LinkedList<Move>()
        rook.getPossibleMoves(game, moveIter)
        assertEquals(expectedNumberOfMoves, moveIter.size)
    }

    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e2-0 Pawn-black-b4-false Rook-white-e4-2 King-black-e8-0", "e4", 11)
    )
}

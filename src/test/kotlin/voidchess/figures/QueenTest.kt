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


class QueenTest {
    @Test
    fun testIsReachable() {
        val des = "white 0 Queen-white-e2  King-white-e1-0  King-black-e8-0"
        val game = ChessGame(des)

        val from = Position.byCode("e2")
        val to1 = Position.byCode("e5")
        val to2 = Position.byCode("c4")
        val to3 = Position.byCode("d4")
        val to4 = Position.byCode("e1")
        val to5 = Position.byCode("e8")

        val queen = Queen(true, from)
        assertTrue(queen.isReachable(to1, game))
        assertTrue(queen.isReachable(to2, game))
        assertFalse(queen.isReachable(to3, game))
        assertFalse(queen.isReachable(to4, game))
        assertTrue(queen.isReachable(to5, game))
        assertFalse(queen.isReachable(from, game))
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(des: String, queenPosCode: String, expectedNumberOfMoves: Int) {
        val game = SimpleArrayBoard(des)

        val queen = game.getFigure(Position.byCode(queenPosCode))
        val moveIter = LinkedList<Move>()
        queen.getPossibleMoves(game, moveIter)
        assertEquals(expectedNumberOfMoves, moveIter.size)
    }

    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Queen-white-d1 King-white-e1-0 King-black-e8-0", "d1", 17),
            arrayOf("white 0 Rook-black-a1-1 Queen-white-d1 King-white-e1-0  King-black-e8-0", "d1", 3),
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0", "b4", 1)
    )

    @Test(dataProvider = "getTestIsSelectableData")
    fun testIsSelectable(des: String, figurePos: String, expectedIsSelectable: Boolean) {
        val game = SimpleArrayBoard(des)
        val queen = game.getFigure(Position.byCode(figurePos))
        assertEquals(expectedIsSelectable, queen.isSelectable(game), "isSelectable")
    }

    @DataProvider
    fun getTestIsSelectableData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0", "b4", true),
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b5 King-black-f8-1", "b5", false)
    )
}

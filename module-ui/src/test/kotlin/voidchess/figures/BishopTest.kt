package voidchess.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.ArrayChessBoard
import voidchess.common.board.move.Position

import voidchess.board.getFigure
import voidchess.figures.Bishop
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class BishopTest {
    @Test
    fun testIsReachable() {
        val des = "white 0 Pawn-white-c2-false Bishop-white-d3- " + "Knight-black-b5 King-white-e1-0 King-black-e8-0"
        val game = ChessGame(des)

        val from = Position.byCode("d3")
        val to1 = Position.byCode("b5")
        val to2 = Position.byCode("h7")
        val to3 = Position.byCode("f1")
        val to4 = Position.byCode("c2")
        val to5 = Position.byCode("a6")
        val to6 = Position.byCode("b1")
        val to7 = Position.byCode("d4")

        val bishop = Bishop(true, from)
        assertTrue(bishop.isReachable(to1, game))
        assertTrue(bishop.isReachable(to2, game))
        assertTrue(bishop.isReachable(to3, game))
        assertFalse(bishop.isReachable(to4, game))
        assertFalse(bishop.isReachable(to5, game))
        assertFalse(bishop.isReachable(to6, game))
        assertFalse(bishop.isReachable(to7, game))
        assertFalse(bishop.isReachable(from, game))
    }

    @Test(dataProvider = "getTestIsSelectableData")
    fun testIsSelectable(des: String, figurePos: String, expectedIsSelectable: Boolean) {
        val game = ArrayChessBoard(des)

        val bishop = game.getFigure(Position.byCode(figurePos))
        val actualIsSelectable = bishop.isSelectable(game)
        assertEquals(expectedIsSelectable, actualIsSelectable, "isSelectable")
    }

    @DataProvider
    fun getTestIsSelectableData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Bishop-white-c1 King-white-e1-0 Pawn-white-b2-false Pawn-white-d2-false King-black-e8-0", "c1", false),
            arrayOf("white 0 Bishop-white-c1 King-white-e1-0 King-black-e8-0", "c1", true),
            arrayOf("white 0 Queen-black-a1 Bishop-white-c1 King-white-e1-0 King-black-e8-0", "c1", false),
            arrayOf("white 0 Queen-black-h1 Bishop-white-c1 King-white-e1-0 King-black-e8-0", "c1", false),
            arrayOf("white 0 Queen-black-a5 Bishop-white-d2 King-white-e1-0 King-black-e8-0", "d2", true)
    )
}

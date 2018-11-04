package voidchess.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.ArrayChessBoard
import voidchess.board.move.Move
import voidchess.board.move.Position

import java.util.LinkedList

import voidchess.board.getFigure
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

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(des: String, bishopPosCode: String, expectedNumberOfMoves: Int) {
        val game = ArrayChessBoard(des)

        val bishop = game.getFigure(Position.byCode(bishopPosCode))
        val moveIter = LinkedList<Move>()
        bishop.getPossibleMoves(game, moveIter)
        assertEquals(expectedNumberOfMoves, moveIter.size)
    }

    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e1-0 Bishop-white-b2 King-black-e8-0", "b2", 9),
            arrayOf("white 0 King-white-b3-3 Bishop-white-d5 Knight-black-f7 King-black-e8-0", "d5", 10),
            arrayOf("white 0 King-white-f1-1 Bishop-white-c4 Bishop-black-b5 Knight-black-d5 King-black-e8-0", "c4", 3)
    )

    @Test
    fun testGetReachableMoves() {
        val des = "white 0 Knight-white-e1 Rook-black-b2-2 Bishop-white-d2 King-white-e2-1 " + "Pawn-white-a5-false Knight-black-g5 King-black-e8-0"
        val game = ArrayChessBoard(des)

        val bishop = game.getFigure(Position.byCode("d2"))
        val moveIter = LinkedList<Move>()
        bishop.getReachableMoves(game, moveIter)
        assertEquals(6, moveIter.size)
    }

    @Test
    fun testCountReachableMoves() {
        val des = "white 0 Knight-white-e1 Rook-black-b2-2 Bishop-white-d2 King-white-e2-1 " + "Pawn-white-a5-false Knight-black-g5 King-black-e8-0"
        val game = ArrayChessBoard(des)

        val bishop = game.getFigure(Position.byCode("d2"))
        assertEquals(6, bishop.countReachableMoves(game))
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

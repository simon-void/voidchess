package voidchess.figures

import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.move.Move
import voidchess.board.move.Position

import java.util.LinkedList

import org.testng.annotations.DataProvider
import voidchess.board.getFigure
import voidchess.initSimpleChessBoard
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PawnTest {
    @Test
    fun testCanBeHitByEnpasent() {
        val move = Move[Position.byCode("e2"), Position.byCode("e4")]
        val otherMove = Move[Position.byCode("d2"), Position.byCode("d4")]


        val pawn = Pawn(true, move.from)
        assertFalse(pawn.canBeHitByEnpasent())
        pawn.figureMoved(move)
        assertTrue(pawn.canBeHitByEnpasent())
        pawn.figureMoved(otherMove)
        assertFalse(pawn.canBeHitByEnpasent())
    }

    @Test
    fun testIsReachable() {
        var des = "white 0 Pawn-white-a2-false Pawn-white-b3-false King-white-e1-0 King-black-e8-0"
        var game = ChessGame(des)

        var from = Position.byCode("a2")
        var to1 = Position.byCode("a3")
        var to2 = Position.byCode("a4")
        var to3 = Position.byCode("b3")
        var to4 = Position.byCode("a1")

        var pawn = game.getFigure(from)
        assertTrue(pawn.isReachable(to1, game))
        assertTrue(pawn.isReachable(to2, game))
        assertFalse(pawn.isReachable(to3, game))
        assertFalse(pawn.isReachable(to4, game))
        assertFalse(pawn.isReachable(from, game))


        des = "black 0 Pawn-white-e4-true Pawn-white-c3-false Pawn-black-d4-false King-white-e1-0 King-black-e8-0"
        game = ChessGame(des)

        from = Position.byCode("d4")
        to1 = Position.byCode("d3")
        to2 = Position.byCode("e3")
        to3 = Position.byCode("c3")
        to4 = Position.byCode("d2")

        pawn = game.getFigure(from)
        assertTrue(pawn.isReachable(to1, game))
        assertTrue(pawn.isReachable(to2, game))
        assertTrue(pawn.isReachable(to3, game))
        assertFalse(pawn.isReachable(to4, game))
    }

    @Test(dataProvider = "getGetPossibleMovesData")
    fun testGetPossibleMoves(gameDes: String, pawnPosCode: String, expectedPossibleToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)

        val pawn = game.getFigure(Position.byCode(pawnPosCode))
        val moveIter1 = LinkedList<Move>()
        pawn.getPossibleMoves(game, moveIter1)
        val actualPossibleToCodes = moveIter1.asSequence().map { it.to.toString() }.toSet()
        assertEquals(expectedPossibleToCodes, actualPossibleToCodes, "possible positions the pawn can move to")
    }

    @DataProvider
    fun getGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("black 0 Pawn-white-a4-true Pawn-black-b4-false King-black-e8-0 King-white-e1-0", "b4", setOf("a3", "b3")),
            arrayOf("black 0 King-black-e8-0 Pawn-black-h7-false King-white-e1-0", "h7", setOf("h6", "h5")),
            arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h4 King-white-e1-0", "h7", setOf("h6", "h5")),
            arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h5 King-white-e1-0", "h7", setOf("h6")),
            arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h6 King-white-e1-0", "h7", setOf<String>()),
            arrayOf("black 0 King-black-h8-2 Pawn-black-g7-false Queen-white-e5 King-white-e1-0", "g7", setOf<String>()),
            arrayOf("black 0 King-black-h8-2 Pawn-black-g7-false Queen-white-f6 King-white-e1-0", "g7", setOf("f6")),
            arrayOf("black 0 King-black-h7-2 Pawn-black-g7-false Queen-white-f5 King-white-e1-0", "g7", setOf("g6")),
            arrayOf("black 0 King-black-h6-2 Pawn-black-g7-false Queen-white-f4 King-white-e1-0", "g7", setOf("g5")),
            arrayOf("black 0 King-black-h7-2 Pawn-black-g7-false Queen-white-a7 King-white-e1-0", "g7", setOf<String>()),
            arrayOf("white 0 King-black-h7-2 Pawn-white-a4-false Pawn-black-b5-true King-white-c4-8", "a4", setOf("b5")),
            arrayOf("white 0 King-black-h7-2 Pawn-white-a5-false Pawn-black-b5-true King-white-c4-8", "a5", setOf("b6")),
            arrayOf("white 0 Pawn-white-b2-false King-white-d3-2 Rook-black-a3-1 King-black-e8-0", "b2", setOf("a3", "b3")),
            arrayOf("black 0 King-black-g7-2 Pawn-black-e4-false Queen-white-d4 King-white-e1-0", "e4", setOf<String>())
    )
}

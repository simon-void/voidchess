package voidchess.figures

import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position

import org.testng.annotations.DataProvider
import voidchess.board.getFigure
import voidchess.figures.Pawn
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PawnTest {
    @Test
    fun testCanBeHitByEnpasent() {
        val move = Move[Position.byCode("e2"), Position.byCode("e4")]
        val otherMove = Move[Position.byCode("d2"), Position.byCode("d4")]


        val pawn = Pawn(true, move.from)
        assertFalse(pawn.canBeHitEnPassant())
        pawn.figureMoved(move)
        assertTrue(pawn.canBeHitEnPassant())
        pawn.figureMoved(otherMove)
        assertFalse(pawn.canBeHitEnPassant())
    }

    @Test(dataProvider = "getIsReachableData")
    fun testIsReachable(des: String, fromPosCode: String, reachablePosCodes: Set<String>, unreachablePosCodes: Set<String>) {
        val game = ChessGame(des)
        val from = Position.byCode(fromPosCode)
        val pawn = game.getFigure(from)
        val shouldBeReachableButAreNot = reachablePosCodes.map { Position.byCode(it) }.filter { pos -> !pawn.isReachable(pos, game)}.map { it.toString() }
        val shouldNotBeReachableButAre = unreachablePosCodes.map { Position.byCode(it) }.filter { pos -> pawn.isReachable(pos, game)}.map { it.toString() }
        assertTrue(
                shouldBeReachableButAreNot.isEmpty()&& shouldNotBeReachableButAre.isEmpty(),
                "shouldBeReachableButAreNot: $shouldBeReachableButAreNot, shouldNotBeReachableButAre$shouldNotBeReachableButAre"
        )
    }

    @DataProvider
    fun getIsReachableData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Pawn-white-a2-false Pawn-white-b3-false King-white-e1-0 King-black-e8-0", "a2", setOf("a3", "a4"), setOf("a5", "b3", "b2", "a1", "a2")),
            arrayOf("black 0 Pawn-white-e4-true Pawn-white-c3-false Pawn-black-d4-false King-white-e1-0 King-black-e8-0", "d4", setOf("c3", "d3", "e3"), setOf("d2", "d4", "d5", "d6"))
    )
}

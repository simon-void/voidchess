package voidchess.figures

import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.common.board.move.Position
import voidchess.figures.Knight
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class KnightTest {
    @Test
    fun testIsReachable() {
        val des = "black 0 Pawn-white-c2-false Knight-black-d4 " + "Knight-black-f5 King-white-e1-0 King-black-e8-0"
        val game = ChessGame(des)

        val from = Position.byCode("d4")
        val to1 = Position.byCode("b3")
        val to2 = Position.byCode("b5")
        val to3 = Position.byCode("c6")
        val to4 = Position.byCode("e6")
        val to5 = Position.byCode("c2")
        val to6 = Position.byCode("f3")
        val to7 = Position.byCode("d1")
        val to8 = Position.byCode("f5")

        val knight = Knight(false, from)
        assertTrue(knight.isReachable(to1, game))
        assertTrue(knight.isReachable(to2, game))
        assertTrue(knight.isReachable(to3, game))
        assertTrue(knight.isReachable(to4, game))
        assertTrue(knight.isReachable(to5, game))
        assertTrue(knight.isReachable(to6, game))
        assertFalse(knight.isReachable(to7, game))
        assertFalse(knight.isReachable(to8, game))
        assertFalse(knight.isReachable(from, game))
    }
}

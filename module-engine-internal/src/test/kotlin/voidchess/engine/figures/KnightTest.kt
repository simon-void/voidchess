package voidchess.engine.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.board.ChessGame
import voidchess.engine.board.ArrayChessBoard
import voidchess.engine.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.initChessBoard
import voidchess.toManualConfig
import voidchess.toTargetPosAsStringSet
import java.util.*

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class KnightTest {
    @Test
    fun testIsReachable() {
        val des = "black 0 Pawn-white-c2-false Knight-black-d4 " + "Knight-black-f5 King-white-e1-0 King-black-e8-0"
        val game = ChessGame(des.toManualConfig())

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

    @Test
    fun testGetPossibleMoves() {
        val des = "white 0 Knight-white-a1 King-white-e1-0 Knight-white-g6 King-black-e8-0"
        val game = ArrayChessBoard(des.toManualConfig())

        val knight1 = game.getFigure(Position.byCode("a1"))
        val moveIter1 = LinkedList<Move>()
        knight1.getPossibleMoves(game, moveIter1)
        assertEquals(2, moveIter1.size)
        val knight2 = game.getFigure(Position.byCode("g6"))
        val moveIter2 = LinkedList<Move>()
        knight2.getPossibleMoves(game, moveIter2)
        assertEquals(6, moveIter2.size)
    }

    @Test(dataProvider = "getGetCriticalMovesData")
    fun testGetCriticalMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val knight = game.getFigure(Position.byCode(posCode))
        val actualIrreversibleResults = TreeSet<Move>()
        knight.getCriticalMoves(game, actualIrreversibleResults)
        assertEquals(expectedMoveToCodes, actualIrreversibleResults.toTargetPosAsStringSet(), "knight can move irreversibly to")

        val actualTakingResults = TreeSet<Move>()
        knight.getCriticalMoves(game, actualTakingResults)
        assertEquals(expectedMoveToCodes, actualTakingResults.toTargetPosAsStringSet(), "knight can take on")
    }

    @DataProvider
    fun getGetCriticalMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Knight-white-d3 Queen-black-e5", "d3", setOf("e5")),
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Knight-white-d3 Queen-black-e5 Bishop-black-d2", "d3", setOf<String>()),
            arrayOf("white 0 King-black-e8-0 King-white-e2-1 Knight-white-d3 Queen-black-e5 Bishop-black-c4", "d3", setOf<String>()),
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Knight-white-d3 Bishop-black-e5 Queen-black-c5", "d3", setOf("e5", "c5"))
    )
}

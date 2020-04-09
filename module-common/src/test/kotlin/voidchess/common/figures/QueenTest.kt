package voidchess.common.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.ArrayChessBoard
import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.initChessBoard
import voidchess.common.toFromPosAsStringSet
import voidchess.common.toManualConfig
import voidchess.common.toTargetPosAsStringSet
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class QueenTest {
    @Test
    fun testIsReachable() {
        val des = "white 0 Queen-white-e2 King-white-e1-0 King-black-e8-0"
        val game = initChessBoard(des)

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
        val game = ArrayChessBoard(des.toManualConfig())

        val queen = game.getFigure(Position.byCode(queenPosCode))
        val moveIter = LinkedList<Move>()
        queen.getPossibleMoves(game, moveIter)
        assertEquals(expectedNumberOfMoves, moveIter.size)
    }

    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Queen-white-d1 King-white-e1-0 King-black-e8-0", "d1", 17),
            arrayOf("white 0 Rook-black-a1-1 Queen-white-d1 King-white-e1-0 King-black-e8-0", "d1", 3),
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0", "b4", 1)
    )

    @Test(dataProvider = "getGetReachableCheckingMovesData")
    fun testGetReachableCheckingMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val queen = game.getFigure(Position.byCode(posCode))
        val actualReachableCheckingResults = TreeSet<Move>()
        queen.getReachableCheckingMoves(game, actualReachableCheckingResults)
        assertEquals(expectedMoveToCodes, actualReachableCheckingResults.toTargetPosAsStringSet(), "queen can critical move to")
        assertEquals(if(expectedMoveToCodes.isEmpty()) emptySet() else setOf(posCode), actualReachableCheckingResults.toFromPosAsStringSet(), "queen's from position should be unique")
    }

    @DataProvider
    fun getGetReachableCheckingMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-black-e4-0 King-white-h1-0 Queen-white-b5", "b5", setOf("d5", "e5", "f5", "e8", "a4", "b4", "c4", "e2", "b1", "d3", "b7", "c6")),
            arrayOf("white 0 King-black-e4-0 King-white-e2-0 Queen-white-b5 Pawn-black-d5-true", "b5", setOf("d5", "e8", "a4", "b4", "c4", "b1", "d3"))
    )

    @Test(dataProvider = "getGetCriticalMovesData")
    fun testGetCriticalMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val queen = game.getFigure(Position.byCode(posCode))
        val actualIrreversibleResults = TreeSet<Move>()
        queen.getCriticalMoves(game, actualIrreversibleResults)
        assertEquals(expectedMoveToCodes, actualIrreversibleResults.toTargetPosAsStringSet(), "queen can move irreversibly to")

        val actualTakingResults = TreeSet<Move>()
        queen.getCriticalMoves(game, actualTakingResults)
        assertEquals(expectedMoveToCodes, actualTakingResults.toTargetPosAsStringSet(), "queen can take on")
    }

    @DataProvider
    fun getGetCriticalMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Queen-white-e2 Queen-black-e5 Bishop-black-d3", "e2", setOf("e5")),
            arrayOf("white 0 King-black-e8-0 King-white-d1-1 Queen-white-e2 Queen-black-e5 Bishop-black-d3", "e2", setOf("e5", "d3", "h5"))
    )

    @Test(dataProvider = "getTestIsSelectableData")
    fun testIsSelectable(des: String, figurePos: String, expectedIsSelectable: Boolean) {
        val game = ArrayChessBoard(des.toManualConfig())
        val queen = game.getFigure(Position.byCode(figurePos))
        assertEquals(expectedIsSelectable, queen.isSelectable(game), "isSelectable")
    }

    @DataProvider
    fun getTestIsSelectableData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0", "b4", true),
            arrayOf("white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b5 King-black-f8-1", "b5", false)
    )
}

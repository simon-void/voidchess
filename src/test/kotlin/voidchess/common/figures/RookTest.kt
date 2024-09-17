package voidchess.common.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.*
import voidchess.common.board.ArrayChessBoard
import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class RookTest {
    @Test
    fun testIsReachable() {
        val des = "white 0 Pawn-white-c2-false Rook-white-c4-2 " + "Knight-black-g4 King-white-e1-0 King-black-e8-0"
        val game = initChessBoard(des)

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
        val rook = Rook(false, Position.byCode("a1"))

        assertTrue(rook.canCastle())
        rook.figureMoved(Move.byCode("a1-b1"))
        assertFalse(rook.canCastle())
        rook.undoMove(Position.byCode("a1"))
        assertTrue(rook.canCastle())
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(des: String, rookPosCode: String, expectedNumberOfMoves: Int) {
        val game = ArrayChessBoard(des.toManualConfig())

        val rook = game.getFigure(Position.byCode(rookPosCode))
        val moveIter = rook.getPossibleMoves(game)
        assertEquals(expectedNumberOfMoves, moveIter.size)
    }

    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf("white 0 King-white-e2-0 Pawn-black-b4-false Rook-white-e4-2 King-black-e8-0", "e4", 11)
    )

    @Test(dataProvider = "getGetReachableCheckingMovesData")
    fun testGetReachableCheckingMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val rook = game.getFigure(Position.byCode(posCode))
        val actualReachableCheckingResults = rook.getReachableCheckingMoves(game)
        assertEquals(
            expectedMoveToCodes,
            actualReachableCheckingResults.toTargetPosAsStringSet(),
            "rook can critical move to"
        )
        assertEquals(
            if (expectedMoveToCodes.isEmpty()) emptySet() else setOf(posCode),
            actualReachableCheckingResults.toFromPosAsStringSet(),
            "rook's from position should be unique"
        )
    }

    @DataProvider
    fun getGetReachableCheckingMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf("white 0 King-black-e4-0 King-white-h1-0 Rook-white-b7-3 Pawn-black-e7-false", "b7", setOf("e7", "b4")),
        arrayOf(
            "white 0 King-black-e4-0 King-white-h1-0 Rook-white-b7-3 Pawn-black-d7-false Pawn-black-c4-true",
            "b7",
            emptySet<String>()
        )
    )

    @Test(dataProvider = "getGetCriticalMovesData")
    fun testGetCriticalMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initChessBoard(gameDes)
        val rook = game.getFigure(Position.byCode(posCode))
        val actualIrreversibleResults = rook.getCriticalMoves(game)
        assertEquals(
            expectedMoveToCodes,
            actualIrreversibleResults.toTargetPosAsStringSet(),
            "rook can move irreversibly to"
        )

        val actualTakingResults = rook.getCriticalMoves(game)
        assertEquals(expectedMoveToCodes, actualTakingResults.toTargetPosAsStringSet(), "rook can take on")
    }

    @DataProvider
    fun getGetCriticalMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-e3-5 Queen-black-e5", "e3", setOf("e5")),
        arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-e3-5 Queen-black-c3", "e3", setOf("c3")),
        arrayOf(
            "white 0 King-black-e8-0 King-white-e1-0 Rook-white-e3-5 Queen-black-e5 Bishop-black-c3",
            "e3",
            setOf<String>()
        ),
        arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-e3-5 Queen-black-a5", "e3", setOf<String>())
    )
}

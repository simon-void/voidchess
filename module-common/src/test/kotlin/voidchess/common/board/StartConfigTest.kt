package voidchess.common.board

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.move.Position
import kotlin.test.*

class StartConfigTest {
    @Test
    fun testClassicConfig() {
        StartConfig.ClassicConfig.assertValid()
    }

    @Test
    fun testValidChess960Config() {
        StartConfig.Chess960Config(0).assertValid()
        StartConfig.Chess960Config(518).assertValid()
        StartConfig.Chess960Config(959).assertValid()
    }

    @Test
    fun testInvalidChess960Config() {
        for(illegalChess960Index in listOf(-1, 960)) {
            try {
                StartConfig.Chess960Config(illegalChess960Index)
                fail("instanciation should have failed because of an out-of-bounds chess960 index: $illegalChess960Index")
            } catch (e: IllegalArgumentException) {}
        }
    }

    @Test(dataProvider = "getManualConfigData")
    fun testManualConfig(
        doesWhitePlayerStart: Boolean,
        numberOfMovesWithoutHit: Int,
        figureStates: List<String>,
        expectedHasHitFigureInPreviousMove: Boolean,
        expectedPawnDoubleJumpToPosInPreviousMove: Position?
    ) {
        StartConfig.ManualConfig(
            doesWhitePlayerStart,
            numberOfMovesWithoutHit,
            figureStates
        ).assertValid(
            doesWhitePlayerStart = doesWhitePlayerStart,
            numberOfMovesWithoutHit = numberOfMovesWithoutHit,
            figureCount = figureStates.size,
            hasHitFigureInPreviousMove = expectedHasHitFigureInPreviousMove,
            pawnDoubleJumpToPosInPreviousMove = expectedPawnDoubleJumpToPosInPreviousMove
        )
    }

    @DataProvider
    fun getManualConfigData(): Array<Array<Any?>> = arrayOf(
        arrayOf<Any?>(true, 0, "Knight-white-b1 King-white-e1-0 Queen-black-d8 King-black-e8-0".split(" "), true, null),
        arrayOf<Any?>(false, 0, "Knight-white-b1 King-white-e1-0 Queen-black-d8 King-black-e8-0".split(" "), true, null),
        arrayOf<Any?>(true, 0, classicFigureStates, false, null),
        arrayOf<Any?>(false, 0, classicFigureStates, false, null),
        arrayOf<Any?>(true, 1, "Knight-white-b1 King-white-e1-0 Queen-black-d8 King-black-e8-0".split(" "), false, null),
        arrayOf<Any?>(false, 1, "Knight-white-b1 King-white-e1-0 Queen-black-d8 King-black-e8-0".split(" "), false, null),
        arrayOf<Any?>(true, 1, "Knight-white-b1 King-white-e1-0 Pawn-black-d5-true King-black-e8-0".split(" "), false, Position.byCode("d5"))
    )

    private fun StartConfig.assertValid(
        doesWhitePlayerStart: Boolean = true,
        numberOfMovesWithoutHit: Int = 0,
        figureCount: Int = 32,
        hasHitFigureInPreviousMove: Boolean = false,
        pawnDoubleJumpToPosInPreviousMove: Position? = null
    ) {
        assertEquals(doesWhitePlayerStart, this.doesWhitePlayerStart, "doesWhitePlayerStart")
        assertEquals(numberOfMovesWithoutHit, this.numberOfMovesWithoutHit, "numberOfMovesWithoutHit")
        assertEquals(figureCount, this.figureCount, "figureCount")
        assertEquals(hasHitFigureInPreviousMove, this.hasHitFigureInPreviousMove, "hasHitFigureInPreviousMove")
        assertEquals(
            pawnDoubleJumpToPosInPreviousMove != null,
            this.pawnDoubleJumpToPosInPreviousMove != null,
            "can pawn be taken enpassant"
        )
        pawnDoubleJumpToPosInPreviousMove?.let {
            assertEquals(
                it,
                this.pawnDoubleJumpToPosInPreviousMove,
                "position of pawn that can be taken enpassant"
            )
        }
    }

    companion object {
        val classicFigureStates: List<String> = (
                "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 " +
                "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 " +
                "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false " +
                "Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false " +
                "Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false " +
                "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 Queen-black-d8 " +
                "King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0"
                ).split(" ").filter { it.isNotEmpty() }
    }
}
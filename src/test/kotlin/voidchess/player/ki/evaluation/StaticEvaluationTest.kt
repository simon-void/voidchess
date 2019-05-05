package voidchess.player.ki.evaluation

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGame

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotEquals


class StaticEvaluationTest {

    @Test(dataProvider = "symmetricPositionProvider")
    fun testSymmetricPositionLeadsToEqualEvaluation(game: ChessGame) {
        val staticEval = StaticEvaluation
        val evalForWhite = staticEval.getPrimaryEvaluation(game, true)
        val evalForBlack = staticEval.getPrimaryEvaluation(game, false)
        assertEquals(evalForWhite.primaryEvaluation, evalForBlack.primaryEvaluation)
        assertEquals(evalForWhite.primaryEvaluation, 0.0)

        staticEval.addSecondaryEvaluation(game, true, evalForWhite)
        staticEval.addSecondaryEvaluation(game, false, evalForBlack)
        assertEquals(evalForWhite.getCombinedEvaluation(), evalForBlack.getCombinedEvaluation(), OK_DELTA)
        assertEquals(evalForWhite.getCombinedEvaluation(), 0.0, OK_DELTA)
    }

    @DataProvider
    fun symmetricPositionProvider(): Array<Array<Any>> {
        return arrayOf(arrayOf<Any>(ChessGame("black 0 King-white-e1-8 King-black-e8-8")), arrayOf<Any>(ChessGame("black 0 Rook-white-a1-0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Knight-white-a1 King-white-e1-0 Knight-black-a8 King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Bishop-white-a1 King-white-e1-0 Bishop-black-a8 King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Queen-white-a1 King-white-e1-0 Queen-black-a8 King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Pawn-white-a2-false King-white-e1-0 Pawn-black-a7-false King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Pawn-white-e2-false King-white-e1-0 Pawn-black-e7-false King-black-e8-0")), arrayOf<Any>(ChessGame("black 0 Pawn-white-e2-false Pawn-white-f3-false King-white-e1-0 Pawn-black-e7-false Pawn-black-f6-false King-black-e8-0")), arrayOf<Any>(ChessGame("white 0 "
                + "King-white-e1-0 "
                + "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-white-g2-false Pawn-white-h2-false "
                + "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Pawn-black-g7-false Pawn-black-h7-false "
                + "King-black-e8-0")), arrayOf<Any>(ChessGame("white 0 "
                + "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 "
                + "Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 "
                + "Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")), arrayOf<Any>(ChessGame(518)), arrayOf<Any>(ChessGame(333)), arrayOf<Any>(ChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1", "e8-h8", "c2-c3", "c7-c6")))
    }

    @Test(dataProvider = "asymmetricPositionProvider")
    fun testAsymmetricPositionLeadsToInverseEvaluation(game: ChessGame) {
        val staticEval = StaticEvaluation
        val evalForWhite = staticEval.getPrimaryEvaluation(game, true)
        val evalForBlack = staticEval.getPrimaryEvaluation(game, false)
        val primaryEvalForWhite = evalForWhite.primaryEvaluation
        val primaryEvalForBlack = evalForBlack.primaryEvaluation
        if (primaryEvalForWhite != 0.0 || primaryEvalForBlack != 0.0)
            assertEquals(primaryEvalForWhite, -primaryEvalForBlack)

        staticEval.addSecondaryEvaluation(game, true, evalForWhite)
        staticEval.addSecondaryEvaluation(game, false, evalForBlack)
        val combinedEvalForWhite = evalForWhite.getCombinedEvaluation()
        val combinedEvalForBlack = evalForBlack.getCombinedEvaluation()
        assertEquals(combinedEvalForWhite, -combinedEvalForBlack, OK_DELTA)
        // it's highly unlikely that an asymmetric position is considered equal
        assertNotEquals(combinedEvalForWhite, 0.0, OK_DELTA)
    }

    @DataProvider
    fun asymmetricPositionProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf<Any>(ChessGame("black 0 Rook-white-a1-0 King-white-e1-0 King-black-e8-0")),
                arrayOf<Any>(ChessGame(518, "a2-a3")),
                arrayOf<Any>(ChessGame(333, "d2-d4", "a8-b6", "e2-e4", "h8-g6")),
                arrayOf<Any>(ChessGame(518, "e2-e4", "d7-d5", "e4-d5")),
                arrayOf<Any>(ChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1")))
    }

    companion object {
        private const val OK_DELTA = 0.00000000001
    }
}

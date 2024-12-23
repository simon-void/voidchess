package voidchess.engine.evaluation

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.engine.Ongoing
import voidchess.engine.inner.board.EngineChessGameImpl
import voidchess.copyGameWithInvertedColors
import voidchess.engine.inner.evaluation.leaf.MiddleGameEval
import voidchess.initEngineChessGame
import voidchess.toManualConfig


internal class MiddleGameEvalTest {

    @Test(dataProvider = "symmetricPositionProvider")
    fun testSymmetricPositionLeadsToEqualEvaluation(game: EngineChessGameImpl) {
        val staticEval = MiddleGameEval
        val prelimEvalForWhite = staticEval.getPreliminaryEvaluation(game, true, true)
        val prelimEvalForBlack = staticEval.getPreliminaryEvaluation(game, false, false)
        assertEquals(prelimEvalForWhite, prelimEvalForBlack)
        assertEquals(prelimEvalForWhite, 0.0)

        val evalForWhite = Ongoing(
                prelimEvalForWhite + staticEval.getSecondaryEvaluation(game, true)
        )
        val evalForBlack = Ongoing(
                prelimEvalForBlack + staticEval.getSecondaryEvaluation(game, false)
        )
        assertEquals(evalForWhite.numericValue, evalForBlack.numericValue, OK_DELTA)
        assertEquals(evalForWhite.numericValue, 0.0, OK_DELTA)
    }

    @DataProvider
    fun symmetricPositionProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf<Any>(EngineChessGameImpl("black 0 King-white-e1-8 King-black-e8-8".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Rook-white-a1-0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Knight-white-a1 King-white-e1-0 Knight-black-a8 King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Bishop-white-a1 King-white-e1-0 Bishop-black-a8 King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Queen-white-a1 King-white-e1-0 Queen-black-a8 King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Pawn-white-a2-false King-white-e1-0 Pawn-black-a7-false King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Pawn-white-e2-false King-white-e1-0 Pawn-black-e7-false King-black-e8-0".toManualConfig())),
            arrayOf<Any>(EngineChessGameImpl("black 0 Pawn-white-e2-false Pawn-white-f3-false King-white-e1-0 Pawn-black-e7-false Pawn-black-f6-false King-black-e8-0".toManualConfig())),
            arrayOf<Any>(
                EngineChessGameImpl(("white 0 "
                + "King-white-e1-0 "
                + "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-white-g2-false Pawn-white-h2-false "
                + "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Pawn-black-g7-false Pawn-black-h7-false "
                + "King-black-e8-0").toManualConfig())
            ),
            arrayOf<Any>(
                EngineChessGameImpl(("white 0 "
                + "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 "
                + "Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 "
                + "Bishop-black-f8 Knight-black-g8 Rook-black-h8-0").toManualConfig())
            ),
            arrayOf<Any>(initEngineChessGame(518)),
            arrayOf<Any>(initEngineChessGame(333)),
            arrayOf<Any>(initEngineChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1", "e8-h8", "c2-c3", "c7-c6")))
    }

    @Test(dataProvider = "asymmetricPositionProvider")
    fun testAsymmetricPositionLeadsToInverseEvaluation(game: EngineChessGameImpl) {
        val staticEval = MiddleGameEval
        val prelimEvalForWhite = staticEval.getPreliminaryEvaluation(game, true, true)
        val prelimEvalForBlack = staticEval.getPreliminaryEvaluation(game, false, false)
        if (prelimEvalForWhite != 0.0 || prelimEvalForBlack != 0.0)
            assertEquals(prelimEvalForWhite, -prelimEvalForBlack)

        val evalForWhite = staticEval.addSecondaryEvaluationTo(prelimEvalForWhite, game, true)
        val evalForBlack = staticEval.addSecondaryEvaluationTo(prelimEvalForBlack, game, false)
        assertEquals(evalForWhite.numericValue, -evalForBlack.numericValue, OK_DELTA)
        // it's highly unlikely that an asymmetric position is considered equal
        assertNotEquals(evalForWhite.numericValue, 0.0, OK_DELTA)
    }

    @Test(dataProvider = "asymmetricPositionProvider")
    fun testInvertedAsymmetricPositionLeadsToSameEvaluation(game: EngineChessGameImpl) {
        val staticEval = MiddleGameEval
        val invertedGame = game.copyGameWithInvertedColors()
        val prelimEvalNormal = staticEval.getPreliminaryEvaluation(game, game.isWhiteTurn, game.isWhiteTurn)
        val prelimEvalInverted = staticEval.getPreliminaryEvaluation(invertedGame, invertedGame.isWhiteTurn, game.isWhiteTurn)
        if (prelimEvalNormal != 0.0 || prelimEvalInverted != 0.0)
            assertEquals(prelimEvalNormal, prelimEvalInverted)

        val evalNormal = staticEval.addSecondaryEvaluationTo(prelimEvalNormal, game, game.isWhiteTurn)
        val evalInverted = staticEval.addSecondaryEvaluationTo(prelimEvalInverted, invertedGame, invertedGame.isWhiteTurn)
        assertEquals(evalNormal.numericValue, evalInverted.numericValue, OK_DELTA)
    }

    @DataProvider
    fun asymmetricPositionProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf<Any>(EngineChessGameImpl("black 0 Rook-white-a1-0 King-white-e1-0 King-black-e8-0".toManualConfig())),
                arrayOf<Any>(initEngineChessGame(518, "a2-a3")),
                arrayOf<Any>(initEngineChessGame(333, "d2-d4", "a8-b6", "e2-e4", "h8-g6")),
                arrayOf<Any>(initEngineChessGame(518, "e2-e4", "d7-d5", "e4-d5")),
                arrayOf<Any>(initEngineChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1"))
                )
    }

    companion object {
        private const val OK_DELTA = 0.00000000001
    }
}

package voidchess.engine.concurrent

import kotlinx.coroutines.runBlocking
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.*
import voidchess.engine.inner.board.EngineChessGameImpl
import voidchess.engine.inner.board.EngineChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.engine.*
import voidchess.copyGameWithInvertedColors
import voidchess.engine.inner.evaluation.leaf.MiddleGameEval
import voidchess.engine.inner.concurrent.SingleThreadStrategy
import voidchess.engine.inner.evaluation.*
import voidchess.engine.inner.evaluation.DefaultPruner
import voidchess.engine.inner.evaluation.PrunerWithPawnMoves
import voidchess.engine.inner.evaluation.SearchTreePruner
import voidchess.engine.inner.evaluation.SingleFullMovePruner
import voidchess.engine.inner.evaluation.SingleFullMovePrunerWithPawnMoves
import voidchess.initEngineChessGame
import voidchess.mirrorRow


internal class SingleThreadStrategyTest {
    private val strategy = SingleThreadStrategy

    //best move with matt
    //color inverted matt
    //best move with value
    //best matt move in 2
    @DataProvider(name = "bestMoveProvider")
    fun bestMoveBoardsProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            1,
            EngineChessGameImpl("black 0 King-white-h1-3 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0".toManualConfig()),
            Move.byCode("f4-f1")
        ),
        arrayOf(
            1,
            EngineChessGameImpl("white 0 King-black-h8-3 Pawn-black-g7-false Pawn-black-h7-false Rook-white-f4-2 King-white-e1-0".toManualConfig()),
            Move.byCode("f4-f8")
        ),
        arrayOf(
            1,
            EngineChessGameImpl("white 0 King-white-e1-0 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f2-2 King-black-e8-0".toManualConfig()),
            Move.byCode("e1-f2")
        ),
        arrayOf(
            2,
            EngineChessGameImpl("white 0 Bishop-white-a1 Rook-black-b2-2 Knight-black-c2 Pawn-white-d6-false King-white-g6-8 King-black-h8-4".toManualConfig()),
            Move.byCode("d6-d7")
        ),
        arrayOf(1, initEngineChessGame(518, "d2-d3", "d7-d6", "c1-g5", "e7-e6"), Move.byCode("g5-d8"))
    )

    @DataProvider(name = "getFindBestResultData")
    fun findBestResultData(): Array<Array<Any>> {
        val mattIn1 =
            EngineChessGameImpl("white 0 King-white-e3-2 Rook-white-e4-2 Bishop-white-f6 Rook-black-d8-2 Knight-black-c8 King-black-g8-1 Pawn-black-d7-false Pawn-black-e5-false Pawn-black-f7-false".toManualConfig())
        val stalemateIn1 =
            EngineChessGameImpl("white 0 King-white-a1-9 Rook-white-h2-2 Pawn-white-g2-false Rook-black-b2-5 King-black-g8-1 Pawn-black-b4-false Pawn-black-c3-false Pawn-black-g3-false Pawn-black-f7-false Pawn-black-g7-false".toManualConfig())
        val twoMovesAheadPruner = PrunerWithPawnMoves(2, 2, 2, 2)
        return arrayOf(
            arrayOf(mattIn1, twoMovesAheadPruner, CheckmateOther(1)),
            arrayOf(stalemateIn1, twoMovesAheadPruner, Stalemate)
        )
    }

    @DataProvider(name = "gameWithObviousEvalProvider")
    fun obviousEvalProvider(): Array<Array<Any>> {
        val easyPruner = SingleFullMovePruner( 4, 3)
        return arrayOf(
            arrayOf(
                EngineChessGameImpl("black 0 King-white-e3-4 King-black-g6-6 Pawn-white-a7-false".toManualConfig()),
                easyPruner,
                -12.0..-8.0,
                "pawn promotion"
            )
        )
    }

    @DataProvider(name = "gameWithEvalPredicateProvider")
    fun evalPredicateProvider(): Array<Array<Any>> {
        val level2Pruner = DefaultPruner(2, 4, 3)
        return arrayOf(
            arrayOf(
                EngineChessGameImpl("black 0 Rook-white-b1-1 King-white-g3-4 Pawn-white-h3-false Knight-white-d4 Pawn-white-f4-false King-black-a5-6".toManualConfig()),
                level2Pruner,
                { evaluation: Evaluation -> evaluation is Ongoing && evaluation.numericValue < -6.0 },
                "shouldn't be checkmate"
            )
        )
    }

    @Test
    fun testSortOrder() {
        val game = EngineChessGameImpl(StartConfig.ClassicConfig)
        val moves = evaluate(1, game).iterator()

        var betterMove = moves.next()
        while (moves.hasNext()) {
            val worseMove = moves.next()
            assertTrue(
                betterMove.value >= worseMove.value,
                "earlier move should be at least as good"
            )
            betterMove = worseMove
        }
    }

    @Test(dataProvider = "bestMoveProvider")
    fun testFindBestMoveIn(depth: Int, game: EngineChessGameImpl, bestMove: Move) {
        val computedMove = getBestMoveInIn(depth, game).move
        assertEquals(computedMove, bestMove, "best move")

        val invertedComputedMove = getBestMoveInIn(depth, game.copyGameWithInvertedColors()).move
        assertEquals(invertedComputedMove, bestMove.mirrorRow(), "best move (inverted board)")
    }

    @Test(dataProvider = "getFindBestResultData", dependsOnMethods = ["testFindBestMoveIn"])
    fun testFindBestResultIn(game: EngineChessGameImpl, pruner: SearchTreePruner, expectedResult: Evaluation) {
        val resultSet = evaluate(pruner, game)
        val actualBestMoveResult = resultSet.bestPossibleEval()
        assertEquals(actualBestMoveResult, expectedResult, "expected result")

        val invertedResultSet = evaluate(pruner, game.copyGameWithInvertedColors())
        val actualBestInvertedMoveResult = invertedResultSet.bestPossibleEval()
        assertEquals(actualBestInvertedMoveResult, expectedResult, "expected result")
    }

    @Test(dataProvider = "gameWithObviousEvalProvider", dependsOnMethods = ["testMinMaxIsInvokedForEachPossibleMove"])
    fun testMinMaxEvaluatesToExpectedRange(
        game: EngineChessGameImpl,
        pruner: SearchTreePruner,
        expectedEvalRange: ClosedRange<Double>,
        msg: String
    ) {
        val bestPossibleEval =
            evaluate(pruner, game)
                .bestPossibleEval() as Ongoing

        assertTrue(
            bestPossibleEval.numericValue in expectedEvalRange,
            "eval should be in range $expectedEvalRange but was: ${bestPossibleEval.numericValue}. msg: $msg"
        )

        val bestInvertedPossibleEval = evaluate(pruner, game.copyGameWithInvertedColors()).bestPossibleEval() as Ongoing

        assertTrue(
            bestInvertedPossibleEval.numericValue in expectedEvalRange,
            "eval should be in range $expectedEvalRange but was: ${bestInvertedPossibleEval.numericValue}. msg: $msg"
        )
    }

    @Test(dataProvider = "gameWithEvalPredicateProvider", dependsOnMethods = ["testMinMaxIsInvokedForEachPossibleMove"])
    fun testMinMaxToEvalPredicate(
        game: EngineChessGameImpl,
        pruner: SearchTreePruner,
        evalTest: (Evaluation) -> Boolean,
        msg: String
    ) {
        val bestPossibleEval = evaluate(pruner, game).bestPossibleEval()
        assertTrue(evalTest(bestPossibleEval), "evalTest returned false on eval $bestPossibleEval. msg: $msg")

        val bestInvertedPossibleEval = evaluate(pruner, game.copyGameWithInvertedColors()).bestPossibleEval() as Ongoing
        assertTrue(
            evalTest(bestInvertedPossibleEval),
            "(inverted) evalTest returned false on eval $bestInvertedPossibleEval. msg: $msg"
        )
    }

    @Test
    fun testMinMaxIsInvokedForEachPossibleMove() = runBlocking {
        // with alpha-beta pruning in place suboptimal moves are discarded and no longer returned
        // so only the invocations of the ProgressCallback can be compared
        var progressCallbackInvokedCounter = 0
        val easyPruner = SingleFullMovePrunerWithPawnMoves( 2, 2, 2)
        val minMax = MinMaxEval(easyPruner, MiddleGameEval)
        val startConfig = "white 0 King-white-h1-4 King-black-h7-6 Pawn-white-a7-false".toManualConfig()

        strategy.evaluateMovesBestMoveFirst(
            EngineChessGameImpl(startConfig, emptyList()),
            minMax,
            progressCallback = { _, _ -> progressCallbackInvokedCounter++ }
        )
        val expectedMoves = setOf("h1-g1", "h1-g2", "h1-h2", "a7-a8")

        // minus one because the function is also invoked once at the start to signal the start of the computation
        assertEquals(
            progressCallbackInvokedCounter-1,
            expectedMoves.size,
            "there should be one progressCallback for each possible move"
        )
    }

    private fun getBestMoveInIn(depth: Int, game: EngineChessGame): EvaluatedMove {
        val moves = evaluate(depth, game)
        return moves.first()
    }

    private fun evaluate(depth: Int, game: EngineChessGame): List<EvaluatedMove> {
        val pruner = if(depth==1) {
            SingleFullMovePrunerWithPawnMoves(depth, depth + 1, depth + 1)
        } else {
            PrunerWithPawnMoves(depth, depth, depth + 1, depth + 1)
        }
        return evaluate(pruner, game)
    }

    private fun evaluate(pruner: SearchTreePruner, game: EngineChessGame): List<EvaluatedMove> = runBlocking {
        val dynEval = MinMaxEval(pruner, MiddleGameEval)
        return@runBlocking strategy.evaluateMovesBestMoveFirst(game, dynEval)
    }
}

private fun List<EvaluatedMove>.bestPossibleEval() = first().value

package voidchess.engine.concurrent

import kotlinx.coroutines.runBlocking
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.inner.board.EngineChessGameImpl
import voidchess.common.board.move.Move

import java.text.NumberFormat

import org.testng.Assert.assertEquals
import voidchess.common.board.other.StartConfig
import voidchess.completeMoveHistory
import voidchess.engine.KaiEngine
import voidchess.engine.inner.board.EngineChessGame
import voidchess.engine.inner.evaluation.leaf.MiddleGameEval
import voidchess.engine.inner.evaluation.leaf.StaticEval
import voidchess.engine.inner.concurrent.MultiThreadStrategy
import voidchess.engine.inner.evaluation.MinMaxEval
import voidchess.engine.inner.evaluation.PrunerWithPawnMoves
import voidchess.engine.inner.evaluation.SearchTreePruner
import voidchess.engine.inner.evaluation.SingleFullMovePrunerWithPawnMoves
import voidchess.initEngineChessGame


internal class TerminationTest {
    @DataProvider
    private fun getPlayData(): Array<Array<Any>> = arrayOf(
        arrayOf("black 0 King-white-g1-2 Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false Pawn-white-e4-false Bishop-black-b7 King-black-g8-2",listOf("e3-f2"), defaultPruner),
        arrayOf("white 0 King-white-g1-4 King-black-c5-9 Pawn-white-a6-false", listOf("a6-a7"), PrunerWithPawnMoves(2, 3, 3, 3)),
        arrayOf("518", listOf("c2-c3"), defaultPruner),
        arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false King-black-b7-3-false", emptyList<String>(), defaultPruner),
        arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false Knight-black-a7 King-black-b7-3-false",emptyList<String>(), defaultPruner),
        arrayOf("621", listOf("f2-f3"), defaultPruner),
        arrayOf("black 0 Bishop-white-b1 King-white-h1-3 Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0",emptyList<String>(), defaultPruner),
        arrayOf("white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e5 King-black-g7-3",emptyList<String>(), defaultPruner),
        arrayOf("314", emptyList<Any>(), defaultPruner),
        arrayOf("621", listOf("g2-g3", "f7-f6", "c2-c3", "g8-b3", "d1-c2"), defaultPruner),
        arrayOf("707", listOf("e1-f3", "b7-b6", "f1-e3", "g7-g6"), defaultPruner)
    )

    @Test(dataProvider = "getPlayData")
    fun testPlay(chess960IndexOrDesc: String, moveDescs: List<String>, pruner: SearchTreePruner) {

        val game: EngineChessGame
        val moveArray = moveDescs.toTypedArray()
        game = try {
            val chess960 = chess960IndexOrDesc.toInt()
            initEngineChessGame(chess960, *moveArray)
        } catch (e: NumberFormatException) {
            initEngineChessGame(chess960IndexOrDesc, *moveArray)
        }

        testTermination(game.startConfig, game.completeMoveHistory, pruner)
    }

    @Test
    fun testInvariance() {
        val des = "black 0 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false Pawn-black-b7-false King-black-e8-0 Rook-black-h8-3"

        val game = initEngineChessGame(des, "b7-b5")
        testTermination(game.startConfig, game.completeMoveHistory, defaultPruner)
        val newDes = "white 1 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false Pawn-black-b5-true King-black-e8-0 Rook-black-h8-3"
        assertEquals(game.toString(), newDes)
    }
}

private fun testTermination(
    startConfig: StartConfig,
    movesSoFar: List<Move>,
    pruner: SearchTreePruner,
    staticEvaluation: StaticEval = MiddleGameEval
) = runBlocking {
    val numberFormat = NumberFormat.getPercentInstance()
    val dynamicEvaluation = MinMaxEval(pruner, staticEvaluation)

//    SingleThreadStrategy.evaluateMovesBestMoveFirst(
//        chessGame = EngineChessGameImpl(startConfig, movesSoFar),
//        minMaxEval = dynamicEvaluation,
//        numericEvalOkRadius = KaiEngine.okDistanceToBest,
//        progressCallback = { movesComputed: Int, totalMoves: Int ->
//            println(numberFormat.format(movesComputed.toDouble() / totalMoves.toDouble()))
//        }
//    )
    MultiThreadStrategy.evaluateMovesBestMoveFirst(
        chessGame = EngineChessGameImpl(startConfig, movesSoFar),
        minMaxEval = dynamicEvaluation,
        numericEvalOkRadius = KaiEngine.OK_DISTANCE_TO_BEST,
        progressCallback = { movesComputed: Int, totalMoves: Int ->
            println(numberFormat.format(movesComputed.toDouble() / totalMoves.toDouble()))
        },
        coresToUse = coresToUse
    )
}

private val coresToUse = (Runtime.getRuntime().availableProcessors()-1).coerceAtLeast(2)
private val defaultPruner = SingleFullMovePrunerWithPawnMoves( 1, 2, 2)

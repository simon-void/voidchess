package voidchess.engine

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.board.EngineChessGameImpl
import voidchess.common.board.move.Move
import voidchess.engine.evaluation.*

import java.text.DecimalFormat
import java.text.NumberFormat

import org.testng.Assert.assertEquals
import voidchess.common.board.other.StartConfig
import voidchess.completeMoveHistory
import voidchess.engine.board.EngineChessGame
import voidchess.engine.concurrent.SingleThreadStrategy
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.leaf.StaticEval
import voidchess.initChessGame
import voidchess.toManualConfig
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


internal class TerminationTest {
    @DataProvider
    private fun getPlayData(): Array<Array<Any>> = arrayOf(
                arrayOf("black 0 King-white-g1-2 Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false Pawn-white-e4-false Bishop-black-b7 King-black-g8-2", listOf("e3-f2")),
                arrayOf("518", listOf("c2-c3")), arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false King-black-b7-3-false", emptyList<Any>()),
                arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false Knight-black-a7 King-black-b7-3-false", emptyList<Any>()),
                arrayOf("621", listOf("f2-f3")), arrayOf("black 0 Bishop-white-b1 King-white-h1-3 Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0", emptyList<Any>()),
                arrayOf("white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e5 King-black-g7-3", emptyList<Any>()),
                arrayOf("314", emptyList<Any>()), arrayOf("621", listOf("g2-g3", "f7-f6", "c2-c3", "g8-b3", "d1-c2")),
                arrayOf("707", listOf("e1-f3", "b7-b6", "f1-e3", "g7-g6")))

    @Test(dataProvider = "getPlayData")
    fun testPlay(chess960IndexOrDesc: String, moveDescs: List<String>) {

        val game: EngineChessGame
        val moveArray = moveDescs.toTypedArray()
        game = try {
            val chess960 = chess960IndexOrDesc.toInt()
            initChessGame(chess960, *moveArray)
        } catch (e: NumberFormatException) {
            initChessGame(chess960IndexOrDesc, *moveArray)
        }

        testTermination(game.startConfig, game.completeMoveHistory)
    }

    @Test
    fun testInvariance() {
        val des = "black 0 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b7-false King-black-e8-0 Rook-black-h8-3"

        val game = initChessGame(des, "b7-b5")
        testTermination(game.startConfig, game.completeMoveHistory)
        val newDes = "white 1 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b5-true King-black-e8-0 Rook-black-h8-3"
        assertEquals(game.toString(), newDes)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                args[0].let { param1 ->
                    if(param1.startsWith("benchmark")) {
                        benchmark(param1.substring("benchmark".length).toInt())
                    }
                }
            } else {
                loadTest()
            }
        }
    }
}

private fun benchmark(benchmarkLevel: Int) {
    val pruner: SearchTreePruner
    val movesSoFarHistory: String

    val closedPositionHistory = "e2-e4,e7-e5,g1-f3,b8-c6,f1-b5,f8-c5"
    val openedPositionHistory = closedPositionHistory + "d2-d3,d7-d6,b1-c3,c8-g4"

    when(benchmarkLevel) {
        1 -> {
            pruner = PrunerWithIrreversibleMoves(1, 2, 4, 2)
            movesSoFarHistory = closedPositionHistory
        }
        2 -> {
            pruner = PrunerWithIrreversibleMoves(1, 2, 4, 2)
            movesSoFarHistory = openedPositionHistory
        }
        3 -> {
            pruner = AllMovesOrNonePruner(2, 5, 3)
            movesSoFarHistory = openedPositionHistory
        }
        4 -> {
            pruner = AllMovesOrNonePruner(3, 3, 3)
            movesSoFarHistory = "d2-d3,d7-d6"
        }
        5 -> {
            pruner = AllMovesOrNonePruner(3, 5, 3)
            movesSoFarHistory = closedPositionHistory
        }
        else -> throw IllegalArgumentException("unknown benchmark level: $benchmarkLevel")
    }

    val movesSoFar = movesSoFarHistory.split(",").toTypedArray()

    val game = initChessGame(518, *movesSoFar)
    val staticEvaluation = MiddleGameEval
    loadTest(game, pruner, staticEvaluation, "Benchmark Level $benchmarkLevel")
}

private fun loadTest() {
    // Loadtest
    println("load test: start")

    // Grundaufstellung ohne Bauern
    val des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
            + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
            + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
            + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    loadTest(des)

    // Grundaufstellung mit Bauern vor König und ohne Läufer
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 "
//                    + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false " + "Rook-black-a8-0 Knight-black-b8 "
//                    + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König und ohne Königsläufer
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König und ohne Damen
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Zeit von
    // Grundaufstellung mit Bauern vor König und ohne Königsläufer
    // ist in etwa so groß wie
    // Grundaufstellung mit Bauern vor König und ohne Dame!!! Warum?
    exitProcess(0)
}

private fun loadTest(des: String) {
    val game = EngineChessGameImpl(des.toManualConfig())
    val pruner = PrunerWithIrreversibleMoves(2, 3, 4, 3)
    val staticEvaluation = MiddleGameEval
    loadTest(game, pruner, staticEvaluation, "load test")
}

private fun loadTest(game: EngineChessGame, pruner: SearchTreePruner, staticEvaluation: StaticEval, type: String) {
    val decimalFormat = DecimalFormat("#.0")

    try {
        println("$type (computation started)")
        val durationInMs = measureTimeMillis {
            testTermination(
                game.startConfig,
                game.completeMoveHistory,
                pruner,
                staticEvaluation
            )
        }
        println("duration: ${decimalFormat.format(durationInMs / 1000.0)}s")
    } catch (e: RuntimeException) {
        println("$type threw exception $e")
    } finally {
        println("$type (computation finished)")
    }
}

private fun testTermination(
    startConfig: StartConfig,
    movesSoFar: List<Move>,
    pruner: SearchTreePruner = PrunerWithIrreversibleMoves(1, 1, 2, 2),
    staticEvaluation: StaticEval = MiddleGameEval
) {
    val numberFormat = NumberFormat.getPercentInstance()
    val dynamicEvaluation = MinMaxEval(pruner, staticEvaluation)

    SingleThreadStrategy.evaluateMovesBestMoveFirst(
        chessGame = EngineChessGameImpl(startConfig, movesSoFar),
        minMaxEval = dynamicEvaluation,
        numericEvalOkRadius = KaiEngine.okDistanceToBest,
        progressCallback = { movesComputed: Int, totalMoves: Int ->
            println(numberFormat.format(movesComputed.toDouble() / totalMoves.toDouble()))
        }
    )
}

package voidchess

import kotlinx.coroutines.runBlocking
import voidchess.engine.board.EngineChessGameImpl
import voidchess.common.board.move.Move
import voidchess.engine.evaluation.*

import java.text.DecimalFormat
import java.text.NumberFormat

import voidchess.common.board.other.StartConfig
import voidchess.engine.KaiEngine
import voidchess.engine.board.EngineChessGame
import voidchess.engine.concurrent.SingleThreadStrategy
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.leaf.StaticEval
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


internal class Benchmark {
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
    val openedPositionHistory = "$closedPositionHistory,d2-d3,d7-d6,b1-c3,c8-g4"

    when(benchmarkLevel) {
        1 -> {
            pruner = SingleFullMovePrunerWithPawnMoves( 2, 4, 2)
            movesSoFarHistory = closedPositionHistory
        }
        2 -> {
            pruner = SingleFullMovePrunerWithPawnMoves( 2, 4, 2)
            movesSoFarHistory = openedPositionHistory
        }
        3 -> {
            pruner = DefaultPruner(2, 5, 3)
            movesSoFarHistory = openedPositionHistory
        }
        4 -> {
            pruner = DefaultPruner(3, 3, 3)
            movesSoFarHistory = "d2-d3,d7-d6"
        }
        5 -> {
            pruner = DefaultPruner(3, 5, 3)
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
    val pruner = PrunerWithPawnMoves(2, 3, 4, 3)
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
        pruner: SearchTreePruner = SingleFullMovePrunerWithPawnMoves( 1, 2, 2),
        staticEvaluation: StaticEval = MiddleGameEval
) = runBlocking {
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

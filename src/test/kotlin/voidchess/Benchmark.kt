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
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val level = args.firstOrNull()?.substring("benchmark".length)?.toIntOrNull() ?: 1
    println("benchmark: $level")
    benchmark(level)
}

private fun benchmark(benchmarkLevel: Int) {
    val pruner: SearchTreePruner
    val movesSoFarHistory: String

    val closedPositionHistory = "e2-e4,e7-e5,g1-f3,b8-c6,f1-b5,f8-c5"
    val openedPositionHistory = "$closedPositionHistory,d2-d3,d7-d6,b1-c3,c8-g4"

    when(benchmarkLevel) {
        1 -> {
            pruner = DefaultPruner(3, 4, 4)
            movesSoFarHistory = openedPositionHistory
        }
        2 -> {
            pruner = DefaultPruner(3, 5, 5)
            movesSoFarHistory = openedPositionHistory
        }
        3 -> {
            pruner = DefaultPruner(4, 4, 4)
            movesSoFarHistory = openedPositionHistory
        }
        else -> throw IllegalArgumentException("unknown benchmark level: $benchmarkLevel")
    }

    val movesSoFar = movesSoFarHistory.split(",").toTypedArray()

    val game = initEngineChessGame(518, *movesSoFar)
    val staticEvaluation = MiddleGameEval
    loadTest(game, pruner, staticEvaluation, "Benchmark Level $benchmarkLevel")
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

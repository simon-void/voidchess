package voidchess.engine.concurrent

import kotlinx.coroutines.*
import voidchess.common.board.move.Move
import voidchess.common.engine.*
import voidchess.engine.board.EngineChessGame
import voidchess.engine.concurrent.ConcurrencyStrategy.Companion.getOkEval
import voidchess.engine.evaluation.BestResponseSet
import voidchess.engine.evaluation.MinMaxEval
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


internal class SuspendStrategy(
    numberOfThreads: Int
) : ConcurrencyStrategy() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
    private val dispatcher: CoroutineDispatcher get() = executorService.asCoroutineDispatcher()

    override fun shutdown() {
        executorService.shutdown()
    }

    override fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> = runBlocking(dispatcher) {

        evaluateMovesWithCoroutines(
            game,
            movesToEvaluate,
            progressCallback,
            minMaxEval,
            numericEvalOkRadius
        ).toMutableList()
    }
}

internal suspend fun evaluateMovesWithCoroutines(
    game: EngineChessGame,
    movesToEvaluate: Collection<Move>,
    progressCallback: ProgressCallback,
    minMaxEval: MinMaxEval,
    numericEvalOkRadius: Double
): List<EvaluatedMove> {
    progressCallback(0, movesToEvaluate.size)
    val numberOfMoves = movesToEvaluate.size
    val movesComputedCounter = AtomicInteger(0)
    fun incProgress() = progressCallback(movesComputedCounter.incrementAndGet(), numberOfMoves)
    val gameAndMovePairs = game.copyGame(numberOfMoves).zip(movesToEvaluate)

    val bestResponsesRef = AtomicReference(BestResponseSet())
    val currentMaxEvaluationRef = AtomicReference<Evaluation?>(null)

    return coroutineScope {
        val evaluatedMoveDeferreds: List<Deferred<EvaluatedMove?>> = gameAndMovePairs.map { (gameCopy, move) ->
            async {
                val currentMaxEvaluation = currentMaxEvaluationRef.get()
                val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)

                val (bestResponse, latestEvaluation) = minMaxEval.evaluateMove(
                    gameCopy,
                    move,
                    currentOkEval,
                    bestResponsesRef.get()
                )

                if (bestResponse != null) {
                    bestResponsesRef.updateAndGet { mutableSet ->
                        mutableSet.add(bestResponse)
                        mutableSet
                    }
                }

                if (currentOkEval == null || latestEvaluation > currentOkEval) {
                    currentMaxEvaluationRef.updateAndGet { latestMaxEvaluation: Evaluation? ->
                        if (latestMaxEvaluation == null || latestEvaluation > latestMaxEvaluation) {
                            latestEvaluation
                        } else {
                            latestMaxEvaluation
                        }
                    }
                    EvaluatedMove(move, latestEvaluation)
                } else {
                    null
                }.also {
                    incProgress()
                }
            }
        }

        evaluatedMoveDeferreds.awaitAll().filterNotNull()
    }
}

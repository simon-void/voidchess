package voidchess.engine.concurrent

import kotlinx.coroutines.*
import voidchess.common.board.move.Move
import voidchess.common.engine.EvaluatedMove
import voidchess.common.engine.Evaluation
import voidchess.common.engine.HighestEvalFirst
import voidchess.common.engine.ProgressCallback
import voidchess.engine.board.EngineChessGame
import voidchess.engine.evaluation.BestResponseSet
import voidchess.engine.evaluation.MinMaxEval
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


internal class MultiThreadStrategy(
    numberOfThreads: Int
): ConcurrencyStrategy() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
    private val dispatcher: CoroutineDispatcher get() = executorService.asCoroutineDispatcher()

    fun shutdown() {
        executorService.shutdown()
    }

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    override suspend fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double,
        progressCallback: ProgressCallback
    ): List<EvaluatedMove> {
        require(numericEvalOkRadius>=.0) {"numericEvalOkRadius must be positive, but was $numericEvalOkRadius"}
        val possibleMoves = chessGame.getAllMoves()
        return evaluateMoves(chessGame, possibleMoves, progressCallback, minMaxEval, numericEvalOkRadius).apply { sortWith(
            HighestEvalFirst
        ) }
    }

    private suspend fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> = coroutineScope{
        withContext(dispatcher) {
            evaluateMovesWithCoroutines(
                game,
                movesToEvaluate,
                progressCallback,
                minMaxEval,
                numericEvalOkRadius
            )
        }.toMutableList()
    }

    internal suspend fun evaluateMovesWithCoroutines(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double
    ): List<EvaluatedMove> = coroutineScope{
        progressCallback(0, movesToEvaluate.size)
        val numberOfMoves = movesToEvaluate.size
        val movesComputedCounter = AtomicInteger(0)
        suspend fun incProgress() = progressCallback(movesComputedCounter.incrementAndGet(), numberOfMoves)
        val ensureCoroutineActive = fun () {ensureActive()}
        val gameAndMovePairs = game.copyGame(numberOfMoves).zip(movesToEvaluate)

        val bestResponsesRef = AtomicReference(BestResponseSet())
        val currentMaxEvaluationRef = AtomicReference<Evaluation?>(null)

        return@coroutineScope coroutineScope {
            val evaluatedMoveDeferreds: List<Deferred<EvaluatedMove?>> = gameAndMovePairs.map { (gameCopy, move) ->
                async {
                    ensureActive()
                    val currentMaxEvaluation = currentMaxEvaluationRef.get()
                    val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)

                    val (bestResponse, latestEvaluation) = minMaxEval.evaluateMove(
                        gameCopy,
                        move,
                        currentOkEval,
                        bestResponsesRef.get(),
                        ensureCoroutineActive
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
}

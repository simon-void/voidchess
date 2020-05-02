package voidchess.engine.concurrent

import voidchess.common.board.move.Move
import voidchess.common.engine.ProgressCallback
import voidchess.common.engine.EvaluatedMove
import voidchess.common.engine.Evaluation
import voidchess.engine.board.EngineChessGame
import voidchess.engine.evaluation.BestResponseSet
import voidchess.engine.evaluation.MinMaxEval
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList


internal class MultiThreadStrategy(
    private val numberOfThreads: Int
) : ConcurrencyStrategy() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    override fun shutdown() {
        executorService.shutdown()
    }


    override fun evaluateMoves(
            game: EngineChessGame,
            movesToEvaluate: Collection<Move>,
            progressCallback: ProgressCallback,
            minMaxEval: MinMaxEval,
            numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> {

        val currentMaxEvaluationRef = AtomicReference<Evaluation?>(null)
        val bestResponsesRef = AtomicReference(BestResponseSet())
        val callablesToEvaluate =
            getEvaluableMoves(
                game,
                movesToEvaluate,
                minMaxEval,
                currentMaxEvaluationRef,
                bestResponsesRef,
                numericEvalOkRadius
            )

        val result = try {
            evaluate(callablesToEvaluate, progressCallback)
        } catch (e: Exception) {
            e.printStackTrace()
            LinkedList<EvaluatedMove>()
        }

        assert(result.isNotEmpty()) { "no evaluation of a possible moves was successful" }

        return result
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun evaluate(
            movesToEvaluate: LinkedList<Callable<EvaluatedMove>>,
            progressCallback: ProgressCallback
    ): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = movesToEvaluate.size
        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)

        val ecs = ExecutorCompletionService<EvaluatedMove>(executorService)
        submitCallables(movesToEvaluate, ecs, numberOfThreads)

        for (i in 0 until totalNumberOfMoves) {
            //show the progress
            progressCallback(i, totalNumberOfMoves)
            //wait for an evaluation to be finished
            val evaluatedMove = ecs.take().get()
            //add a new move to be evaluated to the queue (if some are left)
            submitCallables(movesToEvaluate, ecs, 1)
            //add this evaluation to result set
            if (evaluatedMove != null) {
                result.add(evaluatedMove)
            }
        }
        progressCallback(totalNumberOfMoves, totalNumberOfMoves)

        return result
    }

    private fun getEvaluableMoves(
            game: EngineChessGame,
            movesToEvaluate: Collection<Move>,
            minMaxEval: MinMaxEval,
            currentMaxEvaluationRef: AtomicReference<Evaluation?>,
            bestResponsesRef: AtomicReference<BestResponseSet>,
            numericEvalOkRadius: Double
    ): LinkedList<Callable<EvaluatedMove>> {
        assert(movesToEvaluate.isNotEmpty()) { "no moves were possible and therefore evaluable" }

        val totalNumberOfMoves = movesToEvaluate.size

        // TODO instead of generating one game per move, generate one game per thread
        val gameInstances = game.copyGame(totalNumberOfMoves).iterator()

        val callablesToEvaluate = LinkedList<Callable<EvaluatedMove>>()
        for (move in movesToEvaluate) {
            callablesToEvaluate.add(
                MoveEvaluationCallable(
                    gameInstances.next(),
                    move,
                    minMaxEval,
                    currentMaxEvaluationRef,
                    bestResponsesRef,
                    numericEvalOkRadius
                )
            )
        }

        return callablesToEvaluate
    }

    private fun submitCallables(
            movesToEvaluate: LinkedList<Callable<EvaluatedMove>>,
            completionService: CompletionService<EvaluatedMove>,
            numberOfMovesToSubmit: Int
    ) {
        for (i in 0 until numberOfMovesToSubmit) {
            if (movesToEvaluate.isEmpty()) {
                break
            }
            completionService.submit(movesToEvaluate.removeFirst())
        }
    }

    private class MoveEvaluationCallable internal constructor(
            private val game: EngineChessGame,
            private val move: Move,
            private val minMaxEval: MinMaxEval,
            private val currentMaxEvaluationRef: AtomicReference<Evaluation?>,
            private val bestResponsesRef: AtomicReference<BestResponseSet>,
            private val numericEvalOkRadius: Double
    ) : Callable<EvaluatedMove> {

        @Throws(Exception::class)
        override fun call(): EvaluatedMove? {
            return try {
                val currentMaxEvaluation = currentMaxEvaluationRef.get()
                val bestResponses = bestResponsesRef.get()
                val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)

                val (bestResponse, latestEvaluation) = minMaxEval.evaluateMove(
                    game,
                    move,
                    currentOkEval,
                    bestResponses
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
                }
            } catch (e: Exception) {
                //print out the error
                e.printStackTrace()
                null
            }
        }
    }
}

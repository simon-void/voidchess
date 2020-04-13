package voidchess.engine.player.ki.concurrent

import voidchess.common.board.move.Move
import voidchess.common.player.ki.ProgressCallback
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.Evaluation
import voidchess.engine.board.EngineChessGame
import voidchess.engine.player.ki.evaluation.EvaluatingMinMax
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList


internal class MultiThreadStrategy(
    private val numberOfThreads: Int,
    progressCallback: ProgressCallback
) : ConcurrencyStrategy(progressCallback) {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    override fun shutdown() {
        executorService.shutdown()
    }


    override fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        evaluatingMinMax: EvaluatingMinMax,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> {

        val currentMaxEvaluationRef = AtomicReference<Evaluation?>(null)
        val callablesToEvaluate =
            getEvaluableMoves(game, movesToEvaluate, evaluatingMinMax, currentMaxEvaluationRef, numericEvalOkRadius)

        val result = try {
            evaluate(callablesToEvaluate)
        } catch (e: Exception) {
            e.printStackTrace()
            LinkedList<EvaluatedMove>()
        }

        assert(result.isNotEmpty()) { "no evaluation of a possible moves was successful" }

        return result
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun evaluate(movesToEvaluate: LinkedList<Callable<EvaluatedMove>>): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = movesToEvaluate.size
        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)

        val ecs = ExecutorCompletionService<EvaluatedMove>(executorService)
        submitCallables(movesToEvaluate, ecs, numberOfThreads)

        for (i in 0 until totalNumberOfMoves) {
            //wait for an evaluation to be finished
            val evaluatedMove = ecs.take().get()
            //show the progress
            progressCallback(i, totalNumberOfMoves)
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
        evaluatingMinMax: EvaluatingMinMax,
        currentMaxEvaluationRef: AtomicReference<Evaluation?>,
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
                    gameInstances.next(), move, evaluatingMinMax, currentMaxEvaluationRef, numericEvalOkRadius
                )
            )
        }

        return callablesToEvaluate
    }

    private fun submitCallables(movesToEvaluate: LinkedList<Callable<EvaluatedMove>>, completionService: CompletionService<EvaluatedMove>, numberOfMovesToSubmit: Int) {
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
        private val evaluatingMinMax: EvaluatingMinMax,
        private val currentMaxEvaluationRef: AtomicReference<Evaluation?>,
        private val numericEvalOkRadius: Double
    ) : Callable<EvaluatedMove> {

        @Throws(Exception::class)
        override fun call(): EvaluatedMove? {
            return try {
                val currentMaxEvaluation = currentMaxEvaluationRef.get()
                val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)

                val latestEvaluation = evaluatingMinMax.evaluateMove(game, move, currentOkEval)

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

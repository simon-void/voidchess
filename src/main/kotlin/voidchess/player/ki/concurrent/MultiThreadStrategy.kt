package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.board.move.Move
import voidchess.player.ki.evaluation.EvaluatingMinMax
import voidchess.player.ki.evaluation.EvaluatedMove

import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

internal class MultiThreadStrategy(showProgress: (Int, Int)->Unit, private val numberOfThreads: Int) : AbstractConcurrencyStrategy(showProgress) {
    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)


    override fun evaluateMoves(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {
        //as long as the first parameter is 0 and the second one is bigger
        //the progress bar will always show correctly 0% (so '1' as second parameter is fine)
        showProgress(0, 1)

        val movesToEvaluate = getEvaluableMoves(game, evaluatingMinMax)

        var result = try {
            evaluate(movesToEvaluate)
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
            showProgress(i, totalNumberOfMoves)
            //add a new move to be evaluated to the queue (if some are left)
            submitCallables(movesToEvaluate, ecs, 1)
            //add this evaluation to result set
            if (evaluatedMove != null) {
                result.add(evaluatedMove)
            }
        }

        return result
    }

    private fun getEvaluableMoves(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): LinkedList<Callable<EvaluatedMove>> {
        val possibleMoves = game.getAllMoves()
        assert(possibleMoves.isNotEmpty()) { "no moves were possible and therefore evaluable" }

        val totalNumberOfMoves = possibleMoves.size

        val gameInstances = game.copyGame(totalNumberOfMoves).iterator()

        val movesToEvaluate = LinkedList<Callable<EvaluatedMove>>()
        for (move in possibleMoves) {
            movesToEvaluate.add(
                    MoveEvaluationCallable(
                            gameInstances.next(), move, evaluatingMinMax
                    )
            )
        }

        return movesToEvaluate
    }

    private fun submitCallables(movesToEvaluate: LinkedList<Callable<EvaluatedMove>>, completionService: CompletionService<EvaluatedMove>, numberOfMovesToSubmit: Int) {
        for (i in 0 until numberOfMovesToSubmit) {
            if (movesToEvaluate.isEmpty()) {
                break
            }
            completionService.submit(movesToEvaluate.removeFirst())
        }
    }

    private class MoveEvaluationCallable internal constructor(private val game: ChessGameInterface, private val move: Move, private val evaluatingMinMax: EvaluatingMinMax) : Callable<EvaluatedMove> {

        @Throws(Exception::class)
        override fun call(): EvaluatedMove? {
            return try {
                val value = evaluatingMinMax.evaluateMove(game, move)
                EvaluatedMove(move, value)
            } catch (e: Exception) {
                //print out the error
                e.printStackTrace()
                null
            }
        }
    }

}

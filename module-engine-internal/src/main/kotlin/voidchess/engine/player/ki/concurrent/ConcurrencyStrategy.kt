package voidchess.engine.player.ki.concurrent

import voidchess.engine.board.ChessGameInterface
import voidchess.common.board.move.Move
import voidchess.common.player.ki.ProgressCallback
import voidchess.engine.player.ki.evaluation.EvaluatingMinMax
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.HighestEvalFirst
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

internal sealed class ConcurrencyStrategy(protected val progressCallback: ProgressCallback) {

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    fun evaluateMovesBestMoveFirst(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): List<EvaluatedMove> =
        evaluateMoves(game, game.getAllMoves(), evaluatingMinMax).apply { sortWith(HighestEvalFirst) }

    fun evaluateMove(game: ChessGameInterface, move: Move, evaluatingMinMax: EvaluatingMinMax): EvaluatedMove =
        evaluateMoves(game, listOf(move), evaluatingMinMax).single()

    abstract fun shutdown()

    protected abstract fun evaluateMoves(game: ChessGameInterface, moves: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove>
}



internal class SingleThreadStrategy(progressCallback: ProgressCallback) : ConcurrencyStrategy(progressCallback) {

    override fun evaluateMoves(game: ChessGameInterface, moves: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = moves.size
        progressCallback(0, totalNumberOfMoves)

        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)
        for ((moveIndex, move) in moves.withIndex()) {
            val value = evaluatingMinMax.evaluateMove(game, move)
            result.add(EvaluatedMove(move, value))

            progressCallback(moveIndex + 1, totalNumberOfMoves)
        }

        return result
    }

    override fun shutdown() {}
}



internal class MultiThreadStrategy(
    progressCallback: ProgressCallback,
    private val numberOfThreads: Int
) : ConcurrencyStrategy(progressCallback) {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    override fun shutdown() {
        executorService.shutdown()
    }


    override fun evaluateMoves(game: ChessGameInterface, moves: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {

        val movesToEvaluate = getEvaluableMoves(game, moves, evaluatingMinMax)

        val result = try {
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
            progressCallback(i, totalNumberOfMoves)
            //add a new move to be evaluated to the queue (if some are left)
            submitCallables(movesToEvaluate, ecs, 1)
            //add this evaluation to result set
            if (evaluatedMove != null) {
                result.add(evaluatedMove)
            }
        }

        return result
    }

    private fun getEvaluableMoves(game: ChessGameInterface, moves: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): LinkedList<Callable<EvaluatedMove>> {
        assert(moves.isNotEmpty()) { "no moves were possible and therefore evaluable" }

        val totalNumberOfMoves = moves.size

        val gameInstances = game.copyGame(totalNumberOfMoves).iterator()

        val movesToEvaluate = LinkedList<Callable<EvaluatedMove>>()
        for (move in moves) {
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

    private class MoveEvaluationCallable internal constructor(private val game: ChessGameInterface, private val move: Move, private val evaluatingMinMax: EvaluatingMinMax) :
        Callable<EvaluatedMove> {

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
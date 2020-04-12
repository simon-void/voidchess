package voidchess.engine.player.ki.concurrent

import voidchess.engine.board.EngineChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.player.ki.ProgressCallback
import voidchess.engine.player.ki.evaluation.EvaluatingMinMax
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.HighestEvalFirst
import voidchess.engine.board.EngineChessGameImpl
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

internal sealed class ConcurrencyStrategy(protected val progressCallback: ProgressCallback) {

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    fun evaluateMovesBestMoveFirst(startConfig: StartConfig, movesSoFar: List<Move>, evaluatingMinMax: EvaluatingMinMax): List<EvaluatedMove> {
        val chessGame = EngineChessGameImpl(startConfig, movesSoFar)
        val possibleMoves = chessGame.getAllMoves()
        return evaluateMoves(chessGame, possibleMoves, evaluatingMinMax).apply { sortWith(HighestEvalFirst) }
    }

    fun evaluateMove(startConfig: StartConfig, movesSoFar: List<Move>, move: Move, evaluatingMinMax: EvaluatingMinMax): EvaluatedMove {
        val chessGame = EngineChessGameImpl(startConfig, movesSoFar)
        return evaluateMoves(chessGame, listOf(move), evaluatingMinMax).single()
    }

    abstract fun shutdown()

    protected abstract fun evaluateMoves(game: EngineChessGame, movesToEvaluate: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove>
}



internal class SingleThreadStrategy(progressCallback: ProgressCallback) : ConcurrencyStrategy(progressCallback) {

    override fun evaluateMoves(game: EngineChessGame, movesToEvaluate: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = movesToEvaluate.size
        progressCallback(0, totalNumberOfMoves)

        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)
        for ((moveIndex, move) in movesToEvaluate.withIndex()) {
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


    override fun evaluateMoves(game: EngineChessGame, movesToEvaluate: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {

        val callablesToEvaluate = getEvaluableMoves(game, movesToEvaluate, evaluatingMinMax)

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

        return result
    }

    private fun getEvaluableMoves(game: EngineChessGame, movesToEvaluate: Collection<Move>, evaluatingMinMax: EvaluatingMinMax): LinkedList<Callable<EvaluatedMove>> {
        assert(movesToEvaluate.isNotEmpty()) { "no moves were possible and therefore evaluable" }

        val totalNumberOfMoves = movesToEvaluate.size

        // TODO instead of generating one game per move, generate one game per thread
        val gameInstances = game.copyGame(totalNumberOfMoves).iterator()

        val callablesToEvaluate = LinkedList<Callable<EvaluatedMove>>()
        for (move in movesToEvaluate) {
            callablesToEvaluate.add(
                MoveEvaluationCallable(
                    gameInstances.next(), move, evaluatingMinMax
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

    private class MoveEvaluationCallable internal constructor(private val game: EngineChessGame, private val move: Move, private val evaluatingMinMax: EvaluatingMinMax) :
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
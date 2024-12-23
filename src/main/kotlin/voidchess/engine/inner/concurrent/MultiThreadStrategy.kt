package voidchess.engine.inner.concurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import voidchess.common.board.move.Move
import voidchess.common.engine.EvaluatedMove
import voidchess.common.engine.Evaluation
import voidchess.common.engine.HighestEvalFirst
import voidchess.common.engine.ProgressCallback
import voidchess.engine.inner.evaluation.BestResponseSet
import voidchess.engine.inner.evaluation.MinMaxEval
import voidchess.engine.inner.board.EngineChessGame
import java.util.concurrent.atomic.AtomicReference

internal data object MultiThreadStrategy : ConcurrencyStrategy() {

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    suspend fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        coresToUse: Int,
        numericEvalOkRadius: Double = .0,
        progressCallback: ProgressCallback = { _, _ -> }
    ): List<EvaluatedMove> {
        require(numericEvalOkRadius >= .0) { "numericEvalOkRadius must be positive, but was $numericEvalOkRadius" }
        val possibleMoves = chessGame.getAllMoves().apply {
            shuffle()
        }
        return evaluateMoves(
            chessGame,
            possibleMoves,
            progressCallback,
            minMaxEval,
            numericEvalOkRadius,
            coresToUse
        ).apply {
            sortWith(
                HighestEvalFirst
            )
        }
    }

    private suspend fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double,
        coresToUse: Int
    ): MutableList<EvaluatedMove> {
        val numberOfMoves = movesToEvaluate.size
        progressCallback(0, numberOfMoves)

        val bestResponses = BestResponseSet.synced()
        val currentMaxEvaluationRef = AtomicReference<Evaluation?>(null)
        fun getCurrentOkEval(): Evaluation? {
            val currentMaxEvaluation = currentMaxEvaluationRef.get()
            return getOkEval(currentMaxEvaluation, numericEvalOkRadius)
        }

        return coroutineScope {

            val gameAndMovePairs: ReceiveChannel<Pair<EngineChessGame, Move>> =
                game.copyGame(numberOfMoves).zip(movesToEvaluate).toClosedChannel()

            val results = Channel<EvaluateMoveResult>(numberOfMoves)

            repeat(coresToUse) {
                launch(Dispatchers.Default) {
                    for ((gameCopy, move) in gameAndMovePairs) {
                        ensureActive()

                        val (bestResponse: Move?, evaluation: Evaluation) = minMaxEval.evaluateMove(
                            gameCopy,
                            move,
                            getCurrentOkEval(),
                            bestResponses,
                            ::ensureActive
                        )

                        results.send(
                            EvaluateMoveResult(
                                move,
                                bestResponse,
                                evaluation
                            )
                        )
                    }
                }
            }

            val evaluatedMoves = mutableListOf<EvaluatedMove>()

            for (numberOfComputedMoves in 1..numberOfMoves) {
                val (move: Move, bestResponse: Move?, latestEvaluation: Evaluation) = results.receive()
                progressCallback(numberOfComputedMoves, numberOfMoves)

                if (bestResponse != null) {
                    bestResponses.add(bestResponse)
                }

                val currentOkEval = getCurrentOkEval()
                if (currentOkEval == null || latestEvaluation > currentOkEval) {
                    currentMaxEvaluationRef.updateAndGet { latestMaxEvaluation: Evaluation? ->
                        if (latestMaxEvaluation == null || latestEvaluation > latestMaxEvaluation) {
                            latestEvaluation
                        } else {
                            latestMaxEvaluation
                        }
                    }
                    evaluatedMoves.add(EvaluatedMove(move, latestEvaluation))
                }
            }

            results.close()

            return@coroutineScope evaluatedMoves
        }
    }
}

private data class EvaluateMoveResult(
    val movePlayed: Move,
    val bestResponse : Move?,
    val evaluation: Evaluation
)

private suspend fun <T> List<T>.toClosedChannel(): ReceiveChannel<T> {
    val list = this
    return Channel<T>(list.size).apply {
        list.forEach { send(it) }
        close()
    }
}

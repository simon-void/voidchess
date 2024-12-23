package voidchess.engine.inner.concurrent

import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.engine.EvaluatedMove
import voidchess.common.engine.Evaluation
import voidchess.common.engine.HighestEvalFirst
import voidchess.common.engine.ProgressCallback
import voidchess.engine.inner.evaluation.BestResponseSet
import voidchess.engine.inner.evaluation.MinMaxEval
import voidchess.engine.inner.board.EngineChessGame
import voidchess.engine.inner.board.EngineChessGameImpl

internal data object SingleThreadStrategy: ConcurrencyStrategy() {

    suspend fun evaluateMove(
        startConfig: StartConfig,
        movesSoFar: List<Move>,
        move: Move,
        minMaxEval: MinMaxEval,
        progressCallback: ProgressCallback = progressNothing
    ): EvaluatedMove {
        val chessGame = EngineChessGameImpl(startConfig, movesSoFar)
        return evaluateMoves(chessGame, listOf(move), progressCallback, minMaxEval, .0).single()
    }

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    suspend fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double = .0,
        progressCallback: ProgressCallback = { _, _ -> }
    ): List<EvaluatedMove> {
        require(numericEvalOkRadius>=.0) {"numericEvalOkRadius must be positive, but was $numericEvalOkRadius"}
        val possibleMoves = chessGame.getAllMoves()
        return evaluateMoves(
            chessGame,
            possibleMoves,
            progressCallback,
            minMaxEval,
            numericEvalOkRadius
        ).apply {
            sortWith(HighestEvalFirst)
        }
    }

    private suspend fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = movesToEvaluate.size
        progressCallback(0, totalNumberOfMoves)

        var currentMaxEvaluation: Evaluation? = null
        val bestResponses = BestResponseSet.unsynced()
        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)
        for ((moveIndex, move) in movesToEvaluate.withIndex()) {

            val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)
            val (bestResponse, latestEvaluation) = minMaxEval.evaluateMove(
                game,
                move,
                currentOkEval,
                bestResponses
            )

            if (currentOkEval == null || latestEvaluation > currentOkEval) {
                if (currentMaxEvaluation == null || latestEvaluation > currentMaxEvaluation) {
                    currentMaxEvaluation = latestEvaluation
                }
                result.add(EvaluatedMove(move, latestEvaluation))
            }
            bestResponse?.let { bestResponses.add(it) }

            progressCallback(moveIndex + 1, totalNumberOfMoves)
        }

        return result
    }
}

private val progressNothing: ProgressCallback = { _, _ -> }

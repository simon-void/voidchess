package voidchess.engine.concurrent

import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.engine.ProgressCallback
import voidchess.common.engine.EvaluatedMove
import voidchess.common.engine.Evaluation
import voidchess.common.engine.HighestEvalFirst
import voidchess.engine.board.EngineChessGame
import voidchess.engine.board.EngineChessGameImpl
import voidchess.engine.evaluation.BestResponseSet
import voidchess.engine.evaluation.MinMaxEval


private val progressNothing: ProgressCallback = { _, _ -> }

internal object SingleThreadStrategy: ConcurrencyStrategy() {

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
    override suspend fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double,
        progressCallback: ProgressCallback
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
        val bestResponses = BestResponseSet()
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

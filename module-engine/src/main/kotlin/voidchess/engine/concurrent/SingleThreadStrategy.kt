package voidchess.engine.concurrent

import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.player.ki.ProgressCallback
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.Evaluation
import voidchess.engine.board.EngineChessGame
import voidchess.engine.board.EngineChessGameImpl
import voidchess.engine.evaluation.BestResponseSet
import voidchess.engine.evaluation.MinMaxEval


internal object SingleThreadStrategy : ConcurrencyStrategy() {

    fun evaluateMove(
        startConfig: StartConfig,
        movesSoFar: List<Move>,
        move: Move,
        minMaxEval: MinMaxEval,
        progressCallback: ProgressCallback = { _, _ -> }
    ): EvaluatedMove {
        val chessGame = EngineChessGameImpl(startConfig, movesSoFar)
        return evaluateMoves(chessGame, listOf(move), progressCallback, minMaxEval, .0).single()
    }

    override fun evaluateMoves(
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

    override fun shutdown() {}
}

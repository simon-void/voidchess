package voidchess.engine.player.ki.concurrent

import voidchess.common.board.move.Move
import voidchess.common.player.ki.ProgressCallback
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.Evaluation
import voidchess.common.player.ki.evaluation.NumericalEvaluation
import voidchess.common.player.ki.evaluation.Ongoing
import voidchess.engine.board.EngineChessGame
import voidchess.engine.player.ki.evaluation.EvaluatingMinMax


internal class SingleThreadStrategy(progressCallback: ProgressCallback) : ConcurrencyStrategy(progressCallback) {

    override fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        evaluatingMinMax: EvaluatingMinMax,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove> {
        val totalNumberOfMoves = movesToEvaluate.size
        progressCallback(0, totalNumberOfMoves)

        var currentMaxEvaluation: Evaluation? = null
        val result = ArrayList<EvaluatedMove>(totalNumberOfMoves)
        for ((moveIndex, move) in movesToEvaluate.withIndex()) {

            val currentOkEval: Evaluation? = getOkEval(currentMaxEvaluation, numericEvalOkRadius)
            val latestEvaluation = evaluatingMinMax.evaluateMove(game, move, currentOkEval)

            if (currentOkEval == null || latestEvaluation > currentOkEval) {
                if (currentMaxEvaluation == null || latestEvaluation > currentMaxEvaluation) {
                    currentMaxEvaluation = latestEvaluation
                }
                result.add(EvaluatedMove(move, latestEvaluation))
            }

            progressCallback(moveIndex + 1, totalNumberOfMoves)
        }

        return result
    }

    override fun shutdown() {}
}

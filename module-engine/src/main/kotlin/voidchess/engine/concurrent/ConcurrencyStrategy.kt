package voidchess.engine.concurrent

import voidchess.engine.board.EngineChessGame
import voidchess.common.engine.*
import voidchess.engine.evaluation.MinMaxEval

internal abstract class ConcurrencyStrategy {

    abstract suspend fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double = .0,
        progressCallback: ProgressCallback = { _, _ -> }
    ): List<EvaluatedMove>

    // TODO Kotlin 1.4 contract: result==null <=> currentMaxEvaluation==null
    fun getOkEval(
        currentMaxEvaluation: Evaluation?,
        numericEvalOkRadius: Double
    ): Evaluation? = currentMaxEvaluation?.let { currentMax ->
        if (currentMax is NumericalEvaluation) {
            Ongoing(currentMax.numericValue - numericEvalOkRadius)
        } else {
            currentMax
        }
    }
}

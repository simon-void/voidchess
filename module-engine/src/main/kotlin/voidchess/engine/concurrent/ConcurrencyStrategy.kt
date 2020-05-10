package voidchess.engine.concurrent

import voidchess.engine.board.EngineChessGame
import voidchess.common.board.move.Move
import voidchess.common.engine.*
import voidchess.engine.evaluation.MinMaxEval

internal abstract class ConcurrencyStrategy {

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    fun evaluateMovesBestMoveFirst(
        chessGame: EngineChessGame,
        minMaxEval: MinMaxEval,
        numericEvalOkRadius: Double = .0,
        progressCallback: ProgressCallback = { _, _ -> }
    ): List<EvaluatedMove> {
        require(numericEvalOkRadius>=.0) {"numericEvalOkRadius must be positive, but was $numericEvalOkRadius"}
        val possibleMoves = chessGame.getAllMoves()
        return evaluateMoves(chessGame, possibleMoves, progressCallback, minMaxEval, numericEvalOkRadius).apply { sortWith(HighestEvalFirst) }
    }

    open fun shutdown() {}

    protected abstract fun evaluateMoves(
            game: EngineChessGame,
            movesToEvaluate: Collection<Move>,
            progressCallback: ProgressCallback,
            minMaxEval: MinMaxEval,
            numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove>

    companion object {
        // TODO Kotlin 1.4 contract: result==null <=> currentMaxEvaluation==null
        fun getOkEval(
                currentMaxEvaluation: Evaluation?,
                numericEvalOkRadius: Double
        ): Evaluation? = currentMaxEvaluation?.let { currentMax ->
            if(currentMax is NumericalEvaluation) {
                Ongoing(currentMax.numericValue - numericEvalOkRadius)
            }else{
                currentMax
            }
        }
    }
}

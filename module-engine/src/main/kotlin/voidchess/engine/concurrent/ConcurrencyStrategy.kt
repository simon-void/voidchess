package voidchess.engine.concurrent

import voidchess.engine.board.EngineChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.other.StartConfig
import voidchess.common.player.ki.ProgressCallback
import voidchess.common.player.ki.evaluation.*
import voidchess.engine.evaluation.EvaluatingMinMax
import voidchess.engine.board.EngineChessGameImpl

internal abstract class ConcurrencyStrategy {

    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.engine.player".
     * The first element is the best choice for the computer voidchess.engine.player and the last element being the worst.
     */
    fun evaluateMovesBestMoveFirst(
        startConfig: StartConfig,
        movesSoFar: List<Move>,
        evaluatingMinMax: EvaluatingMinMax,
        numericEvalOkRadius: Double = .0,
        progressCallback: ProgressCallback = { _, _ -> }
    ): List<EvaluatedMove> {
        require(numericEvalOkRadius>=.0) {"numericEvalOkRadius must be positive, but was $numericEvalOkRadius"}
        val chessGame = EngineChessGameImpl(startConfig, movesSoFar)
        val possibleMoves = chessGame.getAllMoves()
        return evaluateMoves(chessGame, possibleMoves, progressCallback, evaluatingMinMax, numericEvalOkRadius).apply { sortWith(HighestEvalFirst) }
    }

    abstract fun shutdown()

    protected abstract fun evaluateMoves(
        game: EngineChessGame,
        movesToEvaluate: Collection<Move>,
        progressCallback: ProgressCallback,
        evaluatingMinMax: EvaluatingMinMax,
        numericEvalOkRadius: Double
    ): MutableList<EvaluatedMove>

    companion object {
        // TODO Kotlin 1.4 contract: result==null <=> currentMaxEvaluation==null
        fun getOkEval(
            currentMaxEvaluation: Evaluation?,
            numericEvalOkRadius: Double
        ): Evaluation? = currentMaxEvaluation?.let { currentMax ->
            if(currentMax is NumericalEvaluation) {
                Ongoing(currentMax.numericValue-numericEvalOkRadius)
            }else{
                currentMax
            }
        }
    }
}

package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.EvaluatingMinMax
import voidchess.player.ki.evaluation.EvaluatedMove
import voidchess.player.ki.evaluation.HighestEvalFirst

abstract class AbstractConcurrencyStrategy(protected val showProgress: (Int, Int)->Unit) : ConcurrencyStrategy {

    override fun evaluateMovesBestMoveFirst(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): List<EvaluatedMove> =
            evaluateMoves(game, evaluatingMinMax).apply { sortWith(HighestEvalFirst) }

    protected abstract fun evaluateMoves(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove>
}

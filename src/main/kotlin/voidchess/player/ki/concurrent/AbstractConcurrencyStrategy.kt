package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.DynamicEvaluation
import voidchess.player.ki.evaluation.EvaluatedMove

import java.util.NavigableSet

abstract class AbstractConcurrencyStrategy(protected val showProgress: (Int, Int)->Unit) : ConcurrencyStrategy {

    abstract override fun evaluatePossibleMoves(game: ChessGameInterface, dynamicEvaluation: DynamicEvaluation): NavigableSet<EvaluatedMove>

    protected fun getPossibleMoves(game: ChessGameInterface) = game.getAllMoves()
}

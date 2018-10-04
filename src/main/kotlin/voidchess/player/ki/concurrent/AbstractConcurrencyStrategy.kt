package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.helper.Move
import voidchess.ui.ComputerPlayerUI
import voidchess.player.ki.DynamicEvaluation
import voidchess.player.ki.evaluation.EvaluatedMove

import java.util.ArrayList
import java.util.NavigableSet

abstract class AbstractConcurrencyStrategy(private val ui: ComputerPlayerUI) : ConcurrencyStrategy {
    private val possibleMovesBuffer = ArrayList<Move>(80)

    abstract override fun evaluatePossibleMoves(game: ChessGameInterface, dynamicEvaluation: DynamicEvaluation): NavigableSet<EvaluatedMove>

    protected fun showProgress(movesDone: Int, totalNumberOfMoves: Int) {
        ui.setProgress(movesDone, totalNumberOfMoves)
    }

    protected fun getPossibleMoves(game: ChessGameInterface): List<Move> {
        possibleMovesBuffer.clear()
        game.getPossibleMoves(possibleMovesBuffer)
        return possibleMovesBuffer
    }
}

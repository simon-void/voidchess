package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.DynamicEvaluation
import voidchess.player.ki.evaluation.EvaluatedMove
import java.util.NavigableSet
import java.util.TreeSet

class SingleThreadStrategy(showProgress: (Int, Int)->Unit) : AbstractConcurrencyStrategy(showProgress) {

    override fun evaluatePossibleMoves(game: ChessGameInterface, dynamicEvaluation: DynamicEvaluation): NavigableSet<EvaluatedMove> {
        val possibleMoves = getPossibleMoves(game)

        val totalNumberOfMoves = possibleMoves.size
        showProgress(0, totalNumberOfMoves)

        val result = TreeSet<EvaluatedMove>()
        for ((moveIndex, move) in possibleMoves.withIndex()) {
            val value = dynamicEvaluation.evaluateMove(game, move)
            result.add(EvaluatedMove(move, value))

            showProgress(moveIndex + 1, totalNumberOfMoves)
        }

        return result
    }
}

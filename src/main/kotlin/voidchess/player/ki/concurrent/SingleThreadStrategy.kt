package voidchess.player.ki.concurrent

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.EvaluatingMinMax
import voidchess.player.ki.evaluation.EvaluatedMove

class SingleThreadStrategy(showProgress: (Int, Int)->Unit) : AbstractConcurrencyStrategy(showProgress) {

    override fun evaluateMoves(game: ChessGameInterface, evaluatingMinMax: EvaluatingMinMax): MutableList<EvaluatedMove> {
        val possibleMoves = game.getAllMoves()

        val totalNumberOfMoves = possibleMoves.size
        showProgress(0, totalNumberOfMoves)

        val result = ArrayList<EvaluatedMove>(possibleMoves.size)
        for ((moveIndex, move) in possibleMoves.withIndex()) {
            val value = evaluatingMinMax.evaluateMove(game, move)
            result.add(EvaluatedMove(move, value))

            showProgress(moveIndex + 1, totalNumberOfMoves)
        }

        return result
    }
}

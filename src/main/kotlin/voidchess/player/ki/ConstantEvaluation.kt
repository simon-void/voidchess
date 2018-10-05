package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.Evaluated
import voidchess.player.ki.evaluation.Ongoing

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 */

class ConstantEvaluation : StaticEvaluationInterface {

    override fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean) = nullValue

    override fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, withPrimaryEvaluation: Evaluated) {
        //do nothing (keep the secondary value = 0)
    }

    companion object {
        private val nullValue = Ongoing(0.0)
    }
}

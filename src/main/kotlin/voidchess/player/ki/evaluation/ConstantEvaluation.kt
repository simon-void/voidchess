package voidchess.player.ki.evaluation

import voidchess.board.ChessGameInterface

/**
 * useful for Profile purposes (only DynamicEvaluation remains)
 */

class ConstantEvaluation : StaticEvaluationInterface {

    override fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean) = nullValue

    override fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, evaluated: Evaluated) {
        //do nothing (keep the secondary value = 0)
    }

    companion object {
        private val nullValue = Ongoing(0.0)
    }
}

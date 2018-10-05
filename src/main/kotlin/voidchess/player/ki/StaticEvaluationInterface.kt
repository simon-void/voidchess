package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.Evaluated
import voidchess.player.ki.evaluation.Ongoing


interface StaticEvaluationInterface {
    fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean): Ongoing
    fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, withPrimaryEvaluation: Evaluated)
}
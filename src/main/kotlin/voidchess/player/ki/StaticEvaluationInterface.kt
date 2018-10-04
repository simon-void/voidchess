package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.player.ki.evaluation.Evaluated


interface StaticEvaluationInterface {
    fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean): Evaluated
    fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, withPrimaryEvaluation: Evaluated)
}
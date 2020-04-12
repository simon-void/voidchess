package voidchess.engine.player.ki.evaluation

import voidchess.engine.board.EngineChessGame
import voidchess.common.board.move.Move
import voidchess.common.player.ki.evaluation.*
import java.util.*

private const val cutoffDiff = 1.5

internal class MaxPreliminaryEvaluatedMoves {
    private val prelimEvalMoves = TreeSet(HighestPrelimEvalFirst)
    private var max = .0
    private var cutoffEval = .0
    private var _isEmpty = true

    fun add(move: Move, prelimEval: Double) {
        when {
            _isEmpty -> {
                prelimEvalMoves.add(Pair(move, prelimEval))
                max = prelimEval
                cutoffEval = prelimEval - cutoffDiff
                _isEmpty = false
                return
            }
            prelimEval < cutoffEval -> return
            prelimEval > max -> {
                max = prelimEval
                cutoffEval = prelimEval - cutoffDiff
            }
        }
        prelimEvalMoves.add(Pair(move, prelimEval))
    }

    fun getMax(
        game: EngineChessGame,
        forWhite: Boolean,
        evaluating: EvaluatingStatically,
        selector: Selector<LowestEvaluationFirstComparator>
    ): Evaluation {
        val localCutoffEval = selector.bestItem.let {
            if (it is CheckmateOther) return it
            if (it is NumericalEvaluation) {
                if(it.preliminaryEvaluation > max) {
                    return@let it.preliminaryEvaluation - cutoffDiff
                }
            }
            cutoffEval
        }

        for ((move, preliminaryEvaluation) in prelimEvalMoves) {
            if (preliminaryEvaluation < localCutoffEval) break
            // ok, preliminaryEvaluation is high enough lets get a complete evaluation
            selector.propose(evaluateToOngoing(game, move, forWhite, preliminaryEvaluation, evaluating))
        }
        return selector.bestItem
            ?: throw IllegalStateException("no preliminary moves were added and the selector was also _isEmpty")
    }

    private fun evaluateToOngoing(
        game: EngineChessGame,
        move: Move,
        forWhite: Boolean,
        prelimEval: Double,
        evaluating: EvaluatingStatically
    ): Ongoing = game.withMove(move) {
        evaluating.addSecondaryEvaluationTo(prelimEval, game, forWhite)
    }
}

internal class MinPreliminaryEvaluatedMoves {
    private val prelimEvalMoves = TreeSet(LowestPrelimEvalFirst)
    private var min = .0
    private var cutoffEval = .0
    private var _isEmpty = true

    fun add(move: Move, prelimEval: Double) {
        when {
            _isEmpty -> {
                prelimEvalMoves.add(Pair(move, prelimEval))
                min = prelimEval
                cutoffEval = prelimEval + cutoffDiff
                _isEmpty = false
                return
            }
            prelimEval > cutoffEval -> return
            prelimEval < min -> {
                min = prelimEval
                cutoffEval = prelimEval + cutoffDiff
            }
        }
        prelimEvalMoves.add(Pair(move, prelimEval))
    }

    fun getMin(
        game: EngineChessGame,
        forWhite: Boolean,
        evaluating: EvaluatingStatically,
        selector: Selector<HighestEvaluationFirstComparator>
    ): Evaluation {
        val localCutoffEval = selector.bestItem.let {
            if (it is CheckmateSelf) return it
            if (it is NumericalEvaluation) {
                if(it.preliminaryEvaluation < min) {
                    return@let it.preliminaryEvaluation + cutoffDiff
                }
            }
            cutoffEval
        }

        for ((move, preliminaryEvaluation) in prelimEvalMoves) {
            if (preliminaryEvaluation > localCutoffEval) break
            // ok, preliminaryEvaluation is low enough lets get a complete evaluation
            selector.propose(evaluateToOngoing(game, move, forWhite, preliminaryEvaluation, evaluating))
        }
        return selector.bestItem
            ?: throw IllegalStateException("no preliminary moves were added and the selector was also empty")
    }

    private fun evaluateToOngoing(
        game: EngineChessGame,
        move: Move,
        forWhite: Boolean,
        prelimEval: Double,
        evaluating: EvaluatingStatically
    ): Ongoing = game.withMove(move) {
        evaluating.addSecondaryEvaluationTo(prelimEval, game, forWhite)
    }
}
package voidchess.player.ki.evaluation

import voidchess.board.ChessGameInterface
import voidchess.board.move.Move
import voidchess.board.withMove
import java.util.*

data class EvaluatedMove(val move: Move, val value: Evaluation)

typealias PreliminaryEvaluateMove = Pair<Move, Double>

class Selector<C:Comparator<Evaluation>>(private val betterIsBiggerComparator: C) {
    private var currentBest: Evaluation? = null
    val bestItem: Evaluation? get() = currentBest

    fun propose(candidate: Evaluation) {
        currentBest.let {
            if (it == null || betterIsBiggerComparator.compare(candidate, it) > 0) {
                currentBest = candidate
            }
        }
    }
}

private const val cutoffDiff = 1.5

class MaxPreliminaryEvaluatedMoves {
    private val prelimEvalMoves = TreeSet<PreliminaryEvaluateMove>(HighestPrelimEvalFirst)
    private var max = .0
    private var cutoffEval = .0
    private var _isEmpty = true
    val isEmpty get() = _isEmpty

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
            game: ChessGameInterface,
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
            game: ChessGameInterface,
            move: Move,
            forWhite: Boolean,
            prelimEval: Double,
            evaluating: EvaluatingStatically
    ): Ongoing = game.withMove(move) { gameAfterMove ->
        evaluating.addSecondaryEvaluationTo(prelimEval, gameAfterMove, forWhite)
    }
}

class MinPreliminaryEvaluatedMoves {
    private val prelimEvalMoves = TreeSet<PreliminaryEvaluateMove>(LowestPrelimEvalFirst)
    private var min = .0
    private var cutoffEval = .0
    private var _isEmpty = true
    val isEmpty get() = _isEmpty

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
            game: ChessGameInterface,
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
            game: ChessGameInterface,
            move: Move,
            forWhite: Boolean,
            prelimEval: Double,
            evaluating: EvaluatingStatically
    ): Ongoing = game.withMove(move) { gameAfterMove ->
        evaluating.addSecondaryEvaluationTo(prelimEval, gameAfterMove, forWhite)
    }
}




object LowestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = LowestEvaluationFirstComparator.compare(o1.value, o2.value)
}

object HighestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = LowestEvaluationFirstComparator.compare(o2.value, o1.value)
}

object LowestPrelimEvalFirst: Comparator<PreliminaryEvaluateMove> {
    override fun compare(o1: Pair<Move, Double>, o2: Pair<Move, Double>) = o1.second.compareTo(o2.second)
}

object HighestPrelimEvalFirst: Comparator<PreliminaryEvaluateMove> {
    override fun compare(o1: Pair<Move, Double>, o2: Pair<Move, Double>) = o2.second.compareTo(o1.second)
}
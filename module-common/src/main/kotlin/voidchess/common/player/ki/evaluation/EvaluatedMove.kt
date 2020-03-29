package voidchess.common.player.ki.evaluation

import voidchess.common.board.move.Move
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

object LowestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) =
        LowestEvaluationFirstComparator.compare(
            o1.value,
            o2.value
        )
}

object HighestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) =
        LowestEvaluationFirstComparator.compare(
            o2.value,
            o1.value
        )
}

object LowestPrelimEvalFirst: Comparator<PreliminaryEvaluateMove> {
    override fun compare(o1: Pair<Move, Double>, o2: Pair<Move, Double>) = o1.second.compareTo(o2.second)
}

object HighestPrelimEvalFirst: Comparator<PreliminaryEvaluateMove> {
    override fun compare(o1: Pair<Move, Double>, o2: Pair<Move, Double>) = o2.second.compareTo(o1.second)
}
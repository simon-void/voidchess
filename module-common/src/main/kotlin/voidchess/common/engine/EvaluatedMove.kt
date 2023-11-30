package voidchess.common.engine

import voidchess.common.board.move.Move

data class EvaluatedMove(val move: Move, val value: Evaluation)

data object LowestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = o1.value.compareTo(o2.value)
}

data object HighestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = o2.value.compareTo(o1.value)
}

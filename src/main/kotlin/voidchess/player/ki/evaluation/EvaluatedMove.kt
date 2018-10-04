package voidchess.player.ki.evaluation

import voidchess.helper.Move

class EvaluatedMove(val move: Move, val value: Evaluated) : Comparable<EvaluatedMove> {

    override fun compareTo(other: EvaluatedMove): Int {
        //moves with high evaluations should come first in a sorted list
        return value.compareTo(other.value)
    }

    override fun equals(obj: Any?): Boolean {
        if(obj !is EvaluatedMove) return false
        return move.equalsMove(obj.move)
    }

    override fun hashCode() = move.hashCode()
}

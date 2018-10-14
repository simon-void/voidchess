package voidchess.player.ki.evaluation

import voidchess.board.move.Move

class EvaluatedMove(val move: Move, val value: Evaluated) : Comparable<EvaluatedMove> {

    override fun compareTo(other: EvaluatedMove): Int {
        //moves with high evaluations should come first in a sorted list
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if(other !is EvaluatedMove) return false
        return move.equalsMove(other.move)
    }

    override fun hashCode() = move.hashCode()
}

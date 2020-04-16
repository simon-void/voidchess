package voidchess.engine.player.ki.evaluation

import voidchess.common.board.move.Move

internal class BestResponseSet {
    private val moveAndOccurrence = HashMap<Move, Int>()

    fun add(move: Move) {
        val oldOccurrence = moveAndOccurrence.getOrDefault(move, 0)
        moveAndOccurrence[move] = oldOccurrence + 1
    }

    inline fun forEach(consume: (Move)->Unit) {
        moveAndOccurrence.entries.sortedByDescending { it.value }.forEach { (move, _)->
            consume(move)
        }
    }
}
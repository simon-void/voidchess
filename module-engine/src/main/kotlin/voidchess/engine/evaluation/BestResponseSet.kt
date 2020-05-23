package voidchess.engine.evaluation

import voidchess.common.board.move.Move
import java.util.concurrent.locks.ReentrantLock


internal interface BestResponseSet {
    fun add(move: Move)
    fun forEach(consume: (Move) -> Unit)

    companion object {
        fun unsynced(): BestResponseSet = UnsyncedBestResponseSet()
        fun synced(): BestResponseSet = SyncedBestResponseSet()
    }
}

private class SyncedBestResponseSet : BestResponseSet {
    private val moveAndOccurrence = mutableMapOf<Move, Int>()
    private val lock = ReentrantLock()

    override fun add(move: Move) {
        synchronized(lock) {
            val oldOccurrence = moveAndOccurrence.getOrDefault(move, 0)
            moveAndOccurrence[move] = oldOccurrence + 1
        }
    }

    override fun forEach(consume: (Move) -> Unit) {
        val moveAndOccurrenceCopy = HashMap<Move, Int>(moveAndOccurrence.size, 1.1F)
        synchronized(lock) {
            moveAndOccurrenceCopy.putAll(moveAndOccurrence)
        }
        moveAndOccurrenceCopy.forEachByDescendingOccurrence(consume)
    }
}

private class UnsyncedBestResponseSet : BestResponseSet {
    private val moveAndOccurrence = mutableMapOf<Move, Int>()

    override fun add(move: Move) {
        val oldOccurrence = moveAndOccurrence.getOrDefault(move, 0)
        moveAndOccurrence[move] = oldOccurrence + 1
    }

    override fun forEach(consume: (Move) -> Unit) {
        moveAndOccurrence.forEachByDescendingOccurrence(consume)
    }
}

private fun Map<Move, Int>.forEachByDescendingOccurrence(consume: (Move) -> Unit) {
    this.entries.sortedByDescending { it.value }.forEach { (move, _) ->
        consume(move)
    }
}
package voidchess.engine.evaluation

import voidchess.common.board.move.Move
import java.util.concurrent.locks.ReentrantReadWriteLock


internal interface BestResponseSet {
    fun add(move: Move)
    fun forEach(consume: (Move) -> Unit)
    fun getOccurrence(move: Move): Int?
    val size: Int

    companion object {
        fun unsynced(): BestResponseSet = UnsyncedBestResponseSet()
        fun synced(): BestResponseSet = SyncedBestResponseSet()
    }
}

private class SyncedBestResponseSet : BestResponseSet {
    private val moveAndOccurrence = mutableMapOf<Move, Int>()
    private val lock = ReentrantReadWriteLock()

    override fun add(move: Move) {
        synchronized(lock.writeLock()) {
            val oldOccurrence = moveAndOccurrence.getOrDefault(move, 0)
            moveAndOccurrence[move] = oldOccurrence + 1
        }
    }

    override fun forEach(consume: (Move) -> Unit) {
        synchronized(lock.readLock()) {
            moveAndOccurrence.forEachByDescendingOccurrence(consume)
        }
    }

    override fun getOccurrence(move: Move): Int? = synchronized(lock.readLock()) {
        moveAndOccurrence[move]
    }

    override val size: Int
        get() = synchronized(lock.readLock()) {moveAndOccurrence.size}
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

    override fun getOccurrence(move: Move): Int? = moveAndOccurrence[move]
    override val size: Int
        get() = moveAndOccurrence.size
}

private fun Map<Move, Int>.forEachByDescendingOccurrence(consume: (Move) -> Unit) {
    this.entries.sortedByDescending { it.value }.forEach { (move, _) ->
        consume(move)
    }
}
package voidchess.board.move

import java.lang.IllegalStateException

data class PositionProgression(
        val inclusiveStartPos: Position,
        val size: Int,
        val direction: Direction
) {
    init {
        assert(size in 0..7)
    }

    val isEmpty = size==0
    val hasSinglePos = size==1

    inline fun forEachReachablePos(informOf: (Position) -> Unit) {
        var index = 1;
        var currentPos = inclusiveStartPos
        if(size!=0) {
            informOf(currentPos)
            while(index!=size) {
                currentPos = currentPos.step(direction) ?: throw IllegalStateException("PositionProgression left the board! startPos: $inclusiveStartPos, size: $size, direction: $direction")
                informOf(currentPos)
                index++
            }
        }
    }

    fun contains(pos: Position): Boolean {
        when(size) {
            0->return false
            1->return pos==inclusiveStartPos
            else ->{
                if(pos==inclusiveStartPos) return true
                val dirToPos = inclusiveStartPos.getDirectionTo(pos) ?: return false
                return if(dirToPos==direction) inclusiveStartPos.distanceTo(pos)<size else false
            }
        }
    }
}
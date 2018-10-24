package voidchess.board.move

data class PositionProgression(
        private val inclusiveStartPos: Position,
        val size: Int,
        val direction: Direction
): Iterable<Position> {
    init {
        assert(size in 0..7)
    }

    val isEmpty = size==0

    override fun iterator(): Iterator<Position> {
        if(isEmpty) {
            return emptyList<Position>().iterator()
        }
        var numberOfElementsLeftToIterateOver = size
        val sequence = generateSequence({ inclusiveStartPos.takeUnless { isEmpty } }) { currentPos: Position ->
            if(--numberOfElementsLeftToIterateOver==0) {
                return@generateSequence null
            } else {
                return@generateSequence currentPos.step(direction)
            }
        }
        return sequence.iterator()
    }
}
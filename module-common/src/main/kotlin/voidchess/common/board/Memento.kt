package voidchess.common.board

import voidchess.common.board.move.Position

class Memento (
        game: StaticChessBoard,
) {
    private val figureCount: Byte
    private val compressedBoard: LongArray

    init {
        val board = IntArray(64)
        figureCount = run {
            var count = 0
            for (index in 0..63) {
                game.getFigureOrNull(Position.byIndex(index))?.let { figure ->
                    board[index] = figure.typeInfo
                    count++
                }
            }
            count.toByte()
        }

        // compress the board by exploiting that typeInfo is smaller than 16
        // and therefore only 4 bits are needed -> pack 16 typeInfos into 1 long
        compressedBoard = longArrayOf(
                compressBoardSlicesToLong(board, 0, 16),
                compressBoardSlicesToLong(board, 16, 32),
                compressBoardSlicesToLong(board, 32, 48),
                compressBoardSlicesToLong(board, 48, 64),
        )
    }

    fun hasSameNumberOfFiguresAs(other: Memento): Boolean = figureCount == other.figureCount

    /**
     * calling function has to make sure that only Mementos are compared that are guaranteed
     * to have been created for Mementos of a "different color"
     * (since the same configuration on the board doesn't count as same configuration, if
     * it's the other player's turn)
     */
    fun equalsOtherWhileAssertingColorMatch(other: Memento): Boolean =
        //isWhite == other.isWhite &&
        compressedBoard.contentEquals(other.compressedBoard)

    private fun compressBoardSlicesToLong(board: IntArray, startIndex: Int, endIndex: Int): Long {
        assert(endIndex - startIndex <= 16)

        val endIndexMinusOne = endIndex - 1
        var compressedValue: Long = 0
        for (i in startIndex until endIndexMinusOne) {
            assert(board[i] in 0..15) // board[i] (=figure==null?0:figure.typeInfo) out of Bounds, it has to fit into 4 bits with 0->no figure!
            // optimized form of
//            compressedValue += board[i].toLong()
//            compressedValue = compressedValue shl 4
            compressedValue = (compressedValue or board[i].toLong()) shl 4
        }
        compressedValue += board[endIndexMinusOne].toLong()
        return compressedValue
    }
}

fun ArrayList<Memento>.doesLatestMementoOccurThreeTimes(
        numberOfMovesWithoutPawnMoveOrFigureTaken: Int
): Boolean {
    // the second condition happens only in test when the game is initialized from a description where figureTaken at startup > 0
    if (numberOfMovesWithoutPawnMoveOrFigureTaken < 8 || numberOfMovesWithoutPawnMoveOrFigureTaken > size) return false
    val lastIndex = this.lastIndex
    val earliestIndex = (lastIndex-numberOfMovesWithoutPawnMoveOrFigureTaken)
    val lastMemento = this[lastIndex]
    var count = 1

    assert(lastMemento.hasSameNumberOfFiguresAs(this[earliestIndex])) {
        """
        since the number of figures between the earlies and last index to check doesn't match,
        the value of the numberOfMovesWithoutPawnMoveOrFigureTaken parameter must have been off.
        """.trimIndent()
    }

    for(i in (lastIndex-4) downTo earliestIndex step 2) {
        val currentMemento = this[i]
        if(lastMemento.equalsOtherWhileAssertingColorMatch(currentMemento)) {
            count++
            if(count==3) {
                return true
            }
        }
    }
    return false
}

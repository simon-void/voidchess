package voidchess.united.board

import voidchess.common.board.*
import voidchess.common.board.move.*
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.King
import java.util.*


internal class CentralChessGameImpl private constructor(
    private val board: ChessBoard,
    override var startConfig: StartConfig,
    private val mementoStack: ArrayDeque<Memento>,
    private val numberStack: NumberStack
): CentralChessGame, StaticChessBoard by board {
    private var numberOfMovesWithoutPawnOrCatchingMove: Int = 0
    private var latestExtendedMove: ExtendedMove? = null

    override val hasHitFigure: Boolean get() = latestExtendedMove?.hasHitFigure ?: startConfig.hasHitFigureInPreviousMove
    override val isWhiteTurn: Boolean get() = board.isWhiteTurn
    override val isCheck get() = board.getCachedAttackLines().isCheck

    private val isEnd: MoveResultType
        get() {
            if (noMovesLeft(isWhiteTurn)) {
                return if (isCheck) {
                    MoveResultType.CHECKMATE
                } else {
                    MoveResultType.STALEMATE
                }
            }
            if (isDrawBecauseOfLowMaterial) {
                return MoveResultType.DRAW
            }
            if (isDrawBecauseOfThreeTimesSamePosition) {
                return MoveResultType.THREE_TIMES_SAME_POSITION
            }
            return if (numberOfMovesWithoutPawnOrCatchingMove == 100) {
                MoveResultType.FIFTY_MOVES_NO_HIT
            } else MoveResultType.NO_END
        }

    private val isDrawBecauseOfThreeTimesSamePosition: Boolean
        get() = mementoStack.countOccurrencesOfLastMemento() >= 3

    constructor(
        startConfig: StartConfig = StartConfig.ClassicConfig
    ) : this(
        ArrayChessBoard(startConfig),
        startConfig,
        ArrayDeque<Memento>(64),
        NumberStack()
    ) {
        numberOfMovesWithoutPawnOrCatchingMove = startConfig.numberOfMovesWithoutHit
        for (i in 0 until numberOfMovesWithoutPawnOrCatchingMove) numberStack.didNotCatchFigureOrMovePawn()

        memorizeGame()
    }

    override fun isSelectable(pos: Position): Boolean {
        val figure = getFigureOrNull(pos)
        return figure!=null && figure.isWhite == isWhiteTurn && figure.isSelectable(board)
    }

    override fun isMovable(from: Position, to: Position): Boolean {
        val figure = getFigureOrNull(from)
        return figure!=null && figure.isWhite == isWhiteTurn && figure.isMovable(to, board)
    }

    override fun move(move: Move): MoveResultType {
        val extendedMove = board.move(move)

        if (extendedMove.hasHitFigure || extendedMove.isPawnMove()) {
            numberStack.didCatchFigureOrMovePawn()
            numberOfMovesWithoutPawnOrCatchingMove = 0
        } else {
            numberStack.didNotCatchFigureOrMovePawn()
            numberOfMovesWithoutPawnOrCatchingMove++
        }

        latestExtendedMove = extendedMove
        memorizeGame()

        return isEnd
    }



    override fun getLatestExtendedMove(): ExtendedMove {
        return latestExtendedMove ?: throw IllegalStateException("no move was been executed yet")
    }

    override fun toString() = "${if (isWhiteTurn) "white" else "black"} $numberOfMovesWithoutPawnOrCatchingMove $board"

    override fun getCompleteHistory() = board.historyToString(null)

    override fun initGame(newConfig: StartConfig) {
        numberOfMovesWithoutPawnOrCatchingMove = 0
        mementoStack.clear()
        numberStack.init()
        startConfig = newConfig
        board.init(newConfig)

        memorizeGame()
    }

    fun equalsOther(other: CentralChessGameImpl): Boolean {
        if (isWhiteTurn != other.isWhiteTurn) return false

        for (index in 0..63) {
            val pos = Position.byIndex(index)
            val content = getFigureOrNull(pos)
            val otherContent = other.getFigureOrNull(pos)
            if (content!=otherContent) {
                return false
            }
        }

        return true
    }

    private fun noMovesLeft(caseWhite: Boolean): Boolean {
        board.forAllFiguresOfColor(caseWhite) { figure ->
            if (figure !is King && figure.isSelectable(board)) {
                return false
            }
        }
        return !board.getKing(caseWhite).isSelectable(board)
    }

    private fun memorizeGame() = mementoStack.addLast(Memento(board, isWhiteTurn))
}

private class Memento constructor(game: StaticChessBoard, private val isWhite: Boolean) {
    internal val figureCount: Int
    private val compressedBoard: LongArray

    init {
        var count = 0
        val board = IntArray(64)
        for (index in 0..63) {
            val figure = game.getFigureOrNull(Position.byIndex(index))
            if (figure != null) {
                board[index] = figure.typeInfo
                count++
            }
        }

        // compress the board by exploiting that typeInfo is smaller than 16
        // and therefore only 4 bits are needed -> pack 15 typeInfos into 1 long
        compressedBoard = longArrayOf(
                compressBoardSlicesToLong(board, 0, 15),
                compressBoardSlicesToLong(board, 15, 30),
                compressBoardSlicesToLong(board, 30, 45),
                compressBoardSlicesToLong(board, 45, 60),
                compressBoardSlicesToLong(board, 60, 64))
        figureCount = count
    }

    fun hasDifferentNumberOfFiguresAs(other: Memento): Boolean {
        return figureCount != other.figureCount
    }

    fun equalsOther(other: Memento): Boolean {
        return isWhite == other.isWhite && compressedBoard.contentEquals(other.compressedBoard)
    }

    private fun compressBoardSlicesToLong(board: IntArray, startIndex: Int, endIndex: Int): Long {
        assert(endIndex - startIndex < 16)

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

private class NumberStack internal constructor() {
    private var numberStack: IntArray
    private var index: Int = 0

    init {
        numberStack = IntArray(50)
        init()
    }

    internal fun init() {
        for (i in numberStack.indices) numberStack[i] = 0
        index = 0
    }

    internal fun didNotCatchFigureOrMovePawn() {
        numberStack[index]++
    }

    internal fun didCatchFigureOrMovePawn() {
        ensureCapacity()
        index++
    }

    private fun ensureCapacity() {
        if (index + 1 == numberStack.size) {
            val newNumberStack = IntArray(numberStack.size * 2)
            System.arraycopy(numberStack, 0, newNumberStack, 0, numberStack.size)
            numberStack = newNumberStack
        }
    }
}

private fun ArrayDeque<Memento>.countOccurrencesOfLastMemento(): Int {
    val inverseIter: Iterator<Memento> = descendingIterator()
    val lastMemento = inverseIter.next()
    var count = 1

    // check only every second memento
    if(inverseIter.hasNext()) {
        inverseIter.next()
    }else{
        return count
    }

    while(inverseIter.hasNext()) {
        val memento = inverseIter.next()
        if(memento.hasDifferentNumberOfFiguresAs(lastMemento)) {
            break
        }
        if(memento.equalsOther(lastMemento)) {
            count++
        }

        // check only every second memento
        if(inverseIter.hasNext()) {
            inverseIter.next()
        }else{
            break
        }
    }

    return count
}

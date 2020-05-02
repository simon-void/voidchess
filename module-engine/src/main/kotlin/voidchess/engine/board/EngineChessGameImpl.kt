package voidchess.engine.board

import voidchess.common.board.*
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.King
import voidchess.engine.evaluation.SearchTreePruner
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


internal class EngineChessGameImpl private constructor(
    override val startConfig: StartConfig,
    private val board: ChessBoard,
    private val mementoStack: ArrayDeque<Memento>,
    private val numberStack: NumberStack
): EngineChessGame, StaticChessBoard by board {
    private var numberOfMovesWithoutHit: Int = 0
    override var hasHitFigure: Boolean = false

    override val isWhiteTurn: Boolean get() = board.isWhiteTurn
    override val isCheck get() = board.getCachedAttackLines().isCheck

    private val isEnd: MoveResult
        get() {
            if (noMovesLeft(isWhiteTurn)) {
                return if (isCheck) {
                    MoveResult.CHECKMATE
                } else {
                    MoveResult.STALEMATE
                }
            }
            if (isDrawBecauseOfLowMaterial) {
                return MoveResult.DRAW
            }
            if (isDrawBecauseOfThreeTimesSamePosition) {
                return MoveResult.THREE_TIMES_SAME_POSITION
            }
            return if (numberOfMovesWithoutHit == 100) {
                MoveResult.FIFTY_MOVES_NO_HIT
            } else MoveResult.NO_END
        }

    private val isDrawBecauseOfThreeTimesSamePosition: Boolean
        get() = mementoStack.countOccurrencesOfLastMemento() >= 3

    /**
     * copy-constructor
     */
    private constructor(
        other: EngineChessGameImpl,
        startConfig: StartConfig,
        movesPlayed: List<Move>
    ) : this(
        other.startConfig,
        when (startConfig) {
            is StartConfig.ManualConfig -> ArrayChessBoard(startConfig)
            is StartConfig.ClassicConfig -> ArrayChessBoard(startConfig)
            is StartConfig.Chess960Config -> ArrayChessBoard(startConfig)
        }.also { board ->
            for (move in movesPlayed) {
                board.move(move)
            }
        },
        other.mementoStack.shallowCopy(),
        NumberStack(other.numberStack)
    ) {
        hasHitFigure = other.hasHitFigure
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit
    }

    constructor(
        startConfig: StartConfig = StartConfig.ClassicConfig,
        movesSoFar: Iterable<Move> = emptyList()
    ) : this(
        startConfig,
        ArrayChessBoard(startConfig),
        ArrayDeque<Memento>(64),
        NumberStack()
    ) {
        numberOfMovesWithoutHit = startConfig.numberOfMovesWithoutHit
        for (i in 0 until numberOfMovesWithoutHit) numberStack.noFigureHit()

        memorizeGame()
        hasHitFigure = numberOfMovesWithoutHit == 0

        for(move in movesSoFar) {
            move(move)
        }
    }

    override fun <T> withMove(move: Move, workWithGameAfterMove: (MoveResult) -> T): T {
        val moveResult = move(move)
        val result = workWithGameAfterMove(moveResult)
        undo()
        return result
    }

    private fun move(move: Move): MoveResult {
        hasHitFigure = board.move(move).hasHitFigure

        if (hasHitFigure) {
            numberStack.figureHit()
            numberOfMovesWithoutHit = 0
        } else {
            numberStack.noFigureHit()
            numberOfMovesWithoutHit++
        }

        memorizeGame()

        return isEnd
    }

    private fun undo() {
        numberOfMovesWithoutHit = numberStack.undo()
        mementoStack.removeLast()

        board.undo()
    }

    internal fun isMovable(from: Position, to: Position): Boolean {
        val figure = getFigureOrNull(from)
        return figure!=null && figure.isWhite == isWhiteTurn && figure.isMovable(to, board)
    }

    override fun toString() = "${if (isWhiteTurn) "white" else "black"} $numberOfMovesWithoutHit $board"

    override val completeHistory: String get() = board.historyToString(null)
    override val shortTermHistory: String get() = board.historyToString(4)

    fun equalsOther(other: EngineChessGameImpl): Boolean {
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

    override fun getAllMoves(): ArrayList<Move> {
        val possibleMoves = ArrayList<Move>(64)
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.forPossibleMoves(board) {
                possibleMoves.add(it)
            }
        }
        return possibleMoves
    }

    override fun getCriticalMoves(): ArrayList<Move> {
        val criticalMoves = LinkedHashSet<Move>()
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.forCriticalMoves(board, criticalMoves)
        }
        return ArrayList<Move>(criticalMoves.size).apply {
            addAll(criticalMoves)
        }
    }

    override fun getTakingMoves(): ArrayList<Move> {
        val takingMoves = ArrayList<Move>(16)
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.forPossibleTakingMoves(board) {
                takingMoves.add(it)
            }
        }
        return takingMoves
    }


    override fun countReachableMoves(): MoveCounter {
        var whiteCount = 0
        var blackCount = 0

        board.forAllFigures { figure->
            val count = figure.countReachableMoves(board)
            if(figure.isWhite) {
                whiteCount += count
            }else{
                blackCount += count
            }
        }

        return MoveCounter(whiteCount, blackCount)
    }

    override fun countAllMoves(): MoveCounter {
        var whiteCount = 0
        var blackCount = 0

        board.forAllFigures { figure->
            figure.forPossibleMoves(board) {
                if(figure.isWhite) {
                    whiteCount ++
                }else{
                    blackCount ++
                }
            }
        }

        return MoveCounter(whiteCount, blackCount)
    }

    override fun copyGame(numberOfInstances: Int): List<EngineChessGame> {
        require(numberOfInstances>0) {"numberOfInstances must be bigger than 0 but was $numberOfInstances"}

        // TODO the stacks can be shortened to when the last figure was taken! This should be a bit more efficient.

        val gameInstances = ArrayList<EngineChessGame>(numberOfInstances)
        gameInstances.add(this)

        if (numberOfInstances > 1) {
            val moves = board.movesPlayed()
            for (i in 1 until numberOfInstances) {
                val copy = EngineChessGameImpl(this, startConfig, moves)
                gameInstances.add(copy)
            }
        }
        return gameInstances
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

private class NumberStack {
    private var numberStack: IntArray
    private var index: Int = 0

    internal constructor() {
        numberStack = IntArray(50)
        init()
    }

    //copy-Constructor
    internal constructor(other: NumberStack) {
        numberStack = IntArray(other.index + SearchTreePruner.MAX_SEARCH_DEPTH)
        System.arraycopy(other.numberStack, 0, numberStack, 0, numberStack.size)
        index = other.index
    }

    internal fun init() {
        for (i in numberStack.indices) numberStack[i] = 0
        index = 0
    }

    internal fun noFigureHit() {
        numberStack[index]++
    }

    internal fun figureHit() {
        ensureCapacity()
        index++
    }

    internal fun undo(): Int {
        if (numberStack[index] == 0) {
            index--
        } else {
            numberStack[index]--
        }
        return numberStack[index]
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

@Suppress("UNCHECKED_CAST")
internal fun <T> ArrayDeque<T>.shallowCopy() = clone() as ArrayDeque<T>

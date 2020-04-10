package voidchess.engine.board

import voidchess.common.board.*
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.ChessGameSupervisorDummy
import voidchess.common.board.other.StartConfig
import voidchess.engine.player.ki.evaluation.SearchTreePruner
import java.util.*


internal class ChessGame private constructor(
    private val board: ChessBoard,
    override val startConfig: StartConfig,
    private val mementoStack: LinkedList<Memento>,
    private val numberStack: NumberStack,
    private var supervisor: ChessGameSupervisor
): ChessGameInterface, BasicChessBoard by board {
    private var numberOfMovesWithoutHit: Int = 0
    private var figureCount: Int = 32
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

    val history: String
        get() = board.historyToString(4)

    private val isDrawBecauseOfLowMaterial: Boolean
        get() {
            if (mementoStack.last.figureCount > 6) {
                return false
            }
            var numberOfWhiteBishops = 0
            var numberOfBlackBishops = 0
            var numberOfWhiteKnights = 0
            var numberOfBlackKnights = 0

            board.forAllFigures { figure ->
                if (figure.isPawn()
                        || figure.isRook()
                        || figure.isQueen()) {
                    return false
                } else if (figure.isBishop()) {
                    if (figure.isWhite)
                        numberOfWhiteBishops++
                    else
                        numberOfBlackBishops++
                } else if (figure.isKnight()) {
                    if (figure.isWhite)
                        numberOfWhiteKnights++
                    else
                        numberOfBlackKnights++
                }
            }

            if (numberOfWhiteBishops > 1 || numberOfBlackBishops > 1) {
                return false
            }
            if (numberOfWhiteKnights > 2 || numberOfBlackKnights > 2) {
                return false
            }
            if (numberOfWhiteBishops == 1 && numberOfWhiteKnights > 0) {
                return false
            }
            return numberOfBlackBishops == 0 || numberOfBlackKnights == 0
        }

    private val isDrawBecauseOfThreeTimesSamePosition: Boolean
        get() = mementoStack.countOccurrencesOfLastMemento() >= 3

    /**
     * the normal constructor
     */
    constructor(supervisor: ChessGameSupervisor): this(
        ArrayChessBoard(StartConfig.ClassicConfig),
            StartConfig.ClassicConfig,
            LinkedList<Memento>(),
            NumberStack(),
            supervisor
    ) {
        initGame()
    }

    /**
     * copy-constructor
     */
    private constructor(other: ChessGame, startConfig: StartConfig, movesPlayed: List<Move>) : this(
        when (startConfig) {
            is StartConfig.ManualConfig -> ArrayChessBoard(startConfig)
            is StartConfig.ClassicConfig -> ArrayChessBoard(startConfig)
            is StartConfig.Chess960Config -> ArrayChessBoard(startConfig)
        }.also { board ->
            for (move in movesPlayed) {
                board.move(move, ChessGameSupervisorDummy)
            }
        },
        other.startConfig,
        other.mementoStack.shallowCopy(),
        NumberStack(other.numberStack),
        ChessGameSupervisorDummy
    ) {
        hasHitFigure = other.hasHitFigure
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit
        figureCount = other.figureCount
    }

    /**
     * for unit-tests
     */
    internal constructor(
        startConfig: StartConfig = StartConfig.ClassicConfig,
        supervisor: ChessGameSupervisor = ChessGameSupervisorDummy
    ) : this(
        ArrayChessBoard(startConfig),
        startConfig,
        LinkedList<Memento>(),
        NumberStack(),
        supervisor
    ) {
        numberOfMovesWithoutHit = startConfig.numberOfMovesWithoutHit
        for (i in 0 until numberOfMovesWithoutHit) numberStack.noFigureHit()

        figureCount = startConfig.figureCount

        memorizeGame()
        hasHitFigure = numberOfMovesWithoutHit == 0
    }

    override fun useSupervisor(supervisor: ChessGameSupervisor) {
        this.supervisor = supervisor
    }

    override fun suspendInteractiveSupervisor(): ChessGameSupervisor {
        val normalSupervisor = supervisor
        supervisor = ChessGameSupervisorDummy
        return normalSupervisor
    }

    override fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean {
        val figure = getFigureOrNull(pos)
        return figure!=null && figure.isWhite == whitePlayer && figure.isSelectable(board)
    }

    override fun isMovable(from: Position, to: Position, whitePlayer: Boolean): Boolean {
        val figure = getFigureOrNull(from)
        return figure!=null && figure.isWhite == whitePlayer && figure.isMovable(to, board)
    }

    override fun countFigures(): Int {
        return figureCount
    }

    override fun move(move: Move): MoveResult {
        hasHitFigure = board.move(move, supervisor).hasHitFigure

        if (hasHitFigure) {
            numberStack.figureHit()
            numberOfMovesWithoutHit = 0
            figureCount--
        } else {
            numberStack.noFigureHit()
            numberOfMovesWithoutHit++
        }

        memorizeGame()

        return isEnd
    }

    override fun undo() {
        numberOfMovesWithoutHit = numberStack.undo()
        mementoStack.removeLast()

        val undoneMoveHasHitFigure = board.undo()

        if (undoneMoveHasHitFigure) {
            figureCount++
        }
    }

    override fun toString() = "${if (isWhiteTurn) "white" else "black"} $numberOfMovesWithoutHit $board"

    override fun getCompleteHistory() = board.historyToString(null)

    private fun initGame() = initGame(518)    //classic chess starting configuration

    override fun initGame(chess960: Int) {
        numberOfMovesWithoutHit = 0
        figureCount = 32
        mementoStack.clear()
        numberStack.init()

        board.init(StartConfig.Chess960Config(chess960))

        memorizeGame()
    }

    fun equalsOther(other: ChessGame): Boolean {
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
            if (!figure.isKing() && figure.isSelectable(board)) {
                return false
            }
        }
        return !board.getKing(caseWhite).isSelectable(board)
    }

    override fun getAllMoves(): List<Move> {
        val possibleMoves = LinkedList<Move>()
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.getPossibleMoves(board, possibleMoves)
        }
        return possibleMoves
    }

    override fun getCriticalMoves(): Collection<Move> {
        val criticalMoves = TreeSet<Move>()
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.getCriticalMoves(board, criticalMoves)
        }
        return criticalMoves
    }

    override fun getTakingMoves(): List<Move> {
        val takingMoves = LinkedList<Move>()
        board.forAllFiguresOfColor(isWhiteTurn) { figure ->
            figure.getPossibleTakingMoves(board, takingMoves)
        }
        return takingMoves
    }


    override fun countReachableMoves(): Pair<Int, Int> {
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

        return Pair(whiteCount, blackCount)
    }

    override fun copyGame(neededInstances: Int): List<ChessGameInterface> {
        val gameInstances = ArrayList<ChessGameInterface>(neededInstances)
        gameInstances.add(this)

        if (neededInstances > 1) {
            val moves = board.movesPlayed()
            for (i in 1 until neededInstances) {
                val copy = ChessGame(this, startConfig, moves)
                gameInstances.add(copy)
            }
        }
        return gameInstances
    }

    private fun memorizeGame() = mementoStack.addLast(Memento(board, isWhiteTurn))
}

private class Memento constructor(game: BasicChessBoard, private val isWhite: Boolean) {
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

private fun LinkedList<Memento>.countOccurrencesOfLastMemento(): Int {
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
internal fun <T> LinkedList<T>.shallowCopy() = clone() as LinkedList<T>

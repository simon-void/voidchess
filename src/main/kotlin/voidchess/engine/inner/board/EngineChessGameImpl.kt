package voidchess.engine.inner.board

import voidchess.common.board.*
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.FigureType
import voidchess.common.figures.King
import voidchess.common.board.Memento
import voidchess.common.board.doesLatestMementoOccurThreeTimes
import voidchess.engine.inner.evaluation.SearchTreePruner


internal class EngineChessGameImpl private constructor(
    override val startConfig: StartConfig,
    private val board: ChessBoard,
    private val mementoStack: ArrayList<Memento>,
    private val numberStack: NumberStack
): EngineChessGame, StaticChessBoard by board {
    private val extendedMovesPlayed = ArrayDeque<ExtendedMove>()
    private var numberOfMovesWithoutHit: Int = 0
    override val latestExtendedMove: ExtendedMove get() = extendedMovesPlayed.last()

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
            return if (numberOfMovesWithoutHit == 100) {
                MoveResultType.FIFTY_MOVES_NO_HIT
            } else MoveResultType.NO_END
        }

    private val isDrawBecauseOfThreeTimesSamePosition: Boolean
        get() = mementoStack.doesLatestMementoOccurThreeTimes(numberOfMovesWithoutHit)

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
        ArrayList(other.mementoStack),
        NumberStack(other.numberStack)
    ) {
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit
    }

    constructor(
        startConfig: StartConfig = StartConfig.ClassicConfig,
        movesSoFar: Iterable<Move> = emptyList()
    ) : this(
        startConfig,
        ArrayChessBoard(startConfig),
        ArrayList<Memento>(64),
        NumberStack()
    ) {
        numberOfMovesWithoutHit = startConfig.numberOfMovesWithoutHit
        for (i in 0 ..< numberOfMovesWithoutHit) numberStack.noFigureHit()

        memorizeGame()

        for(move in movesSoFar) {
            move(move)
        }
    }

    override fun <T> withMove(move: Move, workWithGameAfterMove: (MoveResultType) -> T): T {
        val moveResult = move(move)
        val result = workWithGameAfterMove(moveResult)
        undo()
        return result
    }

    private fun move(move: Move): MoveResultType {
        val extendedMove = board.move(move)
        extendedMovesPlayed.addLast(extendedMove)

        if (extendedMove.hasHitFigure) {
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
        extendedMovesPlayed.removeLast()
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

    override fun countReachableMoves(): MoveCounter {
        var whiteCount = 0
        var blackCount = 0

        board.forAllFigures { figure->
            when( figure.type ) {
                FigureType.KING, FigureType.PAWN -> {}
                else -> {
                    val count = figure.countReachableMoves(board)
                    if(figure.isWhite) {
                        whiteCount += count
                    }else{
                        blackCount += count
                    }
                }
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
            for (i in 1 ..< numberOfInstances) {
                val copy = EngineChessGameImpl(this, startConfig, moves)
                gameInstances.add(copy)
            }
        }
        return gameInstances
    }

    private fun memorizeGame() {
        mementoStack.add(Memento(board))
    }
}

private class NumberStack {
    private var numberStack: IntArray
    private var index: Int = 0

    constructor() {
        numberStack = IntArray(50)
        init()
    }

    //copy-Constructor
    constructor(other: NumberStack) {
        numberStack = IntArray(other.index + SearchTreePruner.MAX_SEARCH_DEPTH)
        System.arraycopy(other.numberStack, 0, numberStack, 0, numberStack.size)
        index = other.index
    }

    fun init() {
        for (i in numberStack.indices) numberStack[i] = 0
        index = 0
    }

    fun noFigureHit() {
        numberStack[index]++
    }

    fun figureHit() {
        ensureCapacity()
        index++
    }

    fun undo(): Int {
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

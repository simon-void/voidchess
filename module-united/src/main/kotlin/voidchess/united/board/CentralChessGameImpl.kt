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

private class NumberStack {
    private var numberStack: IntArray
    private var index: Int = 0

    init {
        numberStack = IntArray(50)
        init()
    }

    fun init() {
        for (i in numberStack.indices) numberStack[i] = 0
        index = 0
    }

    fun didNotCatchFigureOrMovePawn() {
        numberStack[index]++
    }

    fun didCatchFigureOrMovePawn() {
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

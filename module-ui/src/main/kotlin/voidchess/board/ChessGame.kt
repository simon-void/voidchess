package voidchess.board

import voidchess.board.move.ExtendedMove
import voidchess.board.move.MoveResult
import voidchess.board.move.PawnPromotion
import voidchess.common.board.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.engine.board.move.*
import voidchess.engine.figures.*
import voidchess.figures.Bishop
import voidchess.figures.Figure
import voidchess.figures.FigureFactory
import voidchess.figures.King
import voidchess.figures.Knight
import voidchess.figures.Pawn
import voidchess.figures.Queen
import voidchess.figures.Rook
import java.util.*
import kotlin.math.abs


class ChessGame private constructor(
    private val board: ChessBoard,
    override val startConfig: StartConfig,
    private val mementoStack: LinkedList<Memento>,
    private val extendedMoveStack: LinkedList<ExtendedMove>,
    private val numberStack: NumberStack,
    private var supervisor: ChessGameSupervisor
): ChessGameInterface, BasicChessBoard by board {
    private val figureFactory = FigureFactory
    private var numberOfMovesWithoutHit: Int = 0
    private var figureCount: Int = 32
    private var hasHitFigure: Boolean = false
    private var whiteTurn: Boolean = true

    override val isWhiteTurn: Boolean
        get() = whiteTurn

    override fun isCheck(isWhiteInCheck: Boolean) = board.getCachedAttackLines(isWhiteInCheck).isCheck

    private val isEnd: MoveResult
        get() {
            if (noMovesLeft(whiteTurn)) {
                return if (isCheck(whiteTurn)) {
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
        get() = getHistory(4)

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
                if (figure is Pawn
                    || figure is Rook
                    || figure is Queen
                ) {
                    return false
                } else if (figure is Bishop) {
                    if (figure.isWhite)
                        numberOfWhiteBishops++
                    else
                        numberOfBlackBishops++
                } else if (figure is Knight) {
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
        ArrayChessBoard(),
        StartConfig.ClassicConfig,
            LinkedList<Memento>(),
            LinkedList<ExtendedMove>(),
        NumberStack(),
            supervisor
    ) {
        initGame()
    }

    /**
     * for unit-tests
     */
    constructor(game_description: String) : this(ChessGameSupervisorDummy, game_description)

    /**
     * for unit-tests
     */
    @JvmOverloads constructor(initialPosition: Int = 518) : this(ChessGameSupervisorDummy, initialPosition)

    /**
     * for unit-tests
     */
    private constructor(supervisor: ChessGameSupervisor, game_description: String) : this(
        ArrayChessBoard(game_description),
        StartConfig.ManualConfig,
        LinkedList<Memento>(),
        LinkedList<ExtendedMove>(),
        NumberStack(),
        supervisor
    ) {
        val st = StringTokenizer(game_description, " ", false)
        whiteTurn = st.nextToken() == "white"
        numberOfMovesWithoutHit = Integer.parseInt(st.nextToken())
        for (i in 0 until numberOfMovesWithoutHit) numberStack.noFigureHit()

        figureCount = 0
        while (st.hasMoreTokens()) {
            figureCount++
            st.nextToken()
        }

        memorizeGame()
        hasHitFigure = numberOfMovesWithoutHit == 0
    }

    /**
     * for unit-tests
     */
    private constructor(
        supervisor: ChessGameSupervisor,
        initialPosition: Int
    ) : this(
        ArrayChessBoard().apply { init(initialPosition) },
        StartConfig.Chess960Config(initialPosition),
        LinkedList<Memento>(),
        LinkedList<ExtendedMove>(),
        NumberStack(),
        supervisor
    ) {
        memorizeGame()
    }

    override fun useSupervisor(supervisor: ChessGameSupervisor) {
        this.supervisor = supervisor
    }

    override fun suspendInteractiveSupervisor(): ChessGameSupervisor {
        val normalSupervisor = supervisor
        supervisor = ChessGameSupervisorDummy
        return normalSupervisor
    }

    private fun setFigure(pos: Position, figure: Figure?) {
        if (figure == null) {
            board.clearFigure(pos)
        } else {
            board.setFigure(pos, figure)
        }
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
        var rewritableMove = move
        assert(!isFreeArea(rewritableMove.from)) { "the move moves a null value:$rewritableMove" }
        assert(board.getFigure(rewritableMove.from).isWhite == whiteTurn) { "figure to be moved has wrong color" }

        val castlingRook = extractCastlingRook(rewritableMove)
        //im Fall der Castling wird der Zug jetzt so umgebogen,
        //das move.to dem Zielfeld des Königs entspricht
        //und nicht dem Feld des Castlingturms
        if (castlingRook != null) {
            val row = rewritableMove.to.row
            val column = if (rewritableMove.to.column - rewritableMove.from.column > 0) 6 else 2
            rewritableMove = Move[rewritableMove.from, Position[row, column]]
        }

        val hitPawn = handleEnpasent(rewritableMove)
        val hitFigure = moveFigure(rewritableMove)
        val wasWhiteTurn = !whiteTurn

        informFiguresOfMove(wasWhiteTurn, rewritableMove)
        reinsertCastlingRook(castlingRook, rewritableMove.to)
        val pawnTransformed = handlePawnTransformation(rewritableMove)

        memorizeGame()
        memorizeMove(rewritableMove, wasWhiteTurn, pawnTransformed, hitPawn, castlingRook, hitFigure)

        return isEnd
    }

    private fun moveFigure(move: Move): Figure? {
        val toNotEqualsFrom = move.to.notEqualsPosition(move.from)//für manche Schach960castlingn true
        hasHitFigure = !board.isFreeArea(move.to) && toNotEqualsFrom  //Enpasent wird nicht beachtet
        val fromFigure = getFigureOrNull(move.from)

        if (hasHitFigure) {
            numberStack.figureHit()
            numberOfMovesWithoutHit = 0
            figureCount--
        } else {
            numberStack.noFigureHit()
            numberOfMovesWithoutHit++
        }

        var hitFigure: Figure? = null
        if (toNotEqualsFrom) {
            hitFigure = getFigureOrNull(move.to)
            setFigure(move.to, fromFigure)
            setFigure(move.from, null)
        }

        whiteTurn = !whiteTurn

        return hitFigure
    }

    override fun hasHitFigure(): Boolean {
        return hasHitFigure
    }

    private fun handleEnpasent(move: Move): Pawn? {
        if (board.getFigure(move.from) is Pawn
            && move.from.column != move.to.column
            && isFreeArea(move.to)
        ) {
            val pawnToBeHit = Position[move.from.row, move.to.column]
            val pawn = getFigureOrNull(pawnToBeHit) as Pawn?
            setFigure(pawnToBeHit, null)
            figureCount--
            numberOfMovesWithoutHit = -1            //die Variable wird dann von move Figure auf 0 gesetzt
            return pawn
        }
        return null
    }

    private fun extractCastlingRook(move: Move): Rook? {
        val movingFigure = board.getFigure(move.from)
        if (movingFigure !is King) return null

        val castlingRook = getFigureOrNull(move.to)
        if (castlingRook != null && castlingRook.isWhite == movingFigure.isWhite) {
            setFigure(move.to, null)    // the rook is taken off the board temporarily
            movingFigure.performCastling()
            return castlingRook as Rook?
        }
        return null
    }

    private fun reinsertCastlingRook(castlingRook: Rook?, moveTo: Position) {
        if (castlingRook != null) {
            val rookFrom = castlingRook.position
            val rookTo = if (moveTo.column == 6)
                Position[moveTo.row, 5]
            else
                Position[moveTo.row, 3]
            castlingRook.figureMoved(Move[rookFrom, rookTo])
            setFigure(rookTo, castlingRook)
        }
    }

    private fun handlePawnTransformation(move: Move): Boolean {
        if (board.getFigure(move.to) is Pawn) {
            if (move.to.row == 0 || move.to.row == 7) {
                val figure = supervisor.askForPawnChange(move.to)
                val isWhite = move.to.row == 7
                val newFigure: Figure
                newFigure = when (figure) {
                    PawnPromotion.QUEEN -> figureFactory.getQueen(move.to, isWhite)
                    PawnPromotion.ROOK -> figureFactory.getRook(move.to, isWhite)
                    PawnPromotion.KNIGHT -> figureFactory.getKnight(move.to, isWhite)
                    PawnPromotion.BISHOP -> figureFactory.getBishop(move.to, isWhite)
                }
                setFigure(move.to, newFigure)
                return true
            }
        }
        return false
    }

    private fun informFiguresOfMove(whiteTurn: Boolean, move: Move) {
        board.forAllFiguresOfColor(whiteTurn) { figure ->
            figure.figureMoved(move)
        }
    }

    override fun undo() {
        whiteTurn = !whiteTurn
        numberOfMovesWithoutHit = numberStack.undo()
        mementoStack.removeLast()

        val lastExtMove = extendedMoveStack.removeLast()
        val lastMove = lastExtMove.move
        val activeFigure = board.getFigure(lastMove.to)
        setFigure(lastMove.from, activeFigure)
        if (!lastExtMove.isCastling || lastMove.from.notEqualsPosition(lastMove.to)) {
            setFigure(lastMove.to, lastExtMove.figureTaken)
        }
        activeFigure.undoMove(lastMove.from)

        if (lastExtMove.wasFigureTaken) {
            figureCount++
        }

        if (lastExtMove.isCastling) undoCastling(lastExtMove)
        if (lastExtMove.isEnPassant) undoEnPassant(lastExtMove)
        if (lastExtMove.isPawnTransformation) undoPawnTransformation(lastExtMove)
        rebuildPawnEnPassantCapability()
    }

    private fun undoCastling(lastExtMove: ExtendedMove) {
        val rook = lastExtMove.enpassantPawnOrCastlingRook as Rook
        val rookStartPos = rook.initialPosition
        val rookCurrentPos = rook.position

        setFigure(rookStartPos, rook)
        if (rookStartPos.notEqualsPosition(rookCurrentPos) && lastExtMove.move.from.notEqualsPosition(rookCurrentPos)) {
            setFigure(rookCurrentPos, null)
        }
        rook.undoMove(rookStartPos)
    }

    private fun undoEnPassant(lastExtMove: ExtendedMove) {
        val hitPawn = lastExtMove.enpassantPawnOrCastlingRook as Pawn
        val pawnPos = Position[lastExtMove.move.from.row, lastExtMove.move.to.column]
        setFigure(pawnPos, hitPawn)
        hitPawn.setCanBeHitByEnpasent()
    }

    private fun undoPawnTransformation(lastExtMove: ExtendedMove) {
        val pawnPos = lastExtMove.move.from
        val pawn = figureFactory.getPawn(pawnPos, lastExtMove.colorOfMove)
        setFigure(pawnPos, pawn)
    }

    private fun rebuildPawnEnPassantCapability() {
        if (extendedMoveStack.isEmpty()) return

        val newLatestMove = extendedMoveStack.last
        val figure = board.getFigure(newLatestMove.move.to)
        if (figure is Pawn && abs(newLatestMove.move.from.row - newLatestMove.move.to.row) == 2) {
            figure.setCanBeHitByEnpasent()
        }
    }

    override fun toString() = "${if (whiteTurn) "white" else "black"} $numberOfMovesWithoutHit $board"

    private fun getHistory(numberOfHalfMoves: Int) = extendedMoveStack.getLatestMoves(numberOfHalfMoves)

    override fun getCompleteHistory() = extendedMoveStack.getLatestMoves(extendedMoveStack.size)

    private fun initGame() = initGame(518)    //classic chess starting configuration

    override fun initGame(chess960: Int) {
        whiteTurn = true
        numberOfMovesWithoutHit = 0
        figureCount = 32
        mementoStack.clear()
        extendedMoveStack.clear()
        numberStack.init()

        board.init(chess960)

        memorizeGame()
    }

    fun equalsOther(other: ChessGame): Boolean {
        if (whiteTurn != other.whiteTurn) return false

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

    private fun memorizeGame() = mementoStack.addLast(Memento(board, whiteTurn))

    private fun memorizeMove(move: Move,
                             whiteMove: Boolean,
                             pawnTransformed: Boolean,
                             hitPawn: Pawn?,
                             castlingRook: Rook?,
                             hitFigure: Figure?) {
        val hitsEnPassant = hitPawn != null
        val isCastling = castlingRook != null
        var castlingRookOrEnPassantPawn: Figure? = hitPawn
        if (isCastling) {
            castlingRookOrEnPassantPawn = castlingRook
        }
        val extendedMove = ExtendedMove(
            move,
            hitFigure,
            castlingRookOrEnPassantPawn,
            whiteMove,
            isCastling,
            hitsEnPassant,
            pawnTransformed
        )
        extendedMoveStack.addLast(extendedMove)
    }

    override fun getLastExtendedMove(): ExtendedMove = extendedMoveStack.last
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
    private var numberStack: IntArray = IntArray(50)
    private var index: Int = 0

    init {
        init()
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

fun LinkedList<ExtendedMove>.getLatestMoves(count: Int): String {
    assert(count > 0)

    val minIndex = (size - count).coerceAtLeast(0)
    return subList(fromIndex = minIndex, toIndex = size).joinToString(separator = ",") { it.toString() }
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

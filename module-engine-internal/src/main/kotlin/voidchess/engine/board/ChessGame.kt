package voidchess.engine.board

import voidchess.common.board.StartConfig
import voidchess.common.board.move.*
import voidchess.common.board.move.PawnPromotion.*
import voidchess.engine.board.move.ExtendedMove
import voidchess.engine.figures.*
import voidchess.engine.player.ki.evaluation.SearchTreePruner
import java.util.*
import kotlin.math.abs


internal class ChessGame private constructor(
    private val board: ChessBoard,
    override val startConfig: StartConfig,
    private val mementoStack: LinkedList<Memento>,
    private val extendedMoveStack: LinkedList<ExtendedMove>,
    private val numberStack: NumberStack,
    private var supervisor: ChessGameSupervisor
): ChessGameInterface, BasicChessBoard by board {
    private var numberOfMovesWithoutHit: Int = 0
    private var figureCount: Int = 32
    override var hasHitFigure: Boolean = false
    override var isWhiteTurn: Boolean = true



    override fun isCheck(isWhiteInCheck: Boolean) = board.getCachedAttackLines(isWhiteInCheck).isCheck

    private val isEnd: MoveResult
        get() {
            if (noMovesLeft(isWhiteTurn)) {
                return if (isCheck(isWhiteTurn)) {
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
     * copy-constructor
     */
    private constructor(other: ChessGame, desc: String): this(
            ArrayChessBoard(desc),
            other.startConfig,
            other.mementoStack.shallowCopy(),
            other.extendedMoveStack.shallowCopy(),
            NumberStack(other.numberStack),
            ChessGameSupervisorDummy
    ) {
        hasHitFigure = other.hasHitFigure
        isWhiteTurn = other.isWhiteTurn
        numberOfMovesWithoutHit = other.numberOfMovesWithoutHit
        figureCount = other.figureCount
    }

    /**
     * for unit-tests
     */
    constructor(game_description: String) : this(ChessGameSupervisorDummy, game_description)

    /**
     * for unit-tests
     */
    @JvmOverloads constructor(initialPosition: Int = 518, vararg moves: String) : this(ChessGameSupervisorDummy, initialPosition) {
        for(move in moves) {
            val result = move(Move.byCode(move))
            check(result == MoveResult.NO_END) { "board is not supposed to end via these moves but did. end by $result" }
        }
    }

    /**
     * for unit-tests
     */
    private constructor(supervisor: ChessGameSupervisor, game_description: String): this(
            ArrayChessBoard(game_description),
            StartConfig.ManualConfig(game_description, game_description.startsWith("white ")),
            LinkedList<Memento>(),
            LinkedList<ExtendedMove>(),
            NumberStack(),
            supervisor
    ) {
        val st = StringTokenizer(game_description, " ", false)
        isWhiteTurn = st.nextToken() == "white"
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
    private constructor(supervisor: ChessGameSupervisor,
                        initialPosition: Int): this(
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

    fun getLastMove() = if (extendedMoveStack.isEmpty()) null else extendedMoveStack.last

    private fun setFigure(pos: Position, figure: Figure?) {
        if (figure == null) {
            board.clearPos(pos)
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
        val movingFigure: Figure = board.getFigure(move.from)
        val toFigure: Figure? = board.getFigureOrNull(move.to)
        assert(movingFigure.isWhite == isWhiteTurn) { "figure to be moved has wrong color" }

        val extendedMove: ExtendedMove = when(movingFigure.type) {
            FigureType.KING -> {
                if(toFigure!=null && toFigure.isWhite==movingFigure.isWhite) {
                    val isKingSideCastling = move.from.column<move.to.column
                    val kingToColumn = if (isKingSideCastling) 6 else 2
                    val rookToColumn = if (isKingSideCastling) 5 else 3
                    ExtendedMove.Castling(move, Move[move.from, Position[move.to.row, kingToColumn]], Move[move.to, Position[move.to.row, rookToColumn]])
                }else{
                    ExtendedMove.Normal(move, toFigure)
                }
            }
            FigureType.PAWN -> {
                if(move.to.row==0 || move.to.row==7) {
                    ExtendedMove.Promotion(move, movingFigure, board.getFigureOrNull(move.to))
                }else if(abs(move.from.row-move.to.row)==2) {
                    ExtendedMove.PawnDoubleJump(move,  movingFigure as Pawn)
                }else if(move.from.column!=move.to.column && toFigure==null) {
                    val pawnTakenByEnpassant = board.getFigure(Position[move.from.row, move.to.column])
                    ExtendedMove.Enpassant(move, pawnTakenByEnpassant)
                }else{
                    ExtendedMove.Normal(move, toFigure)
                }
            }
            else -> ExtendedMove.Normal(move, toFigure)
        }

        moveFigure(extendedMove)

        memorizeGame()
        memorizeMove(extendedMove)

        return isEnd
    }

    private fun moveFigure(extendedMove: ExtendedMove) {
        val movingFigure = board.clearFigure(extendedMove.move.from)
        when(extendedMove) {
            is ExtendedMove.Castling -> {
                val castlingRook = board.clearFigure(extendedMove.rookMove.from)
                board.setFigure(extendedMove.rookMove.to, castlingRook)
                board.setFigure(extendedMove.kingMove.to, movingFigure)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.kingMove)
                castlingRook.figureMoved(extendedMove.rookMove)
                (movingFigure as King).didCastling = true
            }
            is ExtendedMove.Promotion -> {
                val toPos: Position = extendedMove.move.to
                val promotedPawn: Figure = when (supervisor.askForPawnChange(toPos)) {
                    QUEEN -> getQueen(toPos, movingFigure.isWhite)
                    ROOK -> getRook(toPos, movingFigure.isWhite)
                    KNIGHT -> getKnight(toPos, movingFigure.isWhite)
                    BISHOP -> getBishop(toPos, movingFigure.isWhite)
                }
                setFigure(toPos, promotedPawn)
                // the newly created figure is already aware of its position and doesn't need to be informed
            }
            is ExtendedMove.Enpassant -> {
                board.setFigure(extendedMove.move.to, movingFigure)
                board.clearPos(extendedMove.pawnTaken.position)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.move)
            }
            is ExtendedMove.PawnDoubleJump -> {
                board.setFigure(extendedMove.move.to, movingFigure)
                // inform the involved figure(s) of the move
                extendedMove.pawn.let { pawn: Pawn ->
                    pawn.figureMoved(extendedMove.move)
                    pawn.canBeHitEnpassant = true
                }
            }
            is ExtendedMove.Normal -> {
                board.setFigure(extendedMove.move.to, movingFigure)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.move)
            }
        }.let {}

        // remove possible susceptibility to being hit by enpassant
        extendedMoveStack.lastOrNull()?.let { previousExtendedMove->
            if(previousExtendedMove is ExtendedMove.PawnDoubleJump) {
                previousExtendedMove.pawn.canBeHitEnpassant = false
            }
        }

        isWhiteTurn = !isWhiteTurn
        hasHitFigure = extendedMove.hasHitFigure.also { didHitFigure->
            if (didHitFigure) {
                numberStack.figureHit()
                numberOfMovesWithoutHit = 0
                figureCount--
            } else {
                numberStack.noFigureHit()
                numberOfMovesWithoutHit++
            }
        }
    }

    override fun undo() {
        isWhiteTurn = !isWhiteTurn
        numberOfMovesWithoutHit = numberStack.undo()
        mementoStack.removeLast()

        val lastExtMove: ExtendedMove = extendedMoveStack.removeLast()
        when(lastExtMove) {
            is ExtendedMove.Castling -> {
                val king = board.clearFigure(lastExtMove.kingMove.to) as King
                val rook = board.clearFigure(lastExtMove.rookMove.to)
                board.setFigure(lastExtMove.kingMove.from, king)
                board.setFigure(lastExtMove.rookMove.from, rook)
                // inform the involved figure(s) of the undo
                king.undoMove(lastExtMove.kingMove.from)
                rook.undoMove(lastExtMove.rookMove.from)
                king.didCastling = false
            }
            is ExtendedMove.Promotion -> {
                board.clearPos(lastExtMove.move.to)
                lastExtMove.figureTaken?.let { figureTaken->
                    board.setFigure(figureTaken.position, figureTaken)
                }
                board.setFigure(lastExtMove.move.from, lastExtMove.pawnPromoted)
                // no undo necessary because pawn's position was never updated
            }
            is ExtendedMove.Enpassant -> {
                val pawnMoved = board.clearFigure(lastExtMove.move.to)
                board.setFigure(lastExtMove.move.from, pawnMoved)
                lastExtMove.pawnTaken.let { pawnTaken->
                    board.setFigure(pawnTaken.position, pawnTaken)
                }
                // inform the involved figure(s) of the undo
                pawnMoved.undoMove(lastExtMove.move.from)
            }
            is ExtendedMove.PawnDoubleJump -> {
                lastExtMove.pawn.let { pawn ->
                    board.clearPos(lastExtMove.move.to)
                    board.setFigure(lastExtMove.move.from, pawn)
                    // inform the involved figure(s) of the undo
                    pawn.undoMove(lastExtMove.move.from)
                    pawn.canBeHitEnpassant=false
                }
            }
            is ExtendedMove.Normal -> {
                val movingFigure = board.clearFigure(lastExtMove.move.to)
                board.setFigure(lastExtMove.move.from, movingFigure)
                lastExtMove.figureTaken?.let { board.setFigure(lastExtMove.move.to, it) }
                // inform the involved figure(s) of the undo
                movingFigure.undoMove(lastExtMove.move.from)
            }
        }.let {}

        extendedMoveStack.lastOrNull()?.let { preLastExtMove->
            if(preLastExtMove is ExtendedMove.PawnDoubleJump) {
                preLastExtMove.pawn.canBeHitEnpassant = true
            }
        }

        if (lastExtMove.hasHitFigure) {
            figureCount++
        }
    }

    override fun toString() = "${if (isWhiteTurn) "white" else "black"} $numberOfMovesWithoutHit $board"

    private fun getHistory(numberOfHalfMoves: Int) = extendedMoveStack.getLatestMoves(numberOfHalfMoves)

    override fun getCompleteHistory() = extendedMoveStack.getLatestMoves(extendedMoveStack.size)

    private fun initGame() = initGame(518)    //classic chess starting configuration

    override fun initGame(chess960: Int) {
        isWhiteTurn = true
        numberOfMovesWithoutHit = 0
        figureCount = 32
        mementoStack.clear()
        extendedMoveStack.clear()
        numberStack.init()

        board.init(chess960)

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
            val gameDes = toString()
            for (i in 1 until neededInstances) {
                val copy = ChessGame(this, gameDes)
                gameInstances.add(copy)
            }
        }
        return gameInstances
    }

    private fun memorizeGame() = mementoStack.addLast(Memento(board, isWhiteTurn))

    private fun memorizeMove(
        extendedMove: ExtendedMove
    ) {
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

internal fun LinkedList<ExtendedMove>.getLatestMoves(count: Int): String {
    assert(count > 0)

    val minIndex = (size - count).coerceAtLeast(0)
    return subList(fromIndex = minIndex, toIndex = size).map { it.move }.joinToString(separator = ",") { it.toString() }
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

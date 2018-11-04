package voidchess.board

import voidchess.board.check.CheckSearch
import voidchess.board.move.*
import voidchess.figures.*
import voidchess.player.ki.evaluation.SimplePruner
import java.util.*


class ChessGame : ChessGameInterface {
    private val game: SimpleChessBoardInterface
    private val figureFactory: FigureFactory
    private val mementoStack: LinkedList<Memento>
    private val extendedMoveStack: LinkedList<ExtendedMove>
    private val numberStack: NumberStack
    private var numberOfMovesWithoutHit: Int = 0
    private var figureCount: Int = 0
    private var hasHitFigure: Boolean = false
    private var supervisor: ChessGameSupervisor
    private var standardGame = false
    private var whiteTurn: Boolean = false

    override val isStandardGame: Boolean
        get() = standardGame

    override val isWhiteTurn: Boolean
        get() = whiteTurn

    override val whiteKing: King
        get() = game.whiteKing

    override val blackKing: King
        get() = game.blackKing

    override fun isCheck(isWhiteInCheck: Boolean) = game.getCachedAttackLines(isWhiteInCheck).isCheck

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

            game.forAllFigures { figure ->
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
    constructor(supervisor: ChessGameSupervisor) {
        hasHitFigure = false
        this.supervisor = supervisor
        figureFactory = FigureFactory()
        mementoStack = LinkedList()
        extendedMoveStack = LinkedList()
        numberStack = NumberStack()
        game = SimpleArrayBoard()

        initGame()
    }

    /**
     * copy-constructor
     */
    private constructor(other: ChessGame, desc: String) {
        hasHitFigure = other.hasHitFigure
        supervisor = ChessGameSupervisorDummy
        figureFactory = FigureFactory()
        mementoStack = other.mementoStack.shallowCopy()
        extendedMoveStack = other.extendedMoveStack.shallowCopy()
        numberStack = NumberStack(other.numberStack)
        game = SimpleArrayBoard(desc)

        whiteTurn = other.whiteTurn
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
            if(result!= MoveResult.NO_END) {
                throw IllegalStateException("game is not supposed to end via these moves but did. end by $result")
            }
        }
    }

    /**
     * for unit-tests
     */
    private constructor(supervisor: ChessGameSupervisor, game_description: String) {
        this.supervisor = supervisor
        figureFactory = FigureFactory()
        mementoStack = LinkedList()
        extendedMoveStack = LinkedList()
        numberStack = NumberStack()

        val st = StringTokenizer(game_description, " ", false)
        whiteTurn = st.nextToken() == "white"
        numberOfMovesWithoutHit = Integer.parseInt(st.nextToken())
        for (i in 0 until numberOfMovesWithoutHit) numberStack.noFigureHit()

        figureCount = 0
        while (st.hasMoreTokens()) {
            figureCount++
            st.nextToken()
        }

        game = SimpleArrayBoard()
        game.init(game_description)

        memorizeGame()
        hasHitFigure = numberOfMovesWithoutHit == 0
    }

    /**
     * for unit-tests
     */
    private constructor(supervisor: ChessGameSupervisor,
                        initialPosition: Int) {
        this.supervisor = supervisor
        figureFactory = FigureFactory()
        mementoStack = LinkedList()
        extendedMoveStack = LinkedList()
        numberStack = NumberStack()

        whiteTurn = true
        hasHitFigure = false
        numberOfMovesWithoutHit = 0
        figureCount = 32

        game = SimpleArrayBoard()
        game.init(initialPosition)

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

    override fun isFreeArea(pos: Position): Boolean {
        return game.isFreeArea(pos)
    }

    override fun getFigureOrNull(pos: Position): Figure? {
        return game.getFigureOrNull(pos)
    }

    private fun setFigure(pos: Position, figure: Figure?) {
        if (figure == null) {
            game.clearFigure(pos)
        } else {
            game.setFigure(pos, figure)
        }
    }

    override fun isSelectable(pos: Position, whitePlayer: Boolean): Boolean {
        val figure = getFigureOrNull(pos)
        return figure!=null && figure.isWhite == whitePlayer && figure.isSelectable(game)
    }

    override fun isMovable(from: Position, to: Position, whitePlayer: Boolean): Boolean {
        val figure = getFigureOrNull(from)
        return figure!=null && figure.isWhite == whitePlayer && figure.isMovable(to, game)
    }

    override fun countFigures(): Int {
        return figureCount
    }

    override fun move(move: Move): MoveResult {
        var rewritableMove = move
        assert(!isFreeArea(rewritableMove.from)) { "the move moves a null value:" + rewritableMove.toString() }
        assert(game.getFigure(rewritableMove.from).isWhite == whiteTurn) { "figure to be moved has wrong color" }

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
        hasHitFigure = !isFreeArea(move.to) && toNotEqualsFrom  //Enpasent wird nicht beachtet
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
        if (game.getFigure(move.from).isPawn()
                && move.from.column != move.to.column
                && isFreeArea(move.to)) {
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
        val movingFigure = game.getFigure(move.from)
        if (!movingFigure.isKing()) return null

        val castlingRook = getFigureOrNull(move.to)
        if (castlingRook != null && castlingRook.isWhite == movingFigure.isWhite) {
            setFigure(move.to, null)    // the rook is taken off the board temporarily
            (movingFigure as King).performCastling()
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
        if (game.getFigure(move.to).isPawn()) {
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
        game.forAllFiguresOfColor(whiteTurn) { figure ->
            figure.figureMoved(move)
        }
    }

    override fun undo() {
        whiteTurn = !whiteTurn
        numberOfMovesWithoutHit = numberStack.undo()
        mementoStack.removeLast()

        val lastExtMove = extendedMoveStack.removeLast()
        val lastMove = lastExtMove.move
        val activeFigure = game.getFigure(lastMove.to)
        setFigure(lastMove.from, activeFigure)
        if (!lastExtMove.isCastling || lastMove.from.notEqualsPosition(lastMove.to)) {
            setFigure(lastMove.to, lastExtMove.figureTaken)
        }
        activeFigure.undoMove(lastMove.from)

        if (lastExtMove.wasFigureTaken) {
            figureCount++
        }

        if (lastExtMove.isCastling) undoCastling(lastExtMove)
        if (lastExtMove.isEnpassent) undoEnpassent(lastExtMove)
        if (lastExtMove.isPawnTransformation) undoPawnTransformation(lastExtMove)
        rebuildPawnEnpassentCapability()
    }

    private fun undoCastling(lastExtMove: ExtendedMove) {
        val rook = lastExtMove.enpassentPawnOrCastlingRook as Rook
        val rookStartPos = rook.initialPosition
        val rookCurrentPos = rook.position

        setFigure(rookStartPos, rook)
        if (rookStartPos.notEqualsPosition(rookCurrentPos) && lastExtMove.move.from.notEqualsPosition(rookCurrentPos)) {
            setFigure(rookCurrentPos, null)
        }
        rook.undoMove(rookStartPos)
    }

    private fun undoEnpassent(lastExtMove: ExtendedMove) {
        val hitPawn = lastExtMove.enpassentPawnOrCastlingRook as Pawn
        val pawnPos = Position[lastExtMove.move.from.row, lastExtMove.move.to.column]
        setFigure(pawnPos, hitPawn)
        hitPawn.setCanBeHitByEnpasent()
    }

    private fun undoPawnTransformation(lastExtMove: ExtendedMove) {
        val pawnPos = lastExtMove.move.from
        val pawn = figureFactory.getPawn(pawnPos, lastExtMove.colorOfMove)
        setFigure(pawnPos, pawn)
    }

    private fun rebuildPawnEnpassentCapability() {
        if (extendedMoveStack.isEmpty()) return

        val newLatestMove = extendedMoveStack.last
        val figure = game.getFigure(newLatestMove.move.to)
        if (figure.isPawn() && Math.abs(newLatestMove.move.from.row - newLatestMove.move.to.row) == 2) {
            (figure as Pawn).setCanBeHitByEnpasent()
        }
    }

    override fun toString() = "${if (whiteTurn) "white" else "black"} $numberOfMovesWithoutHit $game"

    private fun getHistory(numberOfHalfMoves: Int) = extendedMoveStack.getLatestMoves(numberOfHalfMoves)

    override fun getCompleteHistory() = extendedMoveStack.getLatestMoves(extendedMoveStack.size)

    private fun initGame() = initGame(518)    //classic chess starting configuration

    override fun initGame(chess960: Int) {
        standardGame = chess960 == 518
        whiteTurn = true
        numberOfMovesWithoutHit = 0
        figureCount = 32
        mementoStack.clear()
        extendedMoveStack.clear()
        numberStack.init()

        game.init(chess960)

        memorizeGame()
    }

    fun equals(other: ChessGame): Boolean {
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
        game.forAllFiguresOfColor(caseWhite) { figure ->
            if (figure.isSelectable(game)) {
                return false
            }
        }
        return true
    }

    override fun getPossibleMoves(possibleMoves: MutableList<Move>) {
        game.forAllFiguresOfColor(whiteTurn) {figure ->
            figure.getPossibleMoves(game, possibleMoves)
        }
    }

    override fun countReachableMoves(): Pair<Int, Int> {
        var whiteCount = 0
        var blackCount = 0

        game.forAllFigures {figure->
            val count = figure.countReachableMoves(game)
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

    private fun memorizeGame() = mementoStack.addLast(Memento(game, whiteTurn))

    private fun memorizeMove(move: Move,
                             whiteMove: Boolean,
                             pawnTransformed: Boolean,
                             hitPawn: Pawn?,
                             castlingRook: Rook?,
                             hitFigure: Figure?) {
        val hitsEnpassent = hitPawn != null
        val isCastling = castlingRook != null
        var castlingRookOrEnpassentPawn: Figure? = hitPawn
        if (isCastling) {
            castlingRookOrEnpassentPawn = castlingRook
        }
        val extendedMove = ExtendedMove(
                move,
                hitFigure,
                castlingRookOrEnpassentPawn,
                whiteMove,
                isCastling,
                hitsEnpassent,
                pawnTransformed)
        extendedMoveStack.addLast(extendedMove)
    }

    override fun getLastExtendedMove(): ExtendedMove = extendedMoveStack.last
}

private class Memento constructor(game: BasicChessGameInterface, private val isWhite: Boolean) {
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

    fun equals(other: Memento): Boolean {
        return isWhite == other.isWhite && Arrays.equals(compressedBoard, other.compressedBoard)
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
        numberStack = IntArray(other.index + SimplePruner.MAX_SEARCH_DEPTH)
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

fun LinkedList<ExtendedMove>.getLatestMoves(count: Int): String {
    assert(count > 0)

    val minIndex = Math.max(0, size - count)
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
        if(memento.equals(lastMemento)) {
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
fun <T> LinkedList<T>.shallowCopy() = clone() as LinkedList<T>

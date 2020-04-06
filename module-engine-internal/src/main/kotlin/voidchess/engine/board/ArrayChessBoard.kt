package voidchess.engine.board

import voidchess.engine.board.check.AttackLines
import voidchess.engine.board.check.CheckSearch
import voidchess.engine.board.check.checkAttackLines
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.helper.splitAndTrim
import voidchess.engine.board.move.ExtendedMove
import voidchess.engine.figures.*
import java.util.*
import kotlin.math.abs

internal class ArrayChessBoard constructor() : ChessBoard {
    private val game: Array<Figure?> = arrayOfNulls(64)
    // TODO use kotlin.ArrayDeque if that is no longer experimental
    private val extendedMoveStack = ArrayDeque<ExtendedMove>(16)

    // the alternative to creating dummy instances for white and black king is 'lateinit'
    // but that would come with a null-check on each access
    override var whiteKing: King = getKing(Position.byCode("a1"), true)
    override var blackKing: King = getKing(Position.byCode("a8"), false)

    private var calculatedWhiteCheck: Boolean = false
    private var calculatedBlackCheck: Boolean = false
    private var isWhiteCheck: Boolean = false
    private var isBlackCheck: Boolean = false
    private var cachedAttackLines: AttackLines? = null

    init {
        init()
    }

    //for testing
    constructor(des: String) : this() {
        init(des)
    }

    private fun clearCheckComputation() {
        calculatedWhiteCheck = false
        calculatedBlackCheck = false
        cachedAttackLines = null
    }

    override fun isCheck(isWhite: Boolean): Boolean {
        return if (isWhite) {
            if (!calculatedWhiteCheck) {
                isWhiteCheck = CheckSearch.isCheck(this, whiteKing)
                calculatedWhiteCheck = true
            }
            isWhiteCheck
        } else {
            if (!calculatedBlackCheck) {
                isBlackCheck = CheckSearch.isCheck(this, blackKing)
                calculatedBlackCheck = true
            }
            isBlackCheck
        }
    }

    override fun getCachedAttackLines(isWhite: Boolean): AttackLines {
        val scopedAttackLines: AttackLines = cachedAttackLines ?: checkAttackLines(this, isWhite)
        cachedAttackLines = scopedAttackLines
        return scopedAttackLines
    }

    override fun init() {
        clear()
        var pos: Position

        for (i in 0..7) {
            pos = Position[1, i]
            setFigure(pos, getPawn(pos, true))
            pos = Position[6, i]
            setFigure(pos, getPawn(pos, false))
        }
        pos = Position.byCode("a1")
        setFigure(pos, getRook(pos, true))
        pos = Position.byCode("h1")
        setFigure(pos, getRook(pos, true))
        pos = Position.byCode("b1")
        setFigure(pos, getKnight(pos, true))
        pos = Position.byCode("g1")
        setFigure(pos, getKnight(pos, true))
        pos = Position.byCode("c1")
        setFigure(pos, getBishop(pos, true))
        pos = Position.byCode("f1")
        setFigure(pos, getBishop(pos, true))
        pos = Position.byCode("d1")
        setFigure(pos, getQueen(pos, true))
        pos = Position.byCode("e1")
        whiteKing = getKing(pos, true)
        setFigure(pos, whiteKing)

        pos = Position.byCode("a8")
        setFigure(pos, getRook(pos, false))
        pos = Position.byCode("h8")
        setFigure(pos, getRook(pos, false))
        pos = Position.byCode("b8")
        setFigure(pos, getKnight(pos, false))
        pos = Position.byCode("g8")
        setFigure(pos, getKnight(pos, false))
        pos = Position.byCode("c8")
        setFigure(pos, getBishop(pos, false))
        pos = Position.byCode("f8")
        setFigure(pos, getBishop(pos, false))
        pos = Position.byCode("d8")
        setFigure(pos, getQueen(pos, false))
        pos = Position.byCode("e8")
        blackKing = getKing(pos, false)
        setFigure(pos, blackKing)
    }

    override fun init(chess960: Int) {
        var code960Code = chess960
        assert(code960Code in 0..959) { "chess960 out of bounds. Should be 0-959, is $code960Code" }

        clear()
        var foundWhiteKing = false
        var foundBlackKing = false
        var pos: Position

        // pawn positions is always the same
        for (i in 0..7) {
            pos = Position[1, i]
            setFigure(pos, getPawn(pos, true))
            pos = Position[6, i]
            setFigure(pos, getPawn(pos, false))
        }

        // first bishop
        var rest = code960Code % 4
        var row = rest * 2 + 1
        code960Code /= 4

        pos = Position[0, row]
        setFigure(pos, getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, getBishop(pos, false))

        // second bishop
        rest = code960Code % 4
        row = rest * 2
        code960Code /= 4

        pos = Position[0, row]
        setFigure(pos, getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, getBishop(pos, false))

        // queen
        rest = code960Code % 6
        row = getFreeRow(rest)
        code960Code /= 6

        pos = Position[0, row]
        setFigure(pos, getQueen(pos, true))
        pos = Position[7, row]
        setFigure(pos, getQueen(pos, false))

        val otherFigures = getFigureArray(code960Code)

        for (figureName in otherFigures) {
            // always into the first free column
            row = getFreeRow(0)
            pos = Position[0, row]
            var figure = createFigure(figureName, true, pos)
            setFigure(pos, figure)
            if (figure is King) {
                whiteKing = figure
                foundWhiteKing = true
            }
            pos = Position[7, row]
            figure = createFigure(figureName, false, pos)
            setFigure(pos, figure)
            if (figure is King) {
                blackKing = figure
                foundBlackKing = true
            }
        }

        require(foundWhiteKing) {"no white king for chess960 configuration $chess960"}
        require(foundBlackKing) {"no black king for chess960 configuration $chess960"}
    }

    override fun init(des: String) {
        clear()
        var foundWhiteKing = false
        var foundBlackKing = false

        val iter = des.splitAndTrim(' ').iterator()
        iter.next()
        iter.next()

        while (iter.hasNext()) {
            val figureDescription = iter.next()
            val pos = getPositionOfCodedFigure(figureDescription)
            val figure = getFigureByString(figureDescription)
            if(figure is King) {
                if(figure.isWhite) {
                    whiteKing = figure

                    require(!foundWhiteKing) {"more than one white king in description [$des]"}
                    foundWhiteKing = true
                }else{
                    blackKing = figure

                    require(!foundBlackKing) {"more than one black king in description [$des]"}
                    foundBlackKing = true
                }
            }
            require(getFigureOrNull(pos)==null) {"two figures at same position $pos"}
            setFigure(pos, figure)
        }
        require(foundWhiteKing) {"no white king in description [$des]"}
        require(foundBlackKing) {"no black king in description [$des]"}
    }

    private fun getPositionOfCodedFigure(figure_description: String): Position {
        val tokens = figure_description.split('-')
        return Position.byCode(tokens[2])
    }

    private fun getFigureArray(index: Int) = when (index) {
        0 -> arrayOf("Knight", "Knight", "Rook", "King", "Rook")
        1 -> arrayOf("Knight", "Rook", "Knight", "King", "Rook")
        2 -> arrayOf("Knight", "Rook", "King", "Knight", "Rook")
        3 -> arrayOf("Knight", "Rook", "King", "Rook", "Knight")
        4 -> arrayOf("Rook", "Knight", "Knight", "King", "Rook")
        5 -> arrayOf("Rook", "Knight", "King", "Knight", "Rook")
        6 -> arrayOf("Rook", "Knight", "King", "Rook", "Knight")
        7 -> arrayOf("Rook", "King", "Knight", "Knight", "Rook")
        8 -> arrayOf("Rook", "King", "Knight", "Rook", "Knight")
        9 -> arrayOf("Rook", "King", "Rook", "Knight", "Knight")
        else -> throw IllegalArgumentException("index should be between [0-9] but is $index")
    }

    private fun createFigure(name: String, isWhite: Boolean, pos: Position) = when (name) {
        "Rook" -> getRook(pos, isWhite)
        "Knight" -> getKnight(pos, isWhite)
        "King" -> getKing(pos, isWhite)
        else -> throw IllegalStateException("unknown figure: $name")
    }

    private fun getFreeRow(index: Int): Int {
        assert(index in 0..7)

        var counter = 0
        for (row in 0..7) {
            if (isFreeArea(Position[0, row])) {
                if (index == counter)
                    return row
                else
                    counter++
            }
        }
        throw RuntimeException("No free Position with index $index found")
    }

    private fun clear() {
        clearCheckComputation()
        extendedMoveStack.clear()
        for (linearIndex in 0..63) {
            game[linearIndex] = null
        }
    }

    private fun setFigure(pos: Position, figure: Figure) {
        game[pos.index] = figure
    }

    private fun clearFigure(pos: Position): Figure {
        val figure: Figure = game[pos.index] ?: throw IllegalStateException("position $pos doesn't contain a figure to clear")
        game[pos.index] = null
        return figure
    }

    private fun clearPos(pos: Position) {
        game[pos.index] = null
    }

    override fun simulateSimplifiedMove(
        figure: Figure,
        warpTo: Position,
        query: (BasicChessBoard) -> Boolean
    ): Boolean {
        fun move(figure: Figure, to: Position): Figure? {
            val move = Move[figure.position, to]
            game[figure.position.index] = null
            val figureTaken = game[to.index]
            game[to.index] = figure
            figure.figureMoved(move)
            return figureTaken
        }
        fun undoMove(figure: Figure, from: Position, figureTaken: Figure?) {
            game[figure.position.index] = figureTaken
            game[from.index] = figure
            figure.undoMove(from)
        }

        val fromPos = figure.position
        val figureTaken = move(figure, warpTo)
        // execute the query with figure on the new position
        val result = query(this)
        // move the figure back to it's original position
        undoMove(figure, fromPos, figureTaken)
        return result
    }

    override fun historyToString(numberOfHalfMoves: Int?) = extendedMoveStack.getLatestMoves(numberOfHalfMoves)

    /**
     * @return true if a figure got hit
     */
    override fun move(
        move: Move,
        supervisor: ChessGameSupervisor
    ): Boolean {
        clearCheckComputation()

        val movingFigure: Figure = clearFigure(move.from)
        val toFigure: Figure? = getFigureOrNull(move.to)

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
                    ExtendedMove.Promotion(move, movingFigure, getFigureOrNull(move.to))
                }else if(abs(move.from.row-move.to.row) ==2) {
                    ExtendedMove.PawnDoubleJump(move,  movingFigure as Pawn)
                }else if(move.from.column!=move.to.column && toFigure==null) {
                    val pawnTakenByEnpassant = getFigure(Position[move.from.row, move.to.column])
                    ExtendedMove.Enpassant(move, pawnTakenByEnpassant)
                }else{
                    ExtendedMove.Normal(move, toFigure)
                }
            }
            else -> ExtendedMove.Normal(move, toFigure)
        }

        when(extendedMove) {
            is ExtendedMove.Castling -> {
                val castlingRook = clearFigure(extendedMove.rookMove.from)
                setFigure(extendedMove.rookMove.to, castlingRook)
                setFigure(extendedMove.kingMove.to, movingFigure)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.kingMove)
                castlingRook.figureMoved(extendedMove.rookMove)
                (movingFigure as King).didCastling = true
            }
            is ExtendedMove.Promotion -> {
                val toPos: Position = extendedMove.move.to
                val promotedPawn: Figure = when (supervisor.askForPawnChange(toPos)) {
                    PawnPromotion.QUEEN -> getQueen(toPos, movingFigure.isWhite)
                    PawnPromotion.ROOK -> getRook(toPos, movingFigure.isWhite)
                    PawnPromotion.KNIGHT -> getKnight(toPos, movingFigure.isWhite)
                    PawnPromotion.BISHOP -> getBishop(toPos, movingFigure.isWhite)
                }
                setFigure(toPos, promotedPawn)
                // the newly created figure is already aware of its position and doesn't need to be informed
            }
            is ExtendedMove.Enpassant -> {
                setFigure(extendedMove.move.to, movingFigure)
                clearPos(extendedMove.pawnTaken.position)
                // inform the involved figure(s) of the move
                movingFigure.figureMoved(extendedMove.move)
            }
            is ExtendedMove.PawnDoubleJump -> {
                setFigure(extendedMove.move.to, movingFigure)
                // inform the involved figure(s) of the move
                extendedMove.pawn.let { pawn: Pawn ->
                    pawn.figureMoved(extendedMove.move)
                    pawn.canBeHitEnpassant = true
                }
            }
            is ExtendedMove.Normal -> {
                setFigure(extendedMove.move.to, movingFigure)
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

        extendedMoveStack.addLast(extendedMove)

        return extendedMove.hasHitFigure
    }

    override fun undo(): Boolean {
        clearCheckComputation()

        val lastExtMove: ExtendedMove = extendedMoveStack.removeLast()
        when(lastExtMove) {
            is ExtendedMove.Castling -> {
                val king = clearFigure(lastExtMove.kingMove.to) as King
                val rook = clearFigure(lastExtMove.rookMove.to)
                setFigure(lastExtMove.kingMove.from, king)
                setFigure(lastExtMove.rookMove.from, rook)
                // inform the involved figure(s) of the undo
                king.undoMove(lastExtMove.kingMove.from)
                rook.undoMove(lastExtMove.rookMove.from)
                king.didCastling = false
            }
            is ExtendedMove.Promotion -> {
                clearPos(lastExtMove.move.to)
                lastExtMove.figureTaken?.let { figureTaken->
                    setFigure(figureTaken.position, figureTaken)
                }
                setFigure(lastExtMove.move.from, lastExtMove.pawnPromoted)
                // no undo necessary because pawn's position was never updated
            }
            is ExtendedMove.Enpassant -> {
                val pawnMoved = clearFigure(lastExtMove.move.to)
                setFigure(lastExtMove.move.from, pawnMoved)
                lastExtMove.pawnTaken.let { pawnTaken->
                    setFigure(pawnTaken.position, pawnTaken)
                }
                // inform the involved figure(s) of the undo
                pawnMoved.undoMove(lastExtMove.move.from)
            }
            is ExtendedMove.PawnDoubleJump -> {
                lastExtMove.pawn.let { pawn ->
                    clearPos(lastExtMove.move.to)
                    setFigure(lastExtMove.move.from, pawn)
                    // inform the involved figure(s) of the undo
                    pawn.undoMove(lastExtMove.move.from)
                    pawn.canBeHitEnpassant=false
                }
            }
            is ExtendedMove.Normal -> {
                val movingFigure = clearFigure(lastExtMove.move.to)
                setFigure(lastExtMove.move.from, movingFigure)
                lastExtMove.figureTaken?.let { setFigure(lastExtMove.move.to, it) }
                // inform the involved figure(s) of the undo
                movingFigure.undoMove(lastExtMove.move.from)
            }
        }.let {}

        extendedMoveStack.lastOrNull()?.let { preLastExtMove->
            if(preLastExtMove is ExtendedMove.PawnDoubleJump) {
                preLastExtMove.pawn.canBeHitEnpassant = true
            }
        }

        return lastExtMove.hasHitFigure
    }

    override fun movesPlayed(): List<Move> = extendedMoveStack.map { it.move }

    override fun getFigureOrNull(pos: Position) = game[pos.index]
    override fun isFreeArea(pos: Position) = game[pos.index] == null

    override fun toString(): String {
        val buffer = StringBuilder(512)
        for (row in 0..7) {
            for (column in 0..7) {
                val pos = Position[row, column]
                getFigureOrNull(pos)?.let {figure ->
                    buffer.append("$figure ")
                }
            }
        }
        //delete the final space
        if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)

        return buffer.toString()
    }
}

private fun ArrayDeque<ExtendedMove>.getLatestMoves(count: Int?): String {
    val latestExtendedMoves = if (count == null) {
        this
    } else {
        assert(count > 0)
        val minIndex = (size - count).coerceAtLeast(0)
        filterIndexed { index, _ -> index >= minIndex }
    }

    return latestExtendedMoves.map { it.move }.joinToString(separator = ",") { it.toString() }
}

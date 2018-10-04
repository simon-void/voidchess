package voidchess.board

import voidchess.figures.Figure
import voidchess.figures.FigureFactory
import voidchess.helper.*
import java.util.*

class SimpleArrayBoard constructor(private val lastMoveProvider: LastMoveProvider) : SimpleChessBoardInterface {
    private val game: Array<Figure?> = arrayOfNulls(64)
    private val figureFactory: FigureFactory = FigureFactory()

    private val defaultKingPos = Position.byCode("a1")
    private var whiteKingPosition: Position = defaultKingPos
    private var blackKingPosition: Position = defaultKingPos

    private var calculatedWhiteCheck: Boolean = false
    private var calculatedBlackCheck: Boolean = false
    private var isWhiteCheck: Boolean = false
    private var isBlackCheck: Boolean = false
    private var whiteCheckStatus: CheckStatus? = null
    private var blackCheckStatus: CheckStatus? = null

    init {
        init()
    }

    //for testing
    constructor(des: String, lastMoveProvider: LastMoveProvider) : this(lastMoveProvider) {
        init(des)
    }

    private fun clearCheckComputation() {
        calculatedWhiteCheck = false
        calculatedBlackCheck = false
        whiteCheckStatus = null
        blackCheckStatus = null
    }

    override fun isCheck(isWhite: Boolean): Boolean {
        return if (isWhite) {
            if (!calculatedWhiteCheck) {
                isWhiteCheck = CheckSearch.isCheck(this, whiteKingPosition)
                calculatedWhiteCheck = true
            }
            isWhiteCheck
        } else {
            if (!calculatedBlackCheck) {
                isBlackCheck = CheckSearch.isCheck(this, blackKingPosition)
                calculatedBlackCheck = true
            }
            isBlackCheck
        }
    }

    override fun getCheckStatus(isWhite: Boolean): CheckStatus {
        val lastMove = lastMoveProvider.getLastMove()
        return if (isWhite) {
            var scopedWhiteCheckStatus = whiteCheckStatus
            if (scopedWhiteCheckStatus == null) {
                scopedWhiteCheckStatus = CheckSearch.analyseCheck(this, true, lastMove)
                whiteCheckStatus = scopedWhiteCheckStatus
            }
            scopedWhiteCheckStatus
        } else {
            var scopedBlackCheckStatus = blackCheckStatus
            if (scopedBlackCheckStatus == null) {
                scopedBlackCheckStatus = CheckSearch.analyseCheck(this, false, lastMove)
                blackCheckStatus = scopedBlackCheckStatus
            }
            scopedBlackCheckStatus
        }
    }

    override fun init() {
        clear()
        var pos: Position

        for (i in 0..7) {
            pos = Position[1, i]
            setFigure(pos, figureFactory.getPawn(pos, true))
            pos = Position[6, i]
            setFigure(pos, figureFactory.getPawn(pos, false))
        }
        pos = Position.byCode("a1")
        setFigure(pos, figureFactory.getRook(pos, true))
        pos = Position.byCode("h1")
        setFigure(pos, figureFactory.getRook(pos, true))
        pos = Position.byCode("b1")
        setFigure(pos, figureFactory.getKnight(pos, true))
        pos = Position.byCode("g1")
        setFigure(pos, figureFactory.getKnight(pos, true))
        pos = Position.byCode("c1")
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position.byCode("f1")
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position.byCode("d1")
        setFigure(pos, figureFactory.getQueen(pos, true))
        pos = Position.byCode("e1")
        setFigure(pos, figureFactory.getKing(pos, true))
        whiteKingPosition = pos

        pos = Position.byCode("a8")
        setFigure(pos, figureFactory.getRook(pos, false))
        pos = Position.byCode("h8")
        setFigure(pos, figureFactory.getRook(pos, false))
        pos = Position.byCode("b8")
        setFigure(pos, figureFactory.getKnight(pos, false))
        pos = Position.byCode("g8")
        setFigure(pos, figureFactory.getKnight(pos, false))
        pos = Position.byCode("c8")
        setFigure(pos, figureFactory.getBishop(pos, false))
        pos = Position.byCode("f8")
        setFigure(pos, figureFactory.getBishop(pos, false))
        pos = Position.byCode("d8")
        setFigure(pos, figureFactory.getQueen(pos, false))
        pos = Position.byCode("e8")
        setFigure(pos, figureFactory.getKing(pos, false))
        blackKingPosition = pos

        assert(whiteKingPosition== Position.byCode("e1")) {"expected kingPos e1 but was $whiteKingPosition"}
        assert(blackKingPosition== Position.byCode("e8")) {"expected kingPos e8 but was $blackKingPosition"}
    }

    override fun init(chess960: Int) {
        var code960 = chess960
        assert(code960 in 0..959) { "chess960 out of bounds. Should be 0-959, is $code960" }

        clear()
        var pos: Position

        // pawn positions is always the same
        for (i in 0..7) {
            pos = Position[1, i]
            setFigure(pos, figureFactory.getPawn(pos, true))
            pos = Position[6, i]
            setFigure(pos, figureFactory.getPawn(pos, false))
        }

        // first bishop
        var rest = code960 % 4
        var row = rest * 2 + 1
        code960 /= 4

        pos = Position[0, row]
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getBishop(pos, false))

        // second bishop
        rest = code960 % 4
        row = rest * 2
        code960 /= 4

        pos = Position[0, row]
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getBishop(pos, false))

        // queen
        rest = code960 % 6
        row = getFreeRow(rest)
        code960 /= 6

        pos = Position[0, row]
        setFigure(pos, figureFactory.getQueen(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getQueen(pos, false))

        val otherFigures = getFigureArray(code960)
        for (i in 0..4) {
            // always into the first free column
            row = getFreeRow(0)
            pos = Position[0, row]
            var figure = createFigure(otherFigures[i], true, pos)
            setFigure(pos, figure)
            if (figure.isKing()) {
                whiteKingPosition = pos
            }
            pos = Position[7, row]
            figure = createFigure(otherFigures[i], false, pos)
            setFigure(pos, figure)
            if (figure.isKing()) {
                blackKingPosition = pos
            }
        }

        assert(whiteKingPosition!= defaultKingPos) {"kingPos is never a1 in chess960"}
        assert(blackKingPosition!= defaultKingPos) {"kingPos is never a1 in chess960"}
    }

    override fun init(des: String) {
        clear()

        val iter = des.splitAndTrim(' ').iterator()
        iter.next()
        iter.next()

        while (iter.hasNext()) {
            val figureDescription = iter.next()
            val pos = getPositionOfCodedFigure(figureDescription)
            val figure = figureFactory.getFigureByString(figureDescription)
            setFigure(pos, figure)
        }
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
        "Rook" -> figureFactory.getRook(pos, isWhite)
        "Knight" -> figureFactory.getKnight(pos, isWhite)
        "King" -> figureFactory.getKing(pos, isWhite)
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
        for (linearIndex in 0..63) {
            game[linearIndex] = null
        }
        whiteKingPosition = defaultKingPos
        blackKingPosition = defaultKingPos
    }

    override fun setFigure(pos: Position, figure: Figure) {
        clearCheckComputation()

        game[pos.index] = figure
        if (figure.isKing()) {
            if (figure.isWhite)
                whiteKingPosition = pos
            else
                blackKingPosition = pos
        }
    }

    override fun clearFigure(pos: Position) {
        assert(game[pos.index] != null) { "position $pos is already clear" }
        game[pos.index] = null
    }

    override fun move(figure: Figure, to: Position): Figure? {
        val move = Move[figure.position, to]
        game[figure.position.index] = null
        val figureTaken = game[to.index]
        game[to.index] = figure
        figure.figureMoved(move)
        return figureTaken
    }

    override fun undoMove(figure: Figure, from: Position, figureTaken: Figure?) {
        game[figure.position.index] = figureTaken
        game[from.index] = figure
        figure.undoMove(from)
    }

    override fun getFigure(pos: Position) = game[pos.index]
    override fun isFreeArea(pos: Position) = game[pos.index] == null
    override fun getContent(pos: Position) = BoardContent.get(game[pos.index])

    override fun getFigures(): List<Figure> {
        val figureIter = LinkedList<Figure>()

        for (linearIndex in 0..63) {
            val figure = game[linearIndex]
            if (figure != null) {
                figureIter.add(figure)
            }
        }
        return figureIter
    }

    override fun getKingPosition(whiteKing: Boolean) =
            if (whiteKing) whiteKingPosition
            else blackKingPosition

    override fun toString(): String {
        val buffer = StringBuilder(512)
        for (row in 0..7) {
            for (column in 0..7) {
                val pos = Position[row, column]
                val content = getContent(pos)
                if (!content.isFreeArea) {
                    buffer.append(content.figure.toString())
                    buffer.append(" ")
                }
            }
        }
        //delete the final space
        if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)

        return buffer.toString()
    }
}

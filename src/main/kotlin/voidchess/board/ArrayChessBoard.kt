package voidchess.board

import voidchess.board.check.AttackLines
import voidchess.board.check.CheckSearch
import voidchess.board.check.checkAttackLines
import voidchess.board.move.Move
import voidchess.board.move.Position
import voidchess.figures.Figure
import voidchess.figures.FigureFactory
import voidchess.figures.King
import voidchess.helper.*

class ArrayChessBoard constructor() : ChessBoard {
    private val game: Array<Figure?> = arrayOfNulls(64)
    private val figureFactory = FigureFactory

    // the alternative to creating dummy instances for white and black king is 'lateinit'
    // but that would come with a null-check on each access
    override var whiteKing: King = figureFactory.getKing(Position.byCode("a1"), true)
    override var blackKing: King = figureFactory.getKing(Position.byCode("a8"), false)

    private var calculatedWhiteCheck: Boolean = false
    private var calculatedBlackCheck: Boolean = false
    private var isWhiteCheck: Boolean = false
    private var isBlackCheck: Boolean = false
    private var cachedAttackLines: AttackLines? = null

    val figureCount: Int
        get() {
            var count = 0
            forAllFigures { count++ }
            return count
        }

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
            var scopedAttackLines = cachedAttackLines
            if (scopedAttackLines == null) {
                scopedAttackLines = checkAttackLines(this, isWhite)
                cachedAttackLines = scopedAttackLines
            }
            return scopedAttackLines
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
        whiteKing = figureFactory.getKing(pos, true)
        setFigure(pos, whiteKing)

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
        blackKing = figureFactory.getKing(pos, false)
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
            setFigure(pos, figureFactory.getPawn(pos, true))
            pos = Position[6, i]
            setFigure(pos, figureFactory.getPawn(pos, false))
        }

        // first bishop
        var rest = code960Code % 4
        var row = rest * 2 + 1
        code960Code /= 4

        pos = Position[0, row]
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getBishop(pos, false))

        // second bishop
        rest = code960Code % 4
        row = rest * 2
        code960Code /= 4

        pos = Position[0, row]
        setFigure(pos, figureFactory.getBishop(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getBishop(pos, false))

        // queen
        rest = code960Code % 6
        row = getFreeRow(rest)
        code960Code /= 6

        pos = Position[0, row]
        setFigure(pos, figureFactory.getQueen(pos, true))
        pos = Position[7, row]
        setFigure(pos, figureFactory.getQueen(pos, false))

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
            val figure = figureFactory.getFigureByString(figureDescription)
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
    }

    override fun setFigure(pos: Position, figure: Figure) {
        clearCheckComputation()
        game[pos.index] = figure
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

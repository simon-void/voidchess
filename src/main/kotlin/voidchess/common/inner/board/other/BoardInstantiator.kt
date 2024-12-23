package voidchess.common.inner.board.other

import voidchess.common.board.move.Position
import voidchess.common.board.other.Chess960Index
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.*
import voidchess.common.figures.Figure
import voidchess.common.figures.FigureType.*
import voidchess.common.figures.King

fun interface BoardInstantiator {
    fun generateInitialSetup(): Iterable<Pair<Position, Figure?>>
}

fun StartConfig.boardInstantiator() = BoardInstantiator {
    when (val startConfig = this) {
        is StartConfig.Chess960Config -> getChess960Setup(startConfig.chess960Index)
        is StartConfig.ManualConfig -> {
            val board = arrayOfNulls<Figure>(64)
            for (figureDescription in startConfig.figureStates) {
                val figure = getFigureByString(figureDescription)
                val pos = figure.position
                board[pos.index] = figure
            }
            board.mapIndexed { posIndex: Int, figure: Figure? ->
                Position.byIndex(posIndex) to figure
            }
        }
    }
}

private fun getChess960Setup(chess960Index: Chess960Index): List<Pair<Position, Figure?>> =
    mutableListOf<Pair<Position, Figure?>>().apply {
        for (posAndFigure in getEmptyRows2to5()) add(posAndFigure)
        for (posAndFigure in getPawnRows()) add(posAndFigure)
        for (posAndFigure in getFigureRows(chess960Index)) add(posAndFigure)
    }

private fun getEmptyRows2to5(): Iterator<Pair<Position, Figure?>> = iterator<Pair<Position, Figure?>> {
    for (row in 2..5) {
        for (column in 0..7) {
            yield(Position[row, column] to null)
        }
    }
}

private fun getPawnRows(): Iterator<Pair<Position, Figure?>> = iterator<Pair<Position, Figure?>> {
    for (column in 0..7) {
        yield(Position[1, column].let { pos -> pos to Pawn(true, pos) })
        yield(Position[6, column].let { pos -> pos to Pawn(false, pos) })
    }
}

private fun getFigureRows(chess960: Chess960Index): Iterator<Pair<Position, Figure?>>  {
    var code960Code = chess960.value

    val figureTypeIn = arrayOfNulls<FigureType>(8)
    fun getFreeColumn(index: Int): Int {
        check(index in 0..7)

        var counter = 0
        for (column in 0..7) {
            if (figureTypeIn[column]==null) {
                if (index == counter)
                    return column
                else
                    counter++
            }
        }
        throw IllegalStateException("No free Position with index $index found")
    }

    // first bishop
    var rest = code960Code % 4
    var assignedColumn = rest * 2 + 1
    code960Code /= 4

    figureTypeIn[assignedColumn] = BISHOP

    // second bishop
    rest = code960Code % 4
    assignedColumn = rest * 2
    code960Code /= 4

    figureTypeIn[assignedColumn] = BISHOP

    // queen
    rest = code960Code % 6
    assignedColumn = getFreeColumn(rest)
    code960Code /= 6

    figureTypeIn[assignedColumn] = QUEEN

    val otherFigureTypes = when (code960Code) {
        0 -> arrayOf(KNIGHT, KNIGHT, ROOK, KING, ROOK)
        1 -> arrayOf(KNIGHT, ROOK, KNIGHT, KING, ROOK)
        2 -> arrayOf(KNIGHT, ROOK, KING, KNIGHT, ROOK)
        3 -> arrayOf(KNIGHT, ROOK, KING, ROOK, KNIGHT)
        4 -> arrayOf(ROOK, KNIGHT, KNIGHT, KING, ROOK)
        5 -> arrayOf(ROOK, KNIGHT, KING, KNIGHT, ROOK)
        6 -> arrayOf(ROOK, KNIGHT, KING, ROOK, KNIGHT)
        7 -> arrayOf(ROOK, KING, KNIGHT, KNIGHT, ROOK)
        8 -> arrayOf(ROOK, KING, KNIGHT, ROOK, KNIGHT)
        9 -> arrayOf(ROOK, KING, ROOK, KNIGHT, KNIGHT)
        else -> throw IllegalArgumentException("index should be between [0-9] but is $code960Code")
    }

    for (figureType in otherFigureTypes) {
        // always into the first free column
        assignedColumn = getFreeColumn(0)
        figureTypeIn[assignedColumn] = figureType
    }

    return iterator {
        for(column in 0..7) {
            when(figureTypeIn[column]) {
                ROOK -> {
                    yield(Position[0, column].let { pos -> pos to Rook(true, pos) })
                    yield(Position[7, column].let { pos -> pos to Rook(false, pos) })
                }
                KNIGHT -> {
                    yield(Position[0, column].let { pos -> pos to Knight(true, pos) })
                    yield(Position[7, column].let { pos -> pos to Knight(false, pos) })
                }
                BISHOP -> {
                    yield(Position[0, column].let { pos -> pos to Bishop(true, pos) })
                    yield(Position[7, column].let { pos -> pos to Bishop(false, pos) })
                }
                QUEEN -> {
                    yield(Position[0, column].let { pos -> pos to Queen(true, pos) })
                    yield(Position[7, column].let { pos -> pos to Queen(false, pos) })
                }
                KING -> {
                    yield(Position[0, column].let { pos -> pos to King(true, pos) })
                    yield(Position[7, column].let { pos -> pos to King(false, pos) })
                }
                PAWN -> {
                    throw IllegalStateException("Pawns should never be in figureTypeRow")
                }
                null -> {
                    throw IllegalStateException("null should never be in figureTypeRow")
                }
            }
        }
    }
}

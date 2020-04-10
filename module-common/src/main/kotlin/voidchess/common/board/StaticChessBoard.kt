package voidchess.common.board

import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.common.figures.Figure
import voidchess.common.figures.King


interface BasicChessBoard {
    val whiteKing: King
    val blackKing: King
    fun isFreeArea(pos: Position): Boolean
    fun getFigureOrNull(pos: Position): Figure?
}

fun BasicChessBoard.getFirstFigureInDir(direction: Direction, startPos: Position): Figure? {
    startPos.forEachPosInLine(direction) { pos->
        val figure = getFigureOrNull(pos)
        if(figure==null) {
            return@forEachPosInLine false
        }else{
            return figure
        }
    }
    return null
}

fun BasicChessBoard.getKing(isWhiteKing: Boolean): King = if (isWhiteKing) { whiteKing } else { blackKing }

fun BasicChessBoard.getFigure(pos: Position) = getFigureOrNull(pos) ?: throw AssertionError("no figure at $pos")

inline fun BasicChessBoard.forAllFigures(informOfFigure: (Figure)->Unit) {
    for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let(informOfFigure)
}

inline fun BasicChessBoard.forAllFiguresOfColor(isWhite: Boolean, informOfFigure: (Figure)->Unit) {
    if(isWhite) {
        for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let {
            if (it.isWhite) {
                informOfFigure(it)
            }
        }
    }else{
        for (linearIndex in 63 downTo 0) getFigureOrNull(Position.byIndex(linearIndex))?.let {
            if (!it.isWhite) {
                informOfFigure(it)
            }
        }
    }
}

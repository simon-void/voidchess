package voidchess.board

import voidchess.board.move.Direction
import voidchess.board.move.Position
import voidchess.figures.Figure
import voidchess.figures.King
import java.lang.IllegalArgumentException


internal interface BasicChessBoard {
    val whiteKing: King
    val blackKing: King
    fun isFreeArea(pos: Position): Boolean
    fun getFigureOrNull(pos: Position): Figure?
}

internal fun BasicChessBoard.getFirstFigureInDir(direction: Direction, startPos: Position): Figure? {
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

internal fun BasicChessBoard.getKing(isWhiteKing: Boolean): King = if (isWhiteKing) { whiteKing } else { blackKing }

internal fun BasicChessBoard.getFigure(pos: Position) = getFigureOrNull(pos) ?: throw IllegalArgumentException("no figure at $pos")

internal inline fun BasicChessBoard.forAllFigures(informOfFigure: (Figure)->Unit) {
    for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let(informOfFigure)
}

internal inline fun BasicChessBoard.forAllFiguresOfColor(isWhite: Boolean, informOfFigure: (Figure)->Unit) {
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

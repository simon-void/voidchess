package voidchess.board

import voidchess.board.move.Direction
import voidchess.board.move.Position
import voidchess.figures.Figure
import voidchess.figures.King
import java.lang.IllegalArgumentException


interface BasicChessGameInterface {
    val whiteKing: King
    val blackKing: King
    fun isFreeArea(pos: Position): Boolean
    fun getFigureOrNull(pos: Position): Figure?
}

fun BasicChessGameInterface.getFirstFigureInDir(direction: Direction, startPos: Position): Figure? {
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

fun BasicChessGameInterface.getKing(isWhiteKing: Boolean): King = if (isWhiteKing) { whiteKing } else { blackKing }

fun BasicChessGameInterface.getFigure(pos: Position) = getFigureOrNull(pos) ?: throw IllegalArgumentException("no figure at $pos")

inline fun BasicChessGameInterface.forAllFigures(informOfFigure: (Figure)->Unit) {
    for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let(informOfFigure)
}

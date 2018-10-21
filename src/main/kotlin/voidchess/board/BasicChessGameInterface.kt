package voidchess.board

import voidchess.board.move.Direction
import voidchess.board.move.Position
import voidchess.figures.Figure
import voidchess.figures.King


interface BasicChessGameInterface {
    val whiteKing: King
    val blackKing: King
    fun isFreeArea(pos: Position): Boolean
    fun getFigure(pos: Position): Figure?
    fun getContent(pos: Position): BoardContent
}

fun BasicChessGameInterface.getFirstFigureInDir(direction: Direction, startPos: Position): Figure? {
    startPos.forEachPosInLine(direction) { pos->
        val figure = getFigure(pos)
        if(figure==null) {
            return@forEachPosInLine false
        }else{
            return figure
        }
    }
    return null
}

fun BasicChessGameInterface.getKing(isWhiteKing: Boolean): King = if (isWhiteKing) { whiteKing } else { blackKing }
inline fun BasicChessGameInterface.forAllFigures(informOfFigure: (Figure)->Unit) {
    for (linearIndex in 0..63) getFigure(Position.byIndex(linearIndex))?.let(informOfFigure)
}

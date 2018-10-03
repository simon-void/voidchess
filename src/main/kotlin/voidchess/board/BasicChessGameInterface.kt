package voidchess.board

import voidchess.figures.Figure
import voidchess.helper.Direction
import voidchess.helper.Position

/**
 * @author stephan
 */
interface BasicChessGameInterface {

    fun getFigures(): List<Figure>
    fun isFreeArea(pos: Position): Boolean
    fun getFigure(pos: Position): Figure?
    fun getContent(pos: Position): BoardContent
    fun getKingPosition(whiteKing: Boolean): Position
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

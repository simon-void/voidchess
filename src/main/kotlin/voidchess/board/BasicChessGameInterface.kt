package voidchess.board

import voidchess.figures.Figure
import voidchess.helper.Position

/**
 * @author stephan
 */
interface BasicChessGameInterface {

    fun getFigures(): List<Figure>
    fun isFreeArea(pos: Position): Boolean
    fun getFigure(pos: Position): Figure?
    fun getKingPosition(whiteKing: Boolean): Position
}

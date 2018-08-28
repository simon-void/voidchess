package voidchess.board

import voidchess.figures.Figure
import voidchess.helper.CheckStatus
import voidchess.helper.Position

/**
 * @author stephan
 */
interface SimpleChessBoardInterface : BasicChessGameInterface {

    fun init()
    fun init(chess960: Int)
    fun init(des: String)
    fun setFigure(pos: Position, figure: Figure)
    fun isCheck(isWhite: Boolean): Boolean
    fun getCheckStatus(isWhite: Boolean): CheckStatus
}

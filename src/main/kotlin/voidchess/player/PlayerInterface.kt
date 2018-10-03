package voidchess.player

import voidchess.board.MoveResult
import voidchess.helper.PawnPromotion
import voidchess.helper.Position


interface PlayerInterface {
    fun gameStarts()
    fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean)
    fun gaveCheck()
    fun play()
    fun askForPawnPromotionType(pawnPosition: Position): PawnPromotion
    fun setColor(isWhite: Boolean)
}

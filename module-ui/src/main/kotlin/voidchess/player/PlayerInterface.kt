package voidchess.player

import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position


interface PlayerInterface {
    fun gameStarts()
    fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean)
    fun gaveCheck()
    fun play()
    fun askForPawnPromotionType(pawnPosition: Position): PawnPromotion
    fun setColor(isWhite: Boolean)
}

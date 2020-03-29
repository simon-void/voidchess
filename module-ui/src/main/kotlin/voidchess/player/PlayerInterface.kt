package voidchess.player

import voidchess.board.move.MoveResult
import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position


interface PlayerInterface {
    fun gameStarts()
    fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean)
    fun gaveCheck()
    fun play()
    fun askForPawnPromotionType(pawnPosition: Position): PawnPromotion
    fun setColor(isWhite: Boolean)
}

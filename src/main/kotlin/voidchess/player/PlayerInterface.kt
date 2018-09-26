package voidchess.player

import voidchess.helper.PawnPromotion
import voidchess.helper.Position


interface PlayerInterface {
    fun setIsPlaying(isPlaying: Boolean)
    fun play()
    fun askForPawnPromotionType(pawnPosition: Position): PawnPromotion
    fun setColor(isWhite: Boolean)
}

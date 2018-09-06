package voidchess.helper

/**
 * @author stephan
 */
interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

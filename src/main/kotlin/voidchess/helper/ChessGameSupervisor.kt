package voidchess.helper

/**
 * @author stephan
 */
interface ChessGameSupervisor {
    // TODO maybe an enum would be better
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

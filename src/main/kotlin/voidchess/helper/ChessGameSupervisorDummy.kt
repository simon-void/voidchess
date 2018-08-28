package voidchess.helper

/**
 * @author stephan
 */

object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

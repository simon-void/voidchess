package voidchess.helper

/**
 * @author stephan
 */
class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

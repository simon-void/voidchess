package voidchess.helper


object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

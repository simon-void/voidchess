package voidchess.helper


interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

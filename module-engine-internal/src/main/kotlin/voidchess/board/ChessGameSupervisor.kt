package voidchess.board

import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position


internal interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

internal object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

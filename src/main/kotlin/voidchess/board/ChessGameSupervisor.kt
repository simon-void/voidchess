package voidchess.board

import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position


interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

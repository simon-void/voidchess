package voidchess.engine.board

import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position


internal interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

internal object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

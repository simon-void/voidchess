package voidchess.common.board.other

import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position

// TODO make functional interface in Kotlin 1.4
interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

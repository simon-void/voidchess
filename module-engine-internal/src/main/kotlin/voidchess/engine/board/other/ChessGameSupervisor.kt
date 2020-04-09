package voidchess.engine.board.other

import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position

// TODO make functional interface in Kotlin 1.4
internal interface ChessGameSupervisor {
    fun askForPawnChange(pawnPosition: Position): PawnPromotion
}

internal object ChessGameSupervisorDummy : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return PawnPromotion.QUEEN
    }

}

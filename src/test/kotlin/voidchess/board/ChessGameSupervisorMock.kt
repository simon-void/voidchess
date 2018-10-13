package voidchess.board

import voidchess.board.ChessGameSupervisor
import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position


class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

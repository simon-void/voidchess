package voidchess.common.board.move

import voidchess.common.helper.ColdPromise

sealed class HumanMoveResult(
    val extendedHumanMove: ExtendedMove
) {
    class GameEnds(extendedHumanMove: ExtendedMove): HumanMoveResult(extendedHumanMove)
    class Ongoing(
        extendedHumanMove: ExtendedMove,
        val computerMovePromise: ColdPromise<ComputerMoveResult>
    ): HumanMoveResult(extendedHumanMove)
}

sealed class ComputerMoveResult(
    val extendedComputerMove: ExtendedMove
) {
    class GameEnds(extendedComputerMove: ExtendedMove): ComputerMoveResult(extendedComputerMove)
    class Ongoing(extendedComputerMove: ExtendedMove) : ComputerMoveResult(extendedComputerMove)
}
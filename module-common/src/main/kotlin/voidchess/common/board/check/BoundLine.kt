package voidchess.common.board.check

import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.common.board.move.PositionProgression

data class BoundLine(
    private val kingPos: Position,
    private val boundFigurePos: Position,
    internal val attackerPos: Position,
    val boundFigureToAttackerDirection: Direction
) {
    init {
        assert(kingPos.getDirectionTo(boundFigurePos)==boundFigureToAttackerDirection)
        assert(boundFigurePos.getDirectionTo(attackerPos)==boundFigureToAttackerDirection)
    }

    /**
     * All positions from bound figure to attacker (exclusive bound figure's position, inclusive attacker's position).
     */
    val possibleMovesToAttacker = PositionProgression(
        boundFigurePos.step(boundFigureToAttackerDirection)!!,
        boundFigurePos.distanceTo(attackerPos),
        boundFigureToAttackerDirection
    )
    /**
     * All positions from bound figure to king (exclusive bound figure's position, exclusive king's position).
     */
    val possibleMovesToKing = PositionProgression(
        boundFigurePos.step(boundFigureToAttackerDirection.reverse)!!,
        boundFigurePos.distanceTo(kingPos) - 1,
        boundFigureToAttackerDirection.reverse
    )
}
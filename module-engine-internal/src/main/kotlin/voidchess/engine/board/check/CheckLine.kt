package voidchess.engine.board.check

import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.engine.board.move.PositionProgression

/**
 * the iterator contains all Positions that can be used to break the check (starting with the position of the attacker)
 */
internal sealed class CheckLine(
    val attackerPos: Position,
    val kingPos: Position,
    val posProgression: PositionProgression
) {
    constructor(attackerPos: Position, kingPos: Position): this(attackerPos, kingPos, PositionProgression(attackerPos, 1, Direction.UP))

    abstract fun keepsKingInCheckIfHeMovesTo(direction: Direction): Boolean
    abstract val isStraightCheck: Boolean
    abstract val isDiagonalCheck: Boolean
}

internal class KnightCheck(
    knightPos: Position,
    kingPos: Position
): CheckLine(knightPos, kingPos) {
    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = false
    override val isStraightCheck = false
    override val isDiagonalCheck = false
}

internal class PawnCheck(
    pawnPos: Position,
    kingPos: Position
): CheckLine(pawnPos, kingPos) {
    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = false
    override val isStraightCheck = false
    override val isDiagonalCheck = true
}

internal class ActualCheckLine(
    kingPos: Position,
    attackerPos: Position,
    private val kingToAttackerDirection: Direction
): CheckLine(attackerPos, kingPos, PositionProgression(attackerPos, attackerPos.distanceTo(kingPos), kingToAttackerDirection.reverse)) {

    init {
        assert(kingPos.getDirectionTo(attackerPos)==kingToAttackerDirection) {"actualDir: $kingToAttackerDirection, expectedDir: ${kingPos.getDirectionTo(attackerPos)}"}
    }

    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = when (direction) {
        kingToAttackerDirection -> posProgression.size!=1
        kingToAttackerDirection.reverse -> true
        else -> false
    }
    override val isStraightCheck = kingToAttackerDirection.isStraight
    override val isDiagonalCheck = kingToAttackerDirection.isDiagonal
}
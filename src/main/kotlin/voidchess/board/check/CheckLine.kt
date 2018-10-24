package voidchess.board.check

import voidchess.board.move.Direction
import voidchess.board.move.Position
import voidchess.board.move.PositionProgression

/**
 * the iterator contains all Positions that can be used to break the check (starting with the position of the attacker)
 */
sealed class CheckLine(
        val attackerPos: Position,
        val kingPos: Position
) : Iterable<Position> {
    abstract fun keepsKingInCheckIfHeMovesTo(direction: Direction): Boolean
    abstract val isStraightCheck: Boolean
    abstract val isDiagonalCheck: Boolean
    abstract val hasSingleInterceptPos: Boolean
}

class KnightCheck(
        knightPos: Position,
        kingPos: Position
): CheckLine(knightPos, kingPos) {
    override fun iterator() = listOf(attackerPos).iterator()
    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = false
    override val isStraightCheck = false
    override val isDiagonalCheck = false
    override val hasSingleInterceptPos = true
}

class PawnCheck(
        pawnPos: Position,
        kingPos: Position
): CheckLine(pawnPos, kingPos) {
    override fun iterator() = listOf(attackerPos).iterator()
    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = false
    override val isStraightCheck = false
    override val isDiagonalCheck = true
    override val hasSingleInterceptPos = true
}

class ActualCheckLine(
        kingPos: Position,
        attackerPos: Position,
        private val kingToAttackerDirection: Direction
): CheckLine(attackerPos, kingPos) {

    init {
        assert(kingPos.getDirectionTo(attackerPos)==kingToAttackerDirection) {"actualDir: $kingToAttackerDirection, expectedDir: ${kingPos.getDirectionTo(attackerPos)}"}
    }

    private val posProgression = PositionProgression(attackerPos, attackerPos.distanceTo(kingPos), kingToAttackerDirection.reverse)
    override fun iterator() = posProgression.iterator()
    override fun keepsKingInCheckIfHeMovesTo(direction: Direction) = when (direction) {
        kingToAttackerDirection -> posProgression.size!=1
        kingToAttackerDirection.reverse -> true
        else -> false
    }
    override val isStraightCheck = kingToAttackerDirection.isStraight
    override val isDiagonalCheck = kingToAttackerDirection.isDiagonal
    override val hasSingleInterceptPos = posProgression.size==1
}
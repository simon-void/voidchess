package voidchess.board.check

import voidchess.board.BasicChessGameInterface
import voidchess.board.getFirstFigureInDir
import voidchess.board.getKing
import voidchess.board.move.Direction
import voidchess.board.move.Position
import java.util.*
import kotlin.collections.HashMap


data class AttackLines(val checkLines: List<CheckLine>, val boundLineByBoundFigurePos: Map<Position, BoundLine>) {
    init {
        assert(checkLines.size in 0..2) {"checkLines can only possibly contain between 0 and 2 checks but does contain ${checkLines.size}"}
    }

    val noCheck
        get() = checkLines.isEmpty()
    val isCheck
        get() = checkLines.isNotEmpty()
    val isSingleCheck
        get() = checkLines.size == 1
    val isDoubleCheck
        get() = checkLines.size == 2
}

/**
 * the iterator contains all Positions that can be used to break the check (starting with the position of the attacker)
 */
sealed class CheckLine(
        internal val attackerPos: Position
) : Iterable<Position> {
    abstract fun doesNotKeepKingInCheckIfHeMovesTo(direction: Direction): Boolean
}

class KnightOrPawnCheck(
        knightOrPawnPos: Position
): CheckLine(knightOrPawnPos) {
    override fun iterator() = listOf(attackerPos).iterator()
    override fun doesNotKeepKingInCheckIfHeMovesTo(direction: Direction) = true
}
class ActualCheckLine(
        kingPos: Position,
        attackerPos: Position,
        private val kingToAttackerDirection: Direction
): CheckLine(attackerPos) {

    init {
        assert(kingPos.getDirectionTo(attackerPos)==kingToAttackerDirection) {"actualDir: $kingToAttackerDirection, expectedDir: ${kingPos.getDirectionTo(attackerPos)}"}
    }

    private val posProgression = PositionProgression(attackerPos, attackerPos.distanceTo(kingPos), kingToAttackerDirection.reverse)
    override fun iterator() = posProgression.iterator()
    override fun doesNotKeepKingInCheckIfHeMovesTo(direction: Direction) = when (direction) {
        kingToAttackerDirection -> posProgression.size==1
        kingToAttackerDirection.reverse -> false
        else -> true
    }
}

data class BoundLine(
        private val kingPos: Position,
        private val boundFigurePos: Position,
        private val attackerPos: Position,
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
            boundFigureToAttackerDirection)
    /**
     * All positions from bound figure to king (exclusive bound figure's position, exclusive king's position).
     */
    val possibleMovesToKing = PositionProgression(
            boundFigurePos.step(boundFigureToAttackerDirection.reverse)!!,
            boundFigurePos.distanceTo(kingPos)-1,
            boundFigureToAttackerDirection.reverse)
}


fun checkAttackLines(game: BasicChessGameInterface, isWhite: Boolean): AttackLines {
    val king = game.getKing(isWhite)
    val kingPos = king.position

    val checkLines = LinkedList<CheckLine>()
    val boundLineByBoundFigurePos = HashMap<Position, BoundLine>(8)

    // check for check by bishop, rook or queen (the only figures that can also bind other figures)
    for(directionFromKingOutwards in Direction.values()) {
        val firstInLine = game.getFirstFigureInDir(directionFromKingOutwards, kingPos)
        if (firstInLine != null) {
            if (firstInLine.isWhite != isWhite) {
                val isAttackingKing = if (directionFromKingOutwards.isStraight) firstInLine.attacksStraightLine else firstInLine.attacksDiagonalLine
                if (isAttackingKing) {
                    checkLines.add(ActualCheckLine(kingPos, firstInLine.position, directionFromKingOutwards))
                }
            } else {
                val secondInLine = game.getFirstFigureInDir(directionFromKingOutwards, firstInLine.position)
                if (secondInLine != null) {
                    if (secondInLine.isWhite != isWhite) {
                        val isFirstInLineBound = if (directionFromKingOutwards.isStraight) secondInLine.attacksStraightLine else secondInLine.attacksDiagonalLine
                        if (isFirstInLineBound) {
                            val boundFigurePos = firstInLine.position
                            boundLineByBoundFigurePos[boundFigurePos] =
                                    BoundLine(kingPos, boundFigurePos, secondInLine.position, directionFromKingOutwards)
                        }
                    }
                }
            }
        }
    }

    // check for check by pawn
    val forwardDirection = Direction.getForward(king.isWhite)
    for(forwardDiagonalDir in sequenceOf(
            Direction.getDiagonal(forwardDirection, Direction.RIGHT),
            Direction.getDiagonal(forwardDirection, Direction.LEFT)
    )) {
        kingPos.step(forwardDiagonalDir)?.let { forwardDiagonalPos ->
            game.getFigureOrNull(forwardDiagonalPos)?.let { possiblyPawn ->
                if(possiblyPawn.isWhite!=isWhite && possiblyPawn.isPawn()) {
                    checkLines.add(KnightOrPawnCheck(forwardDiagonalPos))
                }
            }
        }
    }

    kingPos.forEachKnightPos {possibleKnightPos->
        game.getFigureOrNull(possibleKnightPos)?.let { figure ->
            if(figure.isKnight()&&figure.isWhite!=isWhite) {
                checkLines.add(KnightOrPawnCheck(possibleKnightPos))
            }
        }
    }

    return AttackLines(checkLines, boundLineByBoundFigurePos)
}

data class PositionProgression(
        private val inclusiveStartPos: Position,
        val size: Int,
        val direction: Direction
): Iterable<Position> {
    init {
        assert(size in 0..7)
    }
    val isEmpty = size==0

    override fun iterator(): Iterator<Position> {
        if(isEmpty) {
            return emptyList<Position>().iterator()
        }
        var numberOfElementsLeftToIterateOver = size
        val sequence = generateSequence({ inclusiveStartPos.takeUnless { isEmpty } }) { currentPos: Position ->
            if(--numberOfElementsLeftToIterateOver==0) {
                return@generateSequence null
            } else {
                return@generateSequence currentPos.step(direction)
            }
        }
        return sequence.iterator()
    }

}
package voidchess.board.check

import sun.plugin.dom.exception.InvalidStateException
import voidchess.board.BasicChessGameInterface
import voidchess.board.getFirstFigureInDir
import voidchess.board.getKing
import voidchess.board.move.Direction
import voidchess.board.move.Position
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashMap


data class AttackAndBoundLinesStatus(val checkLines: List<CheckLine>, val boundLineByBoundFigurePos: Map<Position, BoundLine>) {
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
 * @param possibleMovesTo All positions from attacker to king (inclusive attacker's position, exclusive king's position).
 *                        If the attacker is a knight the list contains only the knight's position.
 * @param kingToAttackerDirection Is null if attacker is a knight.
 */
data class CheckLine(val possibleMovesTo: PositionProgression, val kingToAttackerDirection: Direction?) {
    val canKingTakeAttacker = possibleMovesTo.size==1 && kingToAttackerDirection!=null
}

/**
 * @param possibleMovesTo All positions from attacker to bound figure (inclusive attacker's position, exclusive bound figure's position).
 *                        If the attacker is a knight this is only the knight's position.
 * @param boundFigureToAttackerDirection Can't be null because only bishop, rook and queen can bind.
 */
data class BoundLine(val possibleMovesTo: PositionProgression, val boundFigureToAttackerDirection: Direction)

fun checkAttackLines(game: BasicChessGameInterface, isWhite: Boolean) {
    val king = game.getKing(isWhite)

    val checkLines = LinkedList<CheckLine>()
    val boundLineByBoundFigurePos = HashMap<Position, BoundLine>(8)

    for(straightDir in Direction.straightDirs) {
        game.getFirstFigureInDir(straightDir, king.position)?.let {firstInLine ->
            if( firstInLine.isWhite!=isWhite) {
                assert(!firstInLine.isKing()) {"CheckSearch.fun isCheck(game: BasicChessGameInterface, king: King) should shield this function from having to consider chess by king"}
                if(firstInLine.attacksStraightLine) {
                    checkLines.add(CheckLine(PositionProgression(king.position, firstInLine.position), straightDir))
                } else {
                    game.getFirstFigureInDir(straightDir, king.position)?.let { secondInLine ->
                        if( secondInLine.isWhite!=isWhite && secondInLine.attacksStraightLine) {
                            boundLineByBoundFigurePos[firstInLine.position] = BoundLine(PositionProgression(firstInLine.position, secondInLine.position), straightDir)
                        }
                    }
                }
            }
        }
    }

    TODO("iterate over diagonalDirs (with pawn chess special case) and knightCircle")
}

data class PositionProgression(val exclusiveStartPos: Position, val inclusiveEndPos: Position, val direction: Direction): Iterable<Position> {
    val size = exclusiveStartPos.distanceTo(inclusiveEndPos)
    val containsSinglePosition
        get() = size==1

    constructor(exclusiveStartPos: Position, inclusiveEndPos: Position): this(
            exclusiveStartPos,
            inclusiveEndPos,
            exclusiveStartPos.getDirectionTo(inclusiveEndPos) ?: throw IllegalArgumentException("$exclusiveStartPos and $inclusiveEndPos don't lie on a line")
    )

    override fun iterator(): Iterator<Position> {
        if(containsSinglePosition) {
            return listOf(inclusiveEndPos).iterator()
        }
        var prior: Position = exclusiveStartPos
        var numberOfElementsToIterate = size
        val sequence = generateSequence {
            prior = prior.step(direction)
                    ?: throw InvalidStateException(
                    "provided direction($direction) between start $exclusiveStartPos and end $inclusiveEndPos must and  have been wrong because the computation left the board")
            prior.takeUnless { (numberOfElementsToIterate--)==0 }
        }
        return sequence.iterator()
    }

}
package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.AttackLines
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position
import java.util.*


abstract class Figure constructor(
        // a figure's color
        val isWhite: Boolean,
        var position: Position,
        val type: FigureType,
        val attacksDiagonalLine: Boolean,
        val attacksStraightLine: Boolean
) {

    // encodes type of class + color
    val typeInfo: Int = if (isWhite) type.index else (type.index + 7)
    private val reachableMoves = LinkedList<Move>()

    fun isPawn() = type == FigureType.PAWN
    fun isRook() = type == FigureType.ROOK
    fun isKnight() = type == FigureType.KNIGHT
    fun isBishop() = type == FigureType.BISHOP
    fun isQueen() = type == FigureType.QUEEN
    fun isKing() = type == FigureType.KING

    fun hasDifferentColor(other: Figure) = isWhite != other.isWhite

    open fun canBeHitByEnpasent() = false

    open fun canCastle(): Boolean {
        return false
    }

    open fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) position = move.to
    }

    open fun undoMove(oldPosition: Position) {
        position = oldPosition
    }

    abstract fun isReachable(toPos: Position, game: BasicChessGameInterface): Boolean
    abstract fun countReachableMoves(game: BasicChessGameInterface): Int
    abstract fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>)
    abstract fun isSelectable(game: SimpleChessBoardInterface): Boolean

    fun isMovable(toPos: Position, game: SimpleChessBoardInterface): Boolean {
        return isReachable(toPos, game) && !isBound(toPos, game)
    }

    //TODO rewrite with BoundLines in Mind!
    fun getPossibleMoves(game: SimpleChessBoardInterface, result: MutableList<Move>) {
        reachableMoves.clear()
        getReachableMoves(game, reachableMoves)
        val checkStatus = game.getAttackLines(isWhite)

        for (move in reachableMoves) {
            val checkPosition = move.to
            if (!isBound(checkPosition, game, checkStatus)) {
                result.add(move)
            }
        }
    }

    internal fun isBound(toPos: Position, game: SimpleChessBoardInterface): Boolean {
        assert(isReachable(toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getAttackLines(isWhite)
        return isBound(toPos, game, attackLinesStatus)
    }

    private fun isBound(toPos: Position, game: SimpleChessBoardInterface, attackLines: AttackLines): Boolean {
        if( isKing()) {
            return (this as King).canNotMoveThereBecauseOfCheck(toPos, game, attackLines)
        }

        if(attackLines.noCheck) {
            attackLines.boundLineByBoundFigurePos[position]?.let { boundLine->
                val proposedDirection = position.getDirectionTo(toPos)
                val isProposedDirectionOnBindingLine = proposedDirection==boundLine.boundFigureToAttackerDirection
                        || proposedDirection==boundLine.boundFigureToAttackerDirection.reverse
                return if(isProposedDirectionOnBindingLine) {
                    !(boundLine.possibleMovesToAttacker.contains(toPos) || boundLine.possibleMovesToKing.contains(toPos))
                } else {
                    true
                }
            }
            return false
        }

        if (attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            if(!checkLine.contains(toPos)) {
                return true
            }
            return attackLines.boundLineByBoundFigurePos.containsKey(position)
        }

        // isDoubleCheck!
        return true
    }

    protected inline fun forEachReachablePos(game: BasicChessGameInterface, direction: Direction, informOf: (Position) -> Unit) {
        var currentPos: Position = position

        while (true) {
            currentPos = currentPos.step(direction) ?: return
            val figure = game.getFigureOrNull(currentPos)
            if (figure == null) {
                informOf(currentPos)
            } else {
                if (hasDifferentColor(figure)) {
                    informOf(currentPos)
                }
                return
            }
        }
    }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}
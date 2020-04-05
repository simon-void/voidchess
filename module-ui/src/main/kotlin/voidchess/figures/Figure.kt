package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.AttackLines
import voidchess.board.getFirstFigureInDir
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position


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
    open val canBeHitEnpassant = false

    fun hasDifferentColor(other: Figure) = isWhite != other.isWhite
    open fun canCastle(): Boolean = false

    open fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) position = move.to
    }

    open fun undoMove(oldPosition: Position) {
        position = oldPosition
    }

    abstract fun isReachable(toPos: Position, game: BasicChessBoard): Boolean
    abstract fun isSelectable(game: ChessBoard): Boolean
    abstract fun getReachableMoves(game: BasicChessBoard): Collection<Move>

    fun isMovable(toPos: Position, game: ChessBoard): Boolean = isReachable(toPos, game) && !isBound(toPos, game)

    internal fun isBound(toPos: Position, game: ChessBoard): Boolean {
        assert(isReachable(toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getCachedAttackLines(isWhite)
        return isBound(toPos, game, attackLinesStatus)
    }

    private fun isBound(toPos: Position, game: ChessBoard, attackLines: AttackLines): Boolean {
        if( this is King) {
            return canNotMoveThereBecauseOfCheck(toPos, game, attackLines)
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
            if(!checkLine.posProgression.contains(toPos)) {
                return true
            }
            return attackLines.boundLineByBoundFigurePos.containsKey(position)
        }

        // isDoubleCheck!
        return true
    }

    protected inline fun forEachReachablePos(game: BasicChessBoard, direction: Direction, informOf: (Position) -> Unit) {
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

    protected inline fun forReachableTakeableEndPos(game: BasicChessBoard, direction: Direction, informOf: (Position) -> Unit) {
        game.getFirstFigureInDir(direction, position)?.let { figure ->
            if(hasDifferentColor(figure)) {
                informOf(figure.position)
            }
        }
    }

    protected fun isAccessible(game: BasicChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure == null || hasDifferentColor(figure)
            }

    protected fun containsFigureToTake(game: BasicChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure != null && hasDifferentColor(figure)
            }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}
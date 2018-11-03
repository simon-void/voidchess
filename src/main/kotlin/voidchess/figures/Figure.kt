package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.AttackLines
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


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
    internal abstract fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>)
    abstract fun isSelectable(game: SimpleChessBoardInterface): Boolean

    fun isMovable(toPos: Position, game: SimpleChessBoardInterface): Boolean {
        return isReachable(toPos, game) && !isBound(toPos, game)
    }

    open fun getPossibleMoves(game: SimpleChessBoardInterface, result: MutableList<Move>) {
        val attackLines = game.getCachedAttackLines(isWhite)
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                getReachableMoves(game, result)
            }else{
                getPossibleMovesWhileBoundAndNoCheck(game, boundLine, result)
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                getPossibleMovesWhileUnboundAndCheck(game, checkLine, result)
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    protected abstract fun getPossibleMovesWhileUnboundAndCheck(game: SimpleChessBoardInterface, checkLine: CheckLine, result: MutableList<Move>)
    protected abstract fun getPossibleMovesWhileBoundAndNoCheck(game: SimpleChessBoardInterface, boundLine: BoundLine, result: MutableList<Move>)

    protected fun addMoveIfReachable(pos: Position, game: BasicChessGameInterface, result: MutableList<Move>) =
            if(isReachable(pos, game)) result.add(Move[position, pos])
            else false

    internal fun isBound(toPos: Position, game: SimpleChessBoardInterface): Boolean {
        assert(isReachable(toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getCachedAttackLines(isWhite)
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
            if(!checkLine.posProgression.contains(toPos)) {
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

    protected fun isAccessible(game: BasicChessGameInterface, position: Position): Boolean {
        val figure = game.getFigureOrNull(position)
        return if (figure == null) true else hasDifferentColor(figure)
    }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}
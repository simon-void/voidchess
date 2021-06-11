package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.AttackLines
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getFirstFigureInDir
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position

typealias MoveInformer = (Move) -> Unit
typealias PositionInformer = (Position) -> Unit

abstract class Figure(
        // a figure's color
    val isWhite: Boolean,
    val type: FigureType
) {

    // encodes type of class + color
    val typeInfo: Int = if (isWhite) type.index else (type.index + 7)

    fun hasDifferentColor(other: Figure) = isWhite != other.isWhite

    abstract fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean
    abstract fun countReachableMoves(position: Position, game: StaticChessBoard): Int
    abstract fun isSelectable(position: Position, game: ChessBoard): Boolean
    internal open fun forReachableMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun forReachableTakingMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun forReachableCheckingMoves(position: Position, game: ChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")

    // to figure out castling ability
    open fun figureMoved() {}
    open fun undoMove() {}
    open fun canCastle() = false

    fun isMovable(
        position: Position,
        toPos: Position,
        game: ChessBoard,
    ): Boolean {
        return isReachable(position, toPos, game) && !isBound(position, toPos, game)
    }

    open fun forPossibleMoves(
        position: Position,
        game: ChessBoard,
        informOf: MoveInformer,
    ) {
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forReachableMoves(position, game, informOf)
            }else{
                forPossibleMovesWhileBoundAndNoCheck(position, game, boundLine, informOf)
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forPossibleMovesWhileUnboundAndCheck(position, game, checkLine, informOf)
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    /**
     * let's only look for checks to give if our king is not in check.
     * (for minimizing the code complexity)
     */
    //TODO remove
    open fun forPossibleTakingMoves(
        position: Position,
        game: ChessBoard,
        informOf: MoveInformer,
    ) {
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forReachableTakingMoves(position, game, informOf)
            }else{
                if(isReachable(position, boundLine.attackerPos, game)) {
                    informOf(Move[position, boundLine.attackerPos])
                }
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null && isReachable(position, checkLine.attackerPos, game)) {
                informOf(Move[position, checkLine.attackerPos])
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    /**
     * moves are critical if they
     * 1) can't be reversed (e.g. a figure is taken, a king castles or a pawn move)
     * 2) if a knight forks
     * 3) if a check is given
     *
     * because critical moves aren't unique, they are collected in a Set.
     */
    //TODO remove
    open fun forCriticalMoves(
        position: Position,
        game: ChessBoard,
        result: MutableSet<Move>,
    ) {
        // this doesn't make sense for king; and pawn and knight overwrite getCriticalMoves altogether.
        if(this is Pawn||this is Knight||this is King) throw IllegalStateException("Pawn, King and Knight should override this method, but ${javaClass.simpleName} didn't")

        forPossibleTakingMoves(position, game) {
            result.add(it)
        }

        // for simplicity lets only consider checks while not in check and figure unbound
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            if(attackLines.boundLineByBoundFigurePos[position]==null) {
                forReachableCheckingMoves(position, game) {
                    result.add(it)
                }
            }
        }
    }

    protected abstract fun forPossibleMovesWhileUnboundAndCheck(position: Position, game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer)
    protected abstract fun forPossibleMovesWhileBoundAndNoCheck(position: Position, game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer)

    protected fun addMoveIfReachable(
        position: Position,
        toPos: Position,
        game: StaticChessBoard,
        informOf: MoveInformer,
    ) =
        if (isReachable(position, toPos, game)) {
            informOf(Move[position, toPos])
            true
        } else false

    internal fun isBound(
        position: Position,
        toPos: Position,
        game: ChessBoard,
    ): Boolean {
        assert(isReachable(position, toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getCachedAttackLines()
        return isBound(position, toPos, game, attackLinesStatus)
    }

    private fun isBound(
        position: Position,
        toPos: Position,
        game: ChessBoard,
        attackLines: AttackLines
    ): Boolean {
        if( this is King) {
            return canNotMoveThereBecauseOfCheck(position, toPos, game, attackLines)
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

    protected inline fun forEachReachablePos(
        position: Position,
        game: StaticChessBoard,
        direction: Direction,
        informOf: PositionInformer,
    ) {
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

    protected inline fun forReachableTakeableEndPos(
        position: Position,
        game: StaticChessBoard,
        direction: Direction,
        informOf: PositionInformer,
    ) {
        game.getFirstFigureInDir(direction, position)?.let { (figure, pos) ->
            if(figure.isWhite!=isWhite) {
                informOf(pos)
            }
        }
    }

    protected fun isAccessible(game: StaticChessBoard, otherPos: Position) =
            game.getFigureOrNull(otherPos).let { figure ->
                figure == null || hasDifferentColor(figure)
            }

    protected fun containsFigureToTake(game: StaticChessBoard, otherPos: Position) =
            game.getFigureOrNull(otherPos).let { figure ->
                figure != null && hasDifferentColor(figure)
            }

    open fun toStringBuilder()= StringBuilder(20).apply{
        append(type.label)
        append('-')
        if (isWhite) append("white") else append("black")
    }

    override fun toString() = toStringBuilder().toString()
    
    override fun equals(other: Any?) = error("equals is unsupported for figure, since position is unknown")//other is Figure && typeInfo == other.typeInfo && isWhite==other.isWhite
    override fun hashCode() = typeInfo
}

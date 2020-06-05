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

    open fun canCastle(): Boolean {
        return false
    }

    open fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) position = move.to
    }

    open fun undoMove(oldPosition: Position) {
        position = oldPosition
    }

    abstract fun isReachable(toPos: Position, game: StaticChessBoard): Boolean
    abstract fun countReachableMoves(game: StaticChessBoard): Int
    abstract fun isSelectable(game: ChessBoard): Boolean
    internal open fun forReachableMoves(game: StaticChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun forReachableTakingMoves(game: StaticChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun forReachableCheckingMoves(game: ChessBoard, informOf: MoveInformer): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")

    fun isMovable(toPos: Position, game: ChessBoard): Boolean {
        return isReachable(toPos, game) && !isBound(toPos, game)
    }

    open fun forPossibleMoves(game: ChessBoard, informOf: MoveInformer) {
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forReachableMoves(game, informOf)
            }else{
                forPossibleMovesWhileBoundAndNoCheck(game, boundLine, informOf)
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forPossibleMovesWhileUnboundAndCheck(game, checkLine, informOf)
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    /**
     * let's only look for checks to give if our king is not in check.
     * (for minimizing the code complexity)
     */
    //TODO remove
    open fun forPossibleTakingMoves(game: ChessBoard, informOf: MoveInformer) {
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                forReachableTakingMoves(game, informOf)
            }else{
                if(isReachable(boundLine.attackerPos, game)) {
                    informOf(Move[position, boundLine.attackerPos])
                }
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null && isReachable(checkLine.attackerPos, game)) {
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
    open fun forCriticalMoves(game: ChessBoard, result: MutableSet<Move>) {
        // this doesn't make sense for king; and pawn and knight overwrite getCriticalMoves altogether.
        if(this is Pawn||this is Knight||this is King) throw IllegalStateException("Pawn, King and Knight should override this method, but ${javaClass.simpleName} didn't")

        forPossibleTakingMoves(game) {
            result.add(it)
        }

        // for simplicity lets only consider checks while not in check and figure unbound
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            if(attackLines.boundLineByBoundFigurePos[position]==null) {
                forReachableCheckingMoves(game) {
                    result.add(it)
                }
            }
        }
    }

    protected abstract fun forPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer)
    protected abstract fun forPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer)

    protected fun addMoveIfReachable(pos: Position, game: StaticChessBoard, informOf: MoveInformer) =
        if (isReachable(pos, game)) {
            informOf(Move[position, pos])
            true
        } else false

    internal fun isBound(toPos: Position, game: ChessBoard): Boolean {
        assert(isReachable(toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getCachedAttackLines()
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

    protected inline fun forEachReachablePos(game: StaticChessBoard, direction: Direction, informOf: PositionInformer) {
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

    protected inline fun forReachableTakeableEndPos(game: StaticChessBoard, direction: Direction, informOf: PositionInformer) {
        game.getFirstFigureInDir(direction, position)?.let { figure ->
            if(figure.isWhite!=isWhite) {
                informOf(figure.position)
            }
        }
    }

    protected fun isAccessible(game: StaticChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure == null || hasDifferentColor(figure)
            }

    protected fun containsFigureToTake(game: StaticChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure != null && hasDifferentColor(figure)
            }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}
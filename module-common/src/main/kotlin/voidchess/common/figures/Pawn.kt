package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getFigure
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.math.abs
import kotlin.math.sign


class Pawn : Figure {
    private val forwardDirection = Direction.getForward(isWhite)
    private val startingRow = if(isWhite) 1 else 6
    override var canBeHitEnpassant: Boolean = false

    /**
     * attacksDiagonalLine set to false because while the pawn attacks diagonally, he doesn't attack a line
     */
    constructor(isWhite: Boolean, position: Position) : super(isWhite, position, FigureType.PAWN, false, false) {
        canBeHitEnpassant = false
    }

    constructor(isWhite: Boolean, position: Position, canBeHitByEnpassant: Boolean)
            : super(isWhite, position, FigureType.PAWN, false, false) {
        this.canBeHitEnpassant = canBeHitByEnpassant
    }

    private fun hasNotMovedYet() = position.row == startingRow

    private inline fun forEachReachablePos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachDiagonalReachablePos(game, informOf)
        forEachVerticalReachablePos(game, informOf)
    }

    private inline fun forEachDiagonalReachablePos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        listOf(Direction.LEFT, Direction.RIGHT).forEach { side: Direction ->
            val diagonal = Direction.getDiagonal(forwardDirection, side)
            position.step(diagonal)?.let { diagonalPos ->
                if(isDiagonalReachable(diagonalPos, game)) {
                    informOf(diagonalPos)
                }
            }
        }
    }

    private inline fun forEachVerticalReachablePos(game: StaticChessBoard, informOf: (Position) -> Unit) {
        val oneForwardPos = position.step(forwardDirection)!! // !! ok because pawns never stand on the last rank
        if(game.isFreeArea(oneForwardPos)) {
            informOf(oneForwardPos)
            // maybe also two steps possible?
            if(hasNotMovedYet()){
                val twoForwardPos = oneForwardPos.step(forwardDirection)!!
                if(game.isFreeArea(twoForwardPos)) {
                    informOf(twoForwardPos)
                }
            }
        }
    }

    override fun isReachable(toPos: Position, game: StaticChessBoard): Boolean {
        return isStraightReachable(toPos, game) || isDiagonalReachable(toPos, game)
    }

    private fun isStraightReachable(to: Position, game: StaticChessBoard): Boolean {
        if (to.column != position.column) {
            return false
        }
        val oneForwardPos = position.step(forwardDirection) ?: throw IllegalStateException("pawn on last rank at position $position")
        val oneForwardFree = game.isFreeArea(oneForwardPos)
        if( !oneForwardFree) {
            return false
        }
        if(oneForwardPos.equalsPosition(to)) {
            return true
        }
        if(position.row==startingRow) {
            val twoForwardPos = oneForwardPos.step(forwardDirection)!!
            if(twoForwardPos.equalsPosition(to) && game.isFreeArea(to)) {
                return true
            }
        }
        return false
    }

    private fun isDiagonalReachable(to: Position, game: StaticChessBoard): Boolean {
        if (!isOneStepForwardDiagonally(to)) return false
        // now we know that to is one step diagonal to us
        game.getFigureOrNull(to)?.let { if (hasDifferentColor(it)) return true }
        // ok, so no simple diagonal strike, maybe enpassant
        val sidePos = Position[position.row, to.column]
        game.getFigureOrNull(sidePos)?.let { sideFigure->
            if (sideFigure.canBeHitEnpassant) return true
        }
        return false
    }

    private fun isOneStepForwardDiagonally(to: Position): Boolean {
        val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
        return to.row == oneForwardRow && abs(to.column - position.column) == 1
    }

    override fun getReachableMoves(game: StaticChessBoard, result: MutableCollection<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getReachableTakingMoves(game: StaticChessBoard, result: MutableCollection<Move>) {
        forEachDiagonalReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getCriticalMoves(game: ChessBoard, result: MutableSet<Move>) =
            getPossibleMoves(game, result)

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>) {
        fun checkLinePassesThroughPawnColumn(attackerPos: Position, kingPos: Position): Boolean {
            val attackerColumnSign = (position.column - attackerPos.column).sign
            val kingColumnSign = (position.column - kingPos.column).sign
            return attackerColumnSign == -kingColumnSign && kingColumnSign != 0
        }
        // a pawn can only intercept a check by taking the attacker diagonally (possibly through enpassent)
        if (isOneStepForwardDiagonally(checkLine.attackerPos)) {
            result.add(Move[position, checkLine.attackerPos])
        } else if (game.getFigure(checkLine.attackerPos).canBeHitEnpassant) {
            // now i only have to test if my pawn is to the side of the attacker pawn
            if (position.row == checkLine.attackerPos.row && abs(position.column - checkLine.attackerPos.column) == 1) {
                val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
                result.add(Move[position, Position[oneForwardRow, checkLine.attackerPos.column]])
            }
        }
        // or/and by stepping forward into the line
        if (checkLinePassesThroughPawnColumn(checkLine.attackerPos, checkLine.kingPos)) {
            // try to stop check by moving forward
            if (!checkLine.posProgression.hasSinglePos) {
                checkLine.posProgression.forEachReachablePos { interceptPos->
                    if (isStraightReachable(interceptPos, game)) {
                        result.add(Move[position, interceptPos])
                        return
                    }
                }
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>) {
        when {
            boundLine.boundFigureToAttackerDirection.isDiagonal -> {
                if(isOneStepForwardDiagonally(boundLine.attackerPos)) {
                    result.add(Move[position, boundLine.attackerPos])
                }
            }
            boundLine.boundFigureToAttackerDirection.isVertical -> {
                val oneForwardPos = position.step(forwardDirection) ?: throw IllegalStateException("pawn left board with single step from $position")
                if( game.isFreeArea(oneForwardPos)) {
                    result.add(Move[position, oneForwardPos])
                    if(hasNotMovedYet()) {
                        val twoForwardPos = oneForwardPos.step(forwardDirection) ?: throw IllegalStateException("pawn left board with double step from $position")
                        if( game.isFreeArea(twoForwardPos)) {
                            result.add(Move[position, twoForwardPos])
                        }
                    }
                }
            }
            // else the pawn can't move
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: StaticChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

    override fun toString() = "${super.toString()}-$canBeHitEnpassant"
}

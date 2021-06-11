package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.canBeHitEnpassant
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import kotlin.math.abs
import kotlin.math.sign

/**
 * attacksDiagonalLine set to false because while the pawn attacks diagonally, he doesn't attack a line
 */
class Pawn(isWhite: Boolean) : Figure(
    isWhite = isWhite,
    type = FigureType.PAWN,
) {
    private val forwardDirection = Direction.getForward(isWhite)
    private val startingRow = if(isWhite) 1 else 6

    private fun hasNotPawnMovedYet(position: Position) = position.row == startingRow

    private inline fun forEachReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        forEachDiagonalReachablePos(position, game, informOf)
        forEachVerticalReachablePos(position, game, informOf)
    }

    private inline fun forEachDiagonalReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        listOf(Direction.LEFT, Direction.RIGHT).forEach { side: Direction ->
            val diagonal = Direction.getDiagonal(forwardDirection, side)
            position.step(diagonal)?.let { diagonalPos ->
                if(isDiagonalReachable(position, diagonalPos, game)) {
                    informOf(diagonalPos)
                }
            }
        }
    }

    private inline fun forEachVerticalReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        val oneForwardPos = position.step(forwardDirection)!! // !! ok because pawns never stand on the last rank
        if(game.isFreeArea(oneForwardPos)) {
            informOf(oneForwardPos)
            // maybe also two steps possible?
            if(hasNotPawnMovedYet(position)){
                val twoForwardPos = oneForwardPos.step(forwardDirection)!!
                if(game.isFreeArea(twoForwardPos)) {
                    informOf(twoForwardPos)
                }
            }
        }
    }

    override fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        return isStraightReachable(position, toPos, game) || isDiagonalReachable(position, toPos, game)
    }

    private fun isStraightReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        if (toPos.column != position.column) {
            return false
        }
        val oneForwardPos = position.step(forwardDirection) ?: throw IllegalStateException("pawn on last rank at position $position")
        val oneForwardFree = game.isFreeArea(oneForwardPos)
        if( !oneForwardFree) {
            return false
        }
        if(oneForwardPos.equalsPosition(toPos)) {
            return true
        }
        if(position.row==startingRow) {
            val twoForwardPos = oneForwardPos.step(forwardDirection)!!
            if(twoForwardPos.equalsPosition(toPos) && game.isFreeArea(toPos)) {
                return true
            }
        }
        return false
    }

    private fun isDiagonalReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        if (!isOneStepForwardDiagonally(position, toPos)) return false
        // now we know that to is one step diagonal to us
        game.getFigureOrNull(toPos)?.let { if (hasDifferentColor(it)) return true }
        // ok, so no simple diagonal strike, maybe enpassant
        return game.enpassantPos?.equalsCoordinates(
            row = position.row,
            column = toPos.column
        ) ?: false
    }

    private fun isOneStepForwardDiagonally(position: Position, toPos: Position): Boolean {
        val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
        return toPos.row == oneForwardRow && abs(toPos.column - position.column) == 1
    }

    override fun forReachableMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(position, game) { to ->
            informOf(getPromotionSafeMove(position, to))
        }
    }

    override fun forReachableTakingMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachDiagonalReachablePos(position, game) { to ->
            informOf(getPromotionSafeMove(position, to))
        }
    }

    override fun forCriticalMoves(position: Position, game: ChessBoard, result: MutableSet<Move>) {
        forPossibleMoves(position, game) {
            result.add(it)
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(
        position: Position,
        game: ChessBoard,
        checkLine: CheckLine,
        informOf: MoveInformer
    ) {
        fun checkLinePassesThroughPawnColumn(position: Position, attackerPos: Position, kingPos: Position): Boolean {
            val attackerColumnSign = (position.column - attackerPos.column).sign
            val kingColumnSign = (position.column - kingPos.column).sign
            return attackerColumnSign == -kingColumnSign && kingColumnSign != 0
        }
        // a pawn can only intercept a check by taking the attacker diagonally (possibly through enpassent)
        if (isOneStepForwardDiagonally(position, checkLine.attackerPos)) {
            informOf(getPromotionSafeMove(position, checkLine.attackerPos))
        } else if (game.canBeHitEnpassant(checkLine.attackerPos)) {
            // now i only have to test if my pawn is to the side of the attacker pawn
            if (position.row == checkLine.attackerPos.row && abs(position.column - checkLine.attackerPos.column) == 1) {
                val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
                informOf(getPromotionSafeMove(position, Position[oneForwardRow, checkLine.attackerPos.column]))
            }
        }
        // or/and by stepping forward into the line
        if (checkLinePassesThroughPawnColumn(position, checkLine.attackerPos, checkLine.kingPos)) {
            // try to stop check by moving forward
            if (!checkLine.posProgression.hasSinglePos) {
                checkLine.posProgression.forEachReachablePos { interceptPos->
                    if (isStraightReachable(position, interceptPos, game)) {
                        informOf(getPromotionSafeMove(position, interceptPos))
                        return
                    }
                }
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(
        position: Position,
        game: ChessBoard,
        boundLine: BoundLine,
        informOf: MoveInformer
    ) {
        when {
            boundLine.boundFigureToAttackerDirection.isDiagonal -> {
                if(isOneStepForwardDiagonally(position, boundLine.attackerPos)) {
                    informOf(getPromotionSafeMove(position, boundLine.attackerPos))
                }
            }
            boundLine.boundFigureToAttackerDirection.isVertical -> {
                val oneForwardPos = position.step(forwardDirection) ?: throw IllegalStateException("pawn left board with single step from $position")
                if( game.isFreeArea(oneForwardPos)) {
                    informOf(getPromotionSafeMove(position, oneForwardPos))
                    if(hasNotPawnMovedYet(position)) {
                        val twoForwardPos = oneForwardPos.step(forwardDirection) ?: throw IllegalStateException("pawn left board with double step from $position")
                        if( game.isFreeArea(twoForwardPos)) {
                            informOf(getPromotionSafeMove(position, twoForwardPos))
                        }
                    }
                }
            }
            // else the pawn can't move
        }
    }

    override fun isSelectable(position: Position, game: ChessBoard): Boolean {
        forEachReachablePos(position, game) { reachablePos ->
            if (!isBound(position, reachablePos, game)) return true
        }
        return false
    }

    override fun countReachableMoves(position: Position, game: StaticChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(position, game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }
}

// instead of only returning a move for the Queen, should be a second one for the Knight (and a third for the Rook) be returned?
private fun getPromotionSafeMove(from: Position, to: Position): Move = if (to.row == 0 || to.row == 7) {
    Move[from, to, PawnPromotion.QUEEN]
} else {
    Move[from, to]
}

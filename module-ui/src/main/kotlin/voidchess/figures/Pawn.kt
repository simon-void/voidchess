package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.math.abs


class Pawn : Figure {
    private val forwardDirection = Direction.getForward(isWhite)
    private val startingRow = if(isWhite) 1 else 6
    override var canBeHitEnpassant: Boolean = false

    /**
     * attacksDiagonalLine set to false because while the pawn attacks diagonally, he doesn't attack a line
     */
    constructor(isWhite: Boolean, position: Position) : super(isWhite, position,
        FigureType.PAWN, false, false) {
        canBeHitEnpassant = false
    }

    constructor(isWhite: Boolean, position: Position, canBeHitByEnpassant: Boolean)
            : super(isWhite, position, FigureType.PAWN, false, false) {
        this.canBeHitEnpassant = canBeHitByEnpassant
    }

    private fun hasNotMovedYet() = position.row == startingRow

    override fun figureMoved(move: Move) {
        canBeHitEnpassant = move.from.equalsPosition(position) && abs(move.from.row - move.to.row) == 2
        super.figureMoved(move)
    }

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)
        canBeHitEnpassant = false
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forEachDiagonalReachablePos(game, informOf)
        forEachVerticalReachablePos(game, informOf)
    }

    private inline fun forEachDiagonalReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        listOf(Direction.LEFT, Direction.RIGHT).forEach { side: Direction ->
            val diagonal = Direction.getDiagonal(forwardDirection, side)
            position.step(diagonal)?.let { diagonalPos ->
                if(isDiagonalReachable(diagonalPos, game)) {
                    informOf(diagonalPos)
                }
            }
        }
    }

    private inline fun forEachVerticalReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
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

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        return isStraightReachable(toPos, game) || isDiagonalReachable(toPos, game)
    }

    private fun isStraightReachable(to: Position, game: BasicChessBoard): Boolean {
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

    private fun isDiagonalReachable(to: Position, game: BasicChessBoard): Boolean {
        if (!isOneStepForwardDiagonally(to)) return false
        // now we know that to is one step diagonal to us
        game.getFigureOrNull(to)?.let { if (hasDifferentColor(it)) return true }
        // ok, so no simple diagonal strike, maybe enpassent
        val sidePos = Position[position.row, to.column]
        game.getFigureOrNull(sidePos)?.let { if (it.canBeHitEnpassant) return true }
        return false
    }

    private fun isOneStepForwardDiagonally(to: Position): Boolean {
        val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
        return to.row == oneForwardRow && abs(to.column - position.column) == 1
    }

    override fun getReachableMoves(game: BasicChessBoard): Collection<Move> = ArrayList<Move>(4).apply {
        forEachReachablePos(game) {
            add(Move[position, it])
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun toString() = "${super.toString()}-$canBeHitEnpassant"
}

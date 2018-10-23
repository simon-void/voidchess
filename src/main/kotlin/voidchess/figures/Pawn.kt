package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position
import java.util.*


class Pawn : Figure {
    private val forwardDirection = Direction.getForward(isWhite)
    private val startingRow = if(isWhite) 1 else 6
    private var canBeHitByEnpasent: Boolean = false

    /**
     * attacksDiagonalLine set to false because while the pawn attacks diagonally, he doesn't attack a line
     */
    constructor(isWhite: Boolean, position: Position) : super(isWhite, position, FigureType.PAWN, false, false) {
        canBeHitByEnpasent = false
    }

    constructor(isWhite: Boolean, position: Position, canBeHitByEnpasent: Boolean)
            : super(isWhite, position, FigureType.PAWN, false, false) {
        this.canBeHitByEnpasent = canBeHitByEnpasent
    }

    override fun canBeHitByEnpasent() = canBeHitByEnpasent
    fun setCanBeHitByEnpasent() {
        canBeHitByEnpasent = true
    }

    private fun hasntMovedYet() = position.row == startingRow

    override fun figureMoved(move: Move) {
        canBeHitByEnpasent = move.from.equalsPosition(position) && Math.abs(move.from.row - move.to.row) == 2
        super.figureMoved(move)
    }

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)
        canBeHitByEnpasent = false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        forEachDiagonalReachablePos(game, informOf)
        forEachVerticalReachablePos(game, informOf)
    }

    private inline fun forEachDiagonalReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        Arrays.asList(Direction.LEFT, Direction.RIGHT).forEach { side: Direction ->
            val diagonal = Direction.getDiagonal(forwardDirection, side)
            position.step(diagonal)?.let { diagonalPos ->
                if(isDiagonalReachable(diagonalPos, game)) {
                    informOf(diagonalPos)
                }
            }
        }
    }

    private inline fun forEachVerticalReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        val oneForwardPos = position.step(forwardDirection)!! // !! ok because pawns never stand on the last rank
        if(game.isFreeArea(oneForwardPos)) {
            informOf(oneForwardPos)
            // maybe also two steps possible?
            if(hasntMovedYet()){
                val twoForwardPos = oneForwardPos.step(forwardDirection)!!
                if(game.isFreeArea(twoForwardPos)) {
                    informOf(twoForwardPos)
                }
            }
        }
    }

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        return isStraightReachable(to, game) || isDiagonalReachable(to, game)
    }

    private fun isStraightReachable(to: Position, game: BasicChessGameInterface): Boolean {
        if (to.column != position.column) {
            return false
        }
        val oneForwardPos = position.step(forwardDirection) ?: position // ?: ok because pawns never stand on the last rank
        val oneForwardFree = game.isFreeArea(oneForwardPos)
        if( !oneForwardFree) {
            return false
        }
        if(oneForwardPos.equalsPosition(to)) {
            return true
        }
        val pawnDoubleStepRow = if (isWhite) 3 else 4
        if(to.row!=pawnDoubleStepRow) {
            return false
        }
        return game.isFreeArea(to)
    }

    private fun isDiagonalReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val oneForwardRow = if (isWhite) position.row + 1 else position.row - 1
        if(to.row!=oneForwardRow || Math.abs(to.column-position.column)!=1) return false
        // now we know that to is one step diagonal to us
        game.getFigureOrNull(to)?.let { if (hasDifferentColor(it)) return true }
        // ok, so no simple diagonal strike, maybe enpassent
        val sidePos = Position[position.row, to.column]
        game.getFigureOrNull(sidePos)?.let { if (it.canBeHitByEnpasent()) return true }
        return false
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessGameInterface): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

    override fun toString() = "${super.toString()}-$canBeHitByEnpasent"
}

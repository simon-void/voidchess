package voidchess.figures

import voidchess.board.move.Move
import voidchess.board.move.Position


abstract class CastlingFigure(isWhite: Boolean, val initialPosition: Position, protected var stepsTaken: Int, type: FigureType) : Figure(isWhite, initialPosition, type) {

    constructor(isWhite: Boolean, startPosition: Position, type: FigureType) : this(isWhite, startPosition, 0, type)

    override fun undoMove(oldPosition: Position) {
        stepsTaken--
        super.undoMove(oldPosition)
    }

    override fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) stepsTaken++
        super.figureMoved(move)
    }

    override fun canCastle() = stepsTaken == 0
    override fun toString() = "${super.toString()}-$stepsTaken"
}

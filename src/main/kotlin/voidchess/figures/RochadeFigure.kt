package voidchess.figures

import voidchess.helper.Move
import voidchess.helper.Position

/**
 * @author stephan
 */
abstract class RochadeFigure(isWhite: Boolean, val initialPosition: Position, protected var stepsTaken: Int, type: FigureType) : Figure(isWhite, initialPosition, type) {

    constructor(isWhite: Boolean, startPosition: Position, type: FigureType) : this(isWhite, startPosition, 0, type)

    override fun undoMove(oldPosition: Position) {
        stepsTaken--
        super.undoMove(oldPosition)
    }

    override fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) stepsTaken++
        super.figureMoved(move)
    }

    override fun canParticipateInRochade() = stepsTaken == 0
    override fun toString() = "${super.toString()}-$stepsTaken"
}

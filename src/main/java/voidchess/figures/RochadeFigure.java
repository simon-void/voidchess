package voidchess.figures;

import voidchess.helper.Move;
import voidchess.helper.Position;

/**
 * @author stephan
 */
abstract public class RochadeFigure extends Figure {
    protected int stepsTaken;
    final private Position initialPos;

    public RochadeFigure(boolean isWhite, Position startPosition, byte typeIndex) {
        this(isWhite, startPosition, 0, typeIndex);
    }

    public RochadeFigure(boolean isWhite, Position startPosition, int stepsTaken, byte typeIndex) {
        super(isWhite, startPosition, typeIndex);
        initialPos = startPosition;
        this.stepsTaken = stepsTaken;
    }

    public Position getInitialPosition() {
        return initialPos;
    }

    public boolean canParticipateInRochade() {
        return stepsTaken == 0;
    }

    public void undoMove(Position oldPosition) {
        stepsTaken--;
        super.undoMove(oldPosition);
    }

    public void figureMoved(Move move) {
        if (position.equalsPosition(move.from)) stepsTaken++;
        super.figureMoved(move);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(20);
        buffer.append(super.toString());
        buffer.append("-");
        buffer.append(stepsTaken);
        return buffer.toString();
    }

}

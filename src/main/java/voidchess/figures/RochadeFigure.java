package voidchess.figures;

import voidchess.helper.Move;
import voidchess.helper.Position;

/**
 * @author stephan
 */
abstract public class RochadeFigure extends Figure {
    protected int stepsTaken;
    final private Position initialPos;

    public RochadeFigure(boolean isWhite, Position startPosition, FigureType type) {
        this(isWhite, startPosition, 0, type);
    }

    public RochadeFigure(boolean isWhite, Position startPosition, int stepsTaken, FigureType type) {
        super(isWhite, startPosition, type);
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
        if (getPosition().equalsPosition(move.getFrom())) stepsTaken++;
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

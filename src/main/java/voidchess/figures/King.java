package voidchess.figures;

import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.helper.CheckSearch;
import voidchess.helper.Move;
import voidchess.helper.Position;
import voidchess.image.ImageType;

import java.util.List;

/**
 * @author stephan
 */
public class King extends RochadeFigure {
    private boolean didRochade;

    public King(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, (byte) 6);
        didRochade = false;
    }

    public King(boolean isWhite, Position startPosition, int stepsTaken, boolean didRochade) {
        super(isWhite, startPosition, stepsTaken, (byte) 6);
        this.didRochade = didRochade;
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        int horizontal_difference = Math.abs(position.getRow() - to.getRow());
        int vertical_difference = Math.abs(position.getColumn() - to.getColumn());
        if (horizontal_difference <= 1 && vertical_difference <= 1) {
            if (game.isFreeArea(to) || hasDifferentColor(game.getFigure(to))) return true;
        }
        if (((position.getRow() == 0 && isWhite) || (position.getRow() == 7 && !isWhite)) && horizontal_difference == 0) {
            if (isShortRochadeReachable(to, game)) return true;
            if (isLongRochadeReachable(to, game)) return true;
        }
        return false;
    }

    private boolean isShortRochadeReachable(Position to, BasicChessGameInterface game) {
        if (canParticipateInRochade() &&
                !game.isFreeArea(to) &&
                to.getColumn() > position.getColumn()) {
            int ground_row = isWhite() ? 0 : 7;
            Figure toFigure = game.getFigure(to);

            if (toFigure.canParticipateInRochade() && toFigure.isWhite == isWhite) {
                if (position.getColumn() == 6) {
                    if (!game.isFreeArea(Position.Companion.get(ground_row, 5))) {
                        return false;
                    }
                } else {
                    //Die Felder bis zur g-Spalte müssen bis auf den Turm leer sein
                    for (int column = position.getColumn() + 1; column < 7; column++) {
                        Position midlePosition = Position.Companion.get(ground_row, column);
                        if (!game.isFreeArea(midlePosition) &&
                                !game.getFigure(midlePosition).canParticipateInRochade()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isLongRochadeReachable(Position to, BasicChessGameInterface game) {
        if (canParticipateInRochade() &&
                !game.isFreeArea(to) &&
                to.getColumn() < position.getColumn()) {
            int ground_row = isWhite() ? 0 : 7;
            Figure toFigure = game.getFigure(to);

            if (toFigure.canParticipateInRochade() && toFigure.isWhite == isWhite) {
                //kommt der König auf die c-Linie?
                if (position.getColumn() == 1) {        //auf der a-Linie kann der König nicht stehen, da dort Turm sein muß
                    if (!game.isFreeArea(Position.Companion.get(ground_row, 2))) {
                        return false;
                    }
                } else if (position.getColumn() > 2) {
                    //Die Felder bis zur c-Spalte müssen bis auf den Turm leer sein
                    for (int column = position.getColumn() - 1; column >= 2; column--) {
                        Position midlePosition = Position.Companion.get(ground_row, column);
                        if (!game.isFreeArea(midlePosition) &&
                                !game.getFigure(midlePosition).canParticipateInRochade()) {
                            return false;
                        }
                    }
                }
                //kommt der Turm auf die d-Linie?
                if (to.getColumn() != 3) {
                    final int step = CheckSearch.signum(3 - to.getColumn());
                    Position midlePosition;
                    Figure midleFigure;
                    for (int column = to.getColumn() + step; column != 3; column += step) {
                        midlePosition = Position.Companion.get(ground_row, column);
                        if (!game.isFreeArea(midlePosition)) {
                            midleFigure = game.getFigure(midlePosition);
                            if (!midleFigure.canParticipateInRochade() ||
                                    midleFigure.isRock()) {
                                return false;
                            }
                        }
                    }
                    midlePosition = Position.Companion.get(ground_row, 3);
                    if (!game.isFreeArea(midlePosition)) {
                        midleFigure = game.getFigure(midlePosition);
                        if (!midleFigure.canParticipateInRochade() ||
                                midleFigure.isRock()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isPassiveBound(Position to, SimpleChessBoardInterface game) {
        Position realTo = to;
        if (!game.isFreeArea(to) && game.getFigure(to).canParticipateInRochade()) {
            final int column = (to.getColumn() - position.getColumn()) > 0 ? 6 : 2;
            realTo = Position.Companion.get(to.getRow(), column);
            if (CheckSearch.isCheck(game, position)) return true;
            if (isKingAtCheckInbetweenRochade(position, realTo, game)) return true;
        }
        return isKingCheckAt(realTo, game);
    }

    private boolean isKingAtCheckInbetweenRochade(
            Position from,
            Position to,
            SimpleChessBoardInterface game
    ) {
        assert from.getRow() == to.getRow();

        final int step = CheckSearch.signum(to.getColumn() - from.getColumn());
        for (int column = from.getColumn() + step; column != to.getColumn(); column += step) {
            if (isKingCheckAt(Position.Companion.get(from.getRow(), column), game)) return true;
        }
        return false;
    }

    private boolean isKingCheckAt(Position to, SimpleChessBoardInterface game) {
        game.setFigure(position, null);
        Figure hitFigure = game.getFigure(to);
        game.setFigure(to, this);
        boolean isCheck = CheckSearch.isCheck(game, to);
        game.setFigure(to, hitFigure);
        game.setFigure(position, this);
        return isCheck;
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        int minRow = Math.max(position.getRow() - 1, 0);
        int minColumn = Math.max(position.getColumn() - 1, 0);
        int maxRow = Math.min(position.getRow() + 1, 7);
        int maxColumn = Math.min(position.getColumn() + 1, 7);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                Position checkPosition = Position.Companion.get(row, column);
                if (!checkPosition.equalsPosition(position) &&
                        (game.isFreeArea(checkPosition) ||
                                game.getFigure(checkPosition).isWhite != isWhite)
                ) {
                    result.add(Move.get(position, checkPosition));
                }
            }
        }

        if (canParticipateInRochade()) {
            for (int column = position.getColumn() + 1; column < 8; column++) {
                Position pos = Position.Companion.get(position.getRow(), column);
                if (!game.isFreeArea(pos)) {
                    if (game.getFigure(pos).canParticipateInRochade() &&
                            isReachable(pos, game)) {

                        result.add(Move.get(position, pos));
                    }
                    break;
                }
            }
            for (int column = position.getColumn() - 1; column >= 0; column--) {
                Position pos = Position.Companion.get(position.getRow(), column);
                if (!game.isFreeArea(pos)) {
                    if (game.getFigure(pos).canParticipateInRochade() &&
                            isReachable(pos, game)) {

                        result.add(Move.get(position, pos));
                    }
                    break;
                }
            }
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        int minRow = Math.max(position.getRow() - 1, 0);
        int minColumn = Math.max(position.getColumn() - 1, 0);
        int maxRow = Math.min(position.getRow() + 1, 7);
        int maxColumn = Math.min(position.getColumn() + 1, 7);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                Position checkPosition = Position.Companion.get(row, column);
                if (isMoveable(checkPosition, game)) {
                    return true;
                }
            }
        }

        if (position.getColumn() + 2 < 8) {
            Position shortRochade = Position.Companion.get(position.getRow(), position.getColumn() + 2);
            if (isMoveable(shortRochade, game)) {
                return true;
            }
        }

        if (position.getColumn() - 2 >= 0) {
            Position longRochade = Position.Companion.get(position.getRow(), position.getColumn() - 2);
            if (isMoveable(longRochade, game)) {
                return true;
            }
        }

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        int count = 0;
        int minRow = Math.max(position.getRow() - 1, 0);
        int minColumn = Math.max(position.getColumn() - 1, 0);
        int maxRow = Math.min(position.getRow() + 1, 7);
        int maxColumn = Math.min(position.getColumn() + 1, 7);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                Position checkPosition = Position.Companion.get(row, column);
                if (isReachable(checkPosition, game)) {
                    count++;
                }
            }
        }

        if (position.getColumn() + 2 < 8) {
            Position shortRochade = Position.Companion.get(position.getRow(), position.getColumn() + 2);
            if (isReachable(shortRochade, game)) {
                count++;
            }
        }

        if (position.getColumn() - 2 >= 0) {
            Position longRochade = Position.Companion.get(position.getRow(), position.getColumn() - 2);
            if (isReachable(longRochade, game)) {
                count++;
            }
        }

        return count;
    }

    public boolean didRochade() {
        return didRochade;
    }

    public void performRochade() {
        didRochade = true;
    }

    public void undoMove(Position oldPosition) {
        super.undoMove(oldPosition);

        if (stepsTaken == 0) {
            didRochade = false;
        }
    }

    public String toString() {
        String parentString = super.toString();
        StringBuilder buffer = new StringBuilder(parentString.length() + 5);

        buffer.append(parentString);
        if (didRochade) buffer.append("-true");

        return buffer.toString();
    }

    public boolean isKing() {
        return true;
    }

    protected String getType() {
        return "King";
    }

    public ImageType getImageType() {
        if (isWhite) {
            return ImageType.W_KING;
        } else {
            return ImageType.B_KING;
        }
    }
}

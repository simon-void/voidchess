package voidchess.figures;

import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.helper.Move;
import voidchess.helper.Position;
import voidchess.image.ImageType;

import java.util.List;

/**
 * @author stephan
 */
public class Pawn extends Figure {
    private boolean canBeHitByEnpasent;

    public Pawn(boolean isWhite, Position position) {
        super(isWhite, position, FigureType.PAWN);
        canBeHitByEnpasent = false;
    }

    public Pawn(boolean isWhite, Position position, boolean canBeHitByEnpasent) {
        super(isWhite, position, FigureType.PAWN);
        this.canBeHitByEnpasent = canBeHitByEnpasent;
    }

    public boolean canBeHitByEnpasent() {
        return canBeHitByEnpasent;
    }

    public void setCanBeHitByEnpasent() {
        canBeHitByEnpasent = true;
    }

    public void figureMoved(Move move) {
        canBeHitByEnpasent = move.getFrom().equalsPosition(getPosition())
                && Math.abs(move.getFrom().getRow() - move.getTo().getRow()) == 2;
        super.figureMoved(move);
    }

    public void undoMove(Position oldPosition) {
        super.undoMove(oldPosition);
        canBeHitByEnpasent = false;
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return isStraightReachable(to, game) || isDiagonalReachable(to, game);
    }

    private boolean isStraightReachable(Position to, BasicChessGameInterface game) {
        if (to.getColumn() == getPosition().getColumn()) {
            int oneForwardRow = isWhite() ? getPosition().getRow() + 1 : getPosition().getRow() - 1;
            Position oneForwardPosition = Position.Companion.get(oneForwardRow, getPosition().getColumn());
            if (oneForwardPosition.equalsPosition(to)) {
                return game.isFreeArea(to);
            }
            int pawnStartRow = isWhite() ? 1 : 6;
            if (getPosition().getRow() == pawnStartRow) {
                int twoForwardRow = isWhite() ? getPosition().getRow() + 2 : getPosition().getRow() - 2;
                Position twoForwardPosition = Position.Companion.get(twoForwardRow, getPosition().getColumn());
                return to.equalsPosition(twoForwardPosition)
                        && game.isFreeArea(twoForwardPosition)
                        && game.isFreeArea(oneForwardPosition);
            }
            return false;
        }
        return false;
    }

    private boolean isDiagonalReachable(Position to, BasicChessGameInterface game) {
        int oneForwardRow = isWhite() ? getPosition().getRow() + 1 : getPosition().getRow() - 1;

        if (getPosition().getColumn() != 0) {
            int leftColumn = getPosition().getColumn() - 1;
            Position leftForwardPosition = Position.Companion.get(oneForwardRow, leftColumn);
            if (to.equalsPosition(leftForwardPosition)) {
                if (!game.isFreeArea(to) && hasDifferentColor(game.getFigure(to))) {
                    return true;
                }
                Position leftPosition = Position.Companion.get(getPosition().getRow(), leftColumn);
                if (!game.isFreeArea(leftPosition)
                        && game.getFigure(leftPosition).canBeHitByEnpasent()
                        && hasDifferentColor(game.getFigure(leftPosition))
                ) {
                    return true;
                }
            }
        }
        if (getPosition().getColumn() != 7) {
            int rightColumn = getPosition().getColumn() + 1;
            Position rightForwardPosition = Position.Companion.get(oneForwardRow, rightColumn);
            Position rightPosition = Position.Companion.get(getPosition().getRow(), rightColumn);
            if (to.equalsPosition(rightForwardPosition)) {
                if (!game.isFreeArea(to) && hasDifferentColor(game.getFigure(to))) {
                    return true;
                }
                if (!game.isFreeArea(rightPosition)
                        && game.getFigure(rightPosition).canBeHitByEnpasent()
                        && hasDifferentColor(game.getFigure(rightPosition))
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        int minColumn = Math.max(getPosition().getColumn() - 1, 0);
        int maxColumn = Math.min(getPosition().getColumn() + 1, 7);
        int oneForwardRow = isWhite() ? getPosition().getRow() + 1 : getPosition().getRow() - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.Companion.get(oneForwardRow, column);
            if (isReachable(checkPosition, game)) {
                result.add(Move.Companion.get(getPosition(), checkPosition));
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (getPosition().getRow() == pawnStartRow) {
            Position twoForwardPosition = Position.Companion.get(twoForwardRow, getPosition().getColumn());
            if (isReachable(twoForwardPosition, game)) {
                result.add(Move.Companion.get(getPosition(), twoForwardPosition));
            }
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        int minColumn = Math.max(getPosition().getColumn() - 1, 0);
        int maxColumn = Math.min(getPosition().getColumn() + 1, 7);
        int oneForwardRow = isWhite() ? getPosition().getRow() + 1 : getPosition().getRow() - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.Companion.get(oneForwardRow, column);
            if (isMovable(checkPosition, game)) {
                return true;
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (getPosition().getRow() == pawnStartRow) {
            Position twoForwardPosition = Position.Companion.get(twoForwardRow, getPosition().getColumn());
            if (isMovable(twoForwardPosition, game)) {
                return true;
            }
        }

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        int count = 0;
        int minColumn = Math.max(getPosition().getColumn() - 1, 0);
        int maxColumn = Math.min(getPosition().getColumn() + 1, 7);
        int oneForwardRow = isWhite() ? getPosition().getRow() + 1 : getPosition().getRow() - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.Companion.get(oneForwardRow, column);
            if (isReachable(checkPosition, game)) {
                count++;
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (getPosition().getRow() == pawnStartRow) {
            Position twoForwardPosition = Position.Companion.get(twoForwardRow, getPosition().getColumn());
            if (isReachable(twoForwardPosition, game)) {
                count++;
            }
        }

        return count;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(20);
        buffer.append(super.toString());
        buffer.append("-");
        buffer.append(canBeHitByEnpasent);
        return buffer.toString();
    }
}

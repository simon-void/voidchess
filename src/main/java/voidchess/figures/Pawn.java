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
        super(isWhite, position, (byte) 1);
        canBeHitByEnpasent = false;
    }

    public Pawn(boolean isWhite, Position position, boolean canBeHitByEnpasent) {
        super(isWhite, position, (byte) 1);
        this.canBeHitByEnpasent = canBeHitByEnpasent;
    }

    public boolean canBeHitByEnpasent() {
        return canBeHitByEnpasent;
    }

    public void setCanBeHitByEnpasent() {
        canBeHitByEnpasent = true;
    }

    public void figureMoved(Move move) {
        canBeHitByEnpasent = move.from.equalsPosition(position)
                && Math.abs(move.from.row - move.to.row) == 2;
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
        if (to.column == position.column) {
            int oneForwardRow = isWhite() ? position.row + 1 : position.row - 1;
            Position oneForwardPosition = Position.get(oneForwardRow, position.column);
            if (oneForwardPosition.equalsPosition(to)) {
                return game.isFreeArea(to);
            }
            int pawnStartRow = isWhite() ? 1 : 6;
            if (position.row == pawnStartRow) {
                int twoForwardRow = isWhite() ? position.row + 2 : position.row - 2;
                Position twoForwardPosition = Position.get(twoForwardRow, position.column);
                return to.equalsPosition(twoForwardPosition)
                        && game.isFreeArea(twoForwardPosition)
                        && game.isFreeArea(oneForwardPosition);
            }
            return false;
        }
        return false;
    }

    private boolean isDiagonalReachable(Position to, BasicChessGameInterface game) {
        int oneForwardRow = isWhite() ? position.row + 1 : position.row - 1;

        if (position.column != 0) {
            int leftColumn = position.column - 1;
            Position leftForwardPosition = Position.get(oneForwardRow, leftColumn);
            if (to.equalsPosition(leftForwardPosition)) {
                if (!game.isFreeArea(to) && hasDifferentColor(game.getFigure(to))) {
                    return true;
                }
                Position leftPosition = Position.get(position.row, leftColumn);
                if (!game.isFreeArea(leftPosition)
                        && game.getFigure(leftPosition).canBeHitByEnpasent()
                        && hasDifferentColor(game.getFigure(leftPosition))
                        ) {
                    return true;
                }
            }
        }
        if (position.column != 7) {
            int rightColumn = position.column + 1;
            Position rightForwardPosition = Position.get(oneForwardRow, rightColumn);
            Position rightPosition = Position.get(position.row, rightColumn);
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
        int minColumn = Math.max(position.column - 1, 0);
        int maxColumn = Math.min(position.column + 1, 7);
        int oneForwardRow = isWhite() ? position.row + 1 : position.row - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.get(oneForwardRow, column);
            if (isReachable(checkPosition, game)) {
                result.add(Move.get(position, checkPosition));
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (position.row == pawnStartRow) {
            Position twoForwardPosition = Position.get(twoForwardRow, position.column);
            if (isReachable(twoForwardPosition, game)) {
                result.add(Move.get(position, twoForwardPosition));
            }
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        int minColumn = Math.max(position.column - 1, 0);
        int maxColumn = Math.min(position.column + 1, 7);
        int oneForwardRow = isWhite() ? position.row + 1 : position.row - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.get(oneForwardRow, column);
            if (isMoveable(checkPosition, game)) {
                return true;
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (position.row == pawnStartRow) {
            Position twoForwardPosition = Position.get(twoForwardRow, position.column);
            if (isMoveable(twoForwardPosition, game)) {
                return true;
            }
        }

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        int count = 0;
        int minColumn = Math.max(position.column - 1, 0);
        int maxColumn = Math.min(position.column + 1, 7);
        int oneForwardRow = isWhite() ? position.row + 1 : position.row - 1;

        for (int column = minColumn; column <= maxColumn; column++) {
            Position checkPosition = Position.get(oneForwardRow, column);
            if (isReachable(checkPosition, game)) {
                count++;
            }
        }

        int pawnStartRow = isWhite() ? 1 : 6;
        int twoForwardRow = isWhite() ? 3 : 4;

        if (position.row == pawnStartRow) {
            Position twoForwardPosition = Position.get(twoForwardRow, position.column);
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

    public boolean isPawn() {
        return true;
    }

    protected String getType() {
        return "Pawn";
    }

    public ImageType getImageType() {
        if (isWhite) {
            return ImageType.W_PAWN;
        } else {
            return ImageType.B_PAWN;
        }
    }
}

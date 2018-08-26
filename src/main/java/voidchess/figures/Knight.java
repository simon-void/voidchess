package voidchess.figures;

import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.helper.Move;
import voidchess.helper.Position;
import voidchess.image.ImageType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stephan
 */
public class Knight extends Figure {
    private List<Position> posIter = new ArrayList<Position>(8);

    public Knight(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, (byte) 3);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        int horizontal_difference = Math.abs(position.getRow() - to.getRow());
        int vertical_difference = Math.abs(position.getColumn() - to.getColumn());

        if (horizontal_difference + vertical_difference != 3
                || horizontal_difference * vertical_difference == 0) {
            return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private List<Position> getIterator(BasicChessGameInterface game) {
        posIter.clear();

        int minRow = Math.max(position.getRow() - 2, 0);
        int minColumn = Math.max(position.getColumn() - 2, 0);
        int maxRow = Math.min(position.getRow() + 2, 7);
        int maxColumn = Math.min(position.getColumn() + 2, 7);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                if (Math.abs(row - position.getRow()) + Math.abs(column - position.getColumn()) == 3) {
                    posIter.add(Position.Companion.get(row, column));
                }
            }
        }

        return posIter;
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        List<Position> posIter = getIterator(game);
        for (Position checkPosition : posIter) {
            if (isReachable(checkPosition, game)) {
                result.add(Move.get(position, checkPosition));
            }
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        List<Position> positions = getIterator(game);
        for (Position checkPosition : positions) {
            if (isMoveable(checkPosition, game)) {
                return true;
            }
        }

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        return getIterator(game).size();
    }

    public boolean isKnight() {
        return true;
    }

    protected String getType() {
        return "Knight";
    }

    public ImageType getImageType() {
        if (isWhite) {
            return ImageType.W_KNIGHT;
        } else {
            return ImageType.B_KNIGHT;
        }
    }
}

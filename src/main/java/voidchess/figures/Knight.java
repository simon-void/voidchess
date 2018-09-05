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
        super(isWhite, startPosition, FigureType.KNIGHT);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        int horizontal_difference = Math.abs(getPosition().getRow() - to.getRow());
        int vertical_difference = Math.abs(getPosition().getColumn() - to.getColumn());

        if (horizontal_difference + vertical_difference != 3
                || horizontal_difference * vertical_difference == 0) {
            return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private List<Position> getIterator(BasicChessGameInterface game) {
        posIter.clear();

        int minRow = Math.max(getPosition().getRow() - 2, 0);
        int minColumn = Math.max(getPosition().getColumn() - 2, 0);
        int maxRow = Math.min(getPosition().getRow() + 2, 7);
        int maxColumn = Math.min(getPosition().getColumn() + 2, 7);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                if (Math.abs(row - getPosition().getRow()) + Math.abs(column - getPosition().getColumn()) == 3) {
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
                result.add(Move.Companion.get(getPosition(), checkPosition));
            }
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        List<Position> positions = getIterator(game);
        for (Position checkPosition : positions) {
            if (isMovable(checkPosition, game)) {
                return true;
            }
        }

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        return getIterator(game).size();
    }
}

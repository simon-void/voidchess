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
public class Rock extends RochadeFigure {
    private List<Position> positions = new ArrayList<Position>(14);

    public Rock(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, FigureType.ROCK);
    }

    public Rock(boolean isWhite, Position startPosition, int stepsTaken) {
        super(isWhite, startPosition, stepsTaken, FigureType.ROCK);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return isHorizontalReachable(to, game) || isVerticalReachable(to, game);
    }

    private boolean isHorizontalReachable(Position to, BasicChessGameInterface game) {
        if (to.getRow() != getPosition().getRow()) return false;
        if (to.getColumn() == getPosition().getColumn()) return false;

        int minColumn = Math.min(to.getColumn(), getPosition().getColumn());
        int maxColumn = Math.max(to.getColumn(), getPosition().getColumn());

        for (int column = minColumn + 1; column < maxColumn; column++) {
            Position middlePosition = Position.Companion.get(to.getRow(), column);
            if (!game.isFreeArea(middlePosition)) return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private boolean isVerticalReachable(Position to, BasicChessGameInterface game) {
        if (to.getColumn() != getPosition().getColumn()) return false;
        if (to.getRow() == getPosition().getRow()) return false;

        int minRow = Math.min(to.getRow(), getPosition().getRow());
        int maxRow = Math.max(to.getRow(), getPosition().getRow());

        for (int row = minRow + 1; row < maxRow; row++) {
            Position middlePosition = Position.Companion.get(row, to.getColumn());
            if (!game.isFreeArea(middlePosition)) return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private void getNorthIterator(BasicChessGameInterface game, List<Position> result) {
        int row = getPosition().getRow();

        while (true) {
            row++;
            if (row == 8) break;
            Position checkPosition = Position.Companion.get(row, getPosition().getColumn());
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite() != figure.isWhite()) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getSouthIterator(BasicChessGameInterface game, List<Position> result) {
        int row = getPosition().getRow();

        while (true) {
            row--;
            if (row < 0) break;
            Position checkPosition = Position.Companion.get(row, getPosition().getColumn());
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite() != figure.isWhite()) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getEastIterator(BasicChessGameInterface game, List<Position> result) {
        int column = getPosition().getColumn();

        while (true) {
            column++;
            if (column == 8) break;
            Position checkPosition = Position.Companion.get(getPosition().getRow(), column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite() != figure.isWhite()) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getWestIterator(BasicChessGameInterface game, List<Position> result) {
        int column = getPosition().getColumn();

        while (true) {
            column--;
            if (column < 0) break;
            Position checkPosition = Position.Companion.get(getPosition().getRow(), column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite() != figure.isWhite()) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        positions.clear();
        getNorthIterator(game, positions);
        getSouthIterator(game, positions);
        getEastIterator(game, positions);
        getWestIterator(game, positions);

        for (Position pos : positions) {
            result.add(Move.Companion.get(getPosition(), pos));
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        positions.clear();
        getNorthIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getSouthIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getEastIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getWestIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        positions.clear();
        getNorthIterator(game, positions);
        getSouthIterator(game, positions);
        getEastIterator(game, positions);
        getWestIterator(game, positions);
        return positions.size();
    }
}

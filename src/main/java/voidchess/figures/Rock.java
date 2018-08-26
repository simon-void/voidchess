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
    private List<Position> positions = new ArrayList<Position>(8);

    public Rock(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, (byte) 2);
    }

    public Rock(boolean isWhite, Position startPosition, int stepsTaken) {
        super(isWhite, startPosition, stepsTaken, (byte) 2);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return isHorizontalReachable(to, game) || isVerticalReachable(to, game);
    }

    private boolean isHorizontalReachable(Position to, BasicChessGameInterface game) {
        if (to.getRow() != position.getRow()) return false;
        if (to.getColumn() == position.getColumn()) return false;

        int minColumn = Math.min(to.getColumn(), position.getColumn());
        int maxColumn = Math.max(to.getColumn(), position.getColumn());

        for (int column = minColumn + 1; column < maxColumn; column++) {
            Position middlePosition = Position.Companion.get(to.getRow(), column);
            if (!game.isFreeArea(middlePosition)) return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private boolean isVerticalReachable(Position to, BasicChessGameInterface game) {
        if (to.getColumn() != position.getColumn()) return false;
        if (to.getRow() == position.getRow()) return false;

        int minRow = Math.min(to.getRow(), position.getRow());
        int maxRow = Math.max(to.getRow(), position.getRow());

        for (int row = minRow + 1; row < maxRow; row++) {
            Position middlePosition = Position.Companion.get(row, to.getColumn());
            if (!game.isFreeArea(middlePosition)) return false;
        }

        return game.isFreeArea(to) || hasDifferentColor(game.getFigure(to));
    }

    private void getNorthIterator(BasicChessGameInterface game, List<Position> result) {
        int row = position.getRow();

        while (true) {
            row++;
            if (row == 8) break;
            Position checkPosition = Position.Companion.get(row, position.getColumn());
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite != figure.isWhite) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getSouthIterator(BasicChessGameInterface game, List<Position> result) {
        int row = position.getRow();

        while (true) {
            row--;
            if (row < 0) break;
            Position checkPosition = Position.Companion.get(row, position.getColumn());
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite != figure.isWhite) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getEastIterator(BasicChessGameInterface game, List<Position> result) {
        int column = position.getColumn();

        while (true) {
            column++;
            if (column == 8) break;
            Position checkPosition = Position.Companion.get(position.getRow(), column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite != figure.isWhite) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getWestIterator(BasicChessGameInterface game, List<Position> result) {
        int column = position.getColumn();

        while (true) {
            column--;
            if (column < 0) break;
            Position checkPosition = Position.Companion.get(position.getRow(), column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (isWhite != figure.isWhite) {
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
            result.add(Move.get(position, pos));
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

    public boolean isRock() {
        return true;
    }

    protected String getType() {
        return "Rock";
    }

    public ImageType getImageType() {
        if (isWhite) {
            return ImageType.W_ROCK;
        } else {
            return ImageType.B_ROCK;
        }
    }
}

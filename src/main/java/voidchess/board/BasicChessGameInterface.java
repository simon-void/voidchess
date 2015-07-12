package voidchess.board;

import voidchess.figures.Figure;
import voidchess.helper.Position;

import java.util.List;

/**
 * @author stephan
 */
public interface BasicChessGameInterface {
    public boolean isFreeArea(Position pos);

    public Figure getFigure(Position pos);

    public Position getKingPosition(boolean whiteKing);

    public List<Figure> getFigures();
}

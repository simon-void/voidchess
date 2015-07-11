package board;

import figures.Figure;
import helper.Position;

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

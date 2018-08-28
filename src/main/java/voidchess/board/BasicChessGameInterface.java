package voidchess.board;

import voidchess.figures.Figure;
import voidchess.helper.Position;

import java.util.List;

/**
 * @author stephan
 */
public interface BasicChessGameInterface {
    boolean isFreeArea(Position pos);

    Figure getFigure(Position pos);

    Position getKingPosition(boolean whiteKing);

    List<Figure> getFigures();
}

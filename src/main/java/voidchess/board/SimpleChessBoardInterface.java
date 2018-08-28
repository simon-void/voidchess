package voidchess.board;

import voidchess.figures.Figure;
import voidchess.helper.CheckStatus;
import voidchess.helper.Position;

/**
 * @author stephan
 */
public interface SimpleChessBoardInterface extends BasicChessGameInterface {
    void init();

    void init(int chess960);

    void init(String des);

    void setFigure(Position pos, Figure figure);

    boolean isCheck(boolean isWhite);

    CheckStatus getCheckStatus(boolean isWhite);
}

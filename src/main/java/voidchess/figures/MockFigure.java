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
class MockFigure extends Figure {
    MockFigure(boolean isWhite, Position position) {
        super(isWhite, position, FigureType.MOCK);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return false;
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        return true;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        return 0;
    }
}

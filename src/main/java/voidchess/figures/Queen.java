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
public class Queen extends Figure {
    private Bishop bishop;
    private Rock rock;

    public Queen(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, FigureType.QUEEN);
        bishop = new Bishop(isWhite, startPosition);
        rock = new Rock(isWhite, startPosition);
    }

    public void figureMoved(Move move) {
        bishop.figureMoved(move);
        rock.figureMoved(move);
        super.figureMoved(move);
    }

    public void undoMove(Position oldPosition) {
        bishop.undoMove(oldPosition);
        rock.undoMove(oldPosition);
        super.undoMove(oldPosition);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return bishop.isReachable(to, game) || rock.isReachable(to, game);
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        bishop.getReachableMoves(game, result);
        rock.getReachableMoves(game, result);
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        return rock.isSelectable(game) || bishop.isSelectable(game);
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        return rock.countReachableMoves(game) + bishop.countReachableMoves(game);
    }
}

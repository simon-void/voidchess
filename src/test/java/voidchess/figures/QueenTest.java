package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.helper.Move;
import voidchess.helper.Position;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class QueenTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Queen-white-e2";
        ChessGame game = new ChessGame(des);

        Position from = Position.byCode("e2");
        Position to1 = Position.byCode("e5");
        Position to2 = Position.byCode("c4");
        Position to3 = Position.byCode("d4");

        Queen queen = new Queen(true, from);
        assertTrue(queen.isReachable(to1, game));
        assertTrue(queen.isReachable(to2, game));
        assertFalse(queen.isReachable(to3, game));
        assertFalse(queen.isReachable(from, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Queen-white-d1 King-white-e1-0 ";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure queen = game.getFigure(Position.byCode("d1"));
        List<Move> moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 17);

        des = "white 0 Rook-black-a1-1 Queen-white-d1 King-white-e1-0 ";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigure(Position.byCode("d1"));
        moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 3);

        des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigure(Position.byCode("b4"));
        moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 1);
    }

    @Test
    public void testIsSelectable() {
        String des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure queen = game.getFigure(Position.byCode("b4"));
        assertTrue(queen.isSelectable(game), "isSelectable");

        des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b5";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigure(Position.byCode("b5"));
        assertFalse(queen.isSelectable(game), "isSelectable");
    }
}

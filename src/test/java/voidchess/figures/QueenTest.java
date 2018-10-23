package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.move.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.board.move.Move;
import voidchess.board.move.Position;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;


public class QueenTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Queen-white-e2  King-white-e1-0  King-black-e8-0";
        ChessGame game = new ChessGame(des);

        Position from = Position.byCode("e2");
        Position to1 = Position.byCode("e5");
        Position to2 = Position.byCode("c4");
        Position to3 = Position.byCode("d4");
        Position to4 = Position.byCode("e1");
        Position to5 = Position.byCode("e8");

        Queen queen = new Queen(true, from);
        assertTrue(queen.isReachable(to1, game));
        assertTrue(queen.isReachable(to2, game));
        assertFalse(queen.isReachable(to3, game));
        assertFalse(queen.isReachable(to4, game));
        assertTrue(queen.isReachable(to5, game));
        assertFalse(queen.isReachable(from, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Queen-white-d1 King-white-e1-0 King-black-e8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure queen = game.getFigureOrNull(Position.byCode("d1"));
        List<Move> moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 17);

        des = "white 0 Rook-black-a1-1 Queen-white-d1 King-white-e1-0  King-black-e8-0";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigureOrNull(Position.byCode("d1"));
        moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 3);

        des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigureOrNull(Position.byCode("b4"));
        moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 1);
    }

    @Test
    public void testIsSelectable() {
        String des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b4 King-black-e8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure queen = game.getFigureOrNull(Position.byCode("b4"));
        assertTrue(queen.isSelectable(game), "isSelectable");

        des = "white 0 King-white-e1-0 Bishop-black-c3 Queen-white-b5 King-black-f8-1";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        queen = game.getFigureOrNull(Position.byCode("b5"));
        assertFalse(queen.isSelectable(game), "isSelectable");
    }
}

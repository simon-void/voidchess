package voidchess.figures;

import org.testng.annotations.DataProvider;
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


public class BishopTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Pawn-white-c2-false Bishop-white-d3- "
                + "Knight-black-b5";
        ChessGame game = new ChessGame(des);

        Position from = Position.byCode("d3");
        Position to1 = Position.byCode("b5");
        Position to2 = Position.byCode("h7");
        Position to3 = Position.byCode("f1");
        Position to4 = Position.byCode("c2");
        Position to5 = Position.byCode("a6");
        Position to6 = Position.byCode("b1");
        Position to7 = Position.byCode("d4");

        Bishop bishop = new Bishop(true, from);
        assertTrue(bishop.isReachable(to1, game));
        assertTrue(bishop.isReachable(to2, game));
        assertTrue(bishop.isReachable(to3, game));
        assertFalse(bishop.isReachable(to4, game));
        assertFalse(bishop.isReachable(to5, game));
        assertFalse(bishop.isReachable(to6, game));
        assertFalse(bishop.isReachable(to7, game));
        assertFalse(bishop.isReachable(from, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 King-white-e1-0 Bishop-white-b2";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure bishop = game.getFigure(Position.byCode("b2"));
        List<Move> moveIter = new LinkedList<>();
        bishop.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 9);

        des = "white 0 King-white-b3-3 Bishop-white-d5 "
                + "Knight-black-f7";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        bishop = game.getFigure(Position.byCode("d5"));
        moveIter = new LinkedList<>();
        bishop.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 10);

        des = "white 0 King-white-f1-1 Bishop-white-c4 "
                + "Bishop-black-b5 Knight-black-d5";
        game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        bishop = game.getFigure(Position.byCode("c4"));
        moveIter = new LinkedList<>();
        bishop.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 3);
    }

    @Test
    public void testGetReachableMoves() {
        String des = "white 0 Knight-white-e1 Rook-black-b2-2 Bishop-white-d2 King-white-e2-1 "
                + "Pawn-white-a5-false Knight-black-g5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure bishop = game.getFigure(Position.byCode("d2"));
        List<Move> moveIter = new LinkedList<>();
        bishop.getReachableMoves(game, moveIter);
        assertEquals(moveIter.size(), 6);
    }

    @Test
    public void testCountReachableMoves() {
        String des = "white 0 Knight-white-e1 Rook-black-b2-2 Bishop-white-d2 King-white-e2-1 "
                + "Pawn-white-a5-false Knight-black-g5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure bishop = game.getFigure(Position.byCode("d2"));
        assertEquals(bishop.countReachableMoves(game), 6);
    }

    @Test(dataProvider = "getTestIsSelectableData")
    public void testIsSelectable(String des, String figurePos, boolean expectedIsSelectable) {
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure bishop = game.getFigure(Position.byCode(figurePos));
        assertEquals(bishop.isSelectable(game), expectedIsSelectable);
    }

    @DataProvider
    public Object[][] getTestIsSelectableData() {
        return new Object[][]{
                new Object[]{"white 0 Bishop-white-c1 King-white-e1-0 Pawn-white-b2-false Pawn-white-d2-false", "c1", false},
                new Object[]{"white 0 Bishop-white-c1 King-white-e1-0", "c1", true},
                new Object[]{"white 0 Queen-black-a1 Bishop-white-c1 King-white-e1-0", "c1", false},
                new Object[]{"white 0 Queen-black-h1 Bishop-white-c1 King-white-e1-0", "c1", false},
                new Object[]{"white 0 Queen-black-a5 Bishop-white-d2 King-white-e1-0", "d2", true},
        };
    }
}

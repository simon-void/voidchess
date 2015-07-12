package voidchess.figures;

import voidchess.board.ChessGame;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.helper.Move;
import voidchess.helper.Position;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class KingTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Rock-white-a1-0 King-white-e1-0 "
                + "Rock-white-h1-0";
        ChessGame game = new ChessGame(des);

        Position from = Position.get("e1");
        Position to1 = Position.get("f1");
        Position to2 = Position.get("g1");
        Position to3 = Position.get("d1");
        Position to4 = Position.get("c1");
        Position to5 = Position.get("d2");
        Position to6 = Position.get("e3");
        Position to7 = Position.get("a1");
        Position to8 = Position.get("h1");

        King king = new King(true, from);
        assertTrue(king.isReachable(to1, game));
        assertFalse(king.isReachable(to2, game));
        assertTrue(king.isReachable(to3, game));
        assertFalse(king.isReachable(to4, game));
        assertTrue(king.isReachable(to5, game));
        assertFalse(king.isReachable(to6, game));
        assertFalse(king.isReachable(from, game));
        assertTrue(king.isReachable(to7, game));
        assertTrue(king.isReachable(to8, game));


        des = "black 0 Rock-black-a8-0 Knight-black-b8 "
                + "King-black-c8-0 Rock-black-h8-0";
        game = new ChessGame(des);

        from = Position.get("c8");
        to1 = Position.get("a8");
        to2 = Position.get("d8");
        to3 = Position.get("d7");
        to4 = Position.get("b8");
        to5 = Position.get("g8");
        to6 = Position.get("e6");
        to7 = Position.get("h8");

        king = new King(false, from);
        assertFalse(king.isReachable(to1, game));
        assertTrue(king.isReachable(to2, game));
        assertTrue(king.isReachable(to3, game));
        assertFalse(king.isReachable(to4, game));
        assertFalse(king.isReachable(to5, game));
        assertFalse(king.isReachable(to6, game));
        assertTrue(king.isReachable(to7, game));
        assertFalse(king.isReachable(from, game));


        game = new ChessGame(621);
        from = Position.get("e1");
        to1 = Position.get("f1");
        king = new King(true, from);
        assertFalse(king.isReachable(to1, game));
    }

    @Test
    public void testGetReachableMoves() {
        ChessGame game = new ChessGame(621);
        Position from = Position.get("e1");
        Figure king = game.getFigure(from);

        List<Move> moveIter = new LinkedList<>();
        king.getReachableMoves(game, moveIter);
        assertEquals(0, moveIter.size());
    }

    @Test
    public void testIsPassiveBound() {
        String des = "black 0 Knight-white-b6 Rock-black-a8-0 "
                + "King-black-e8-0 Rock-black-h8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.get("e8");
        Position to1 = Position.get("c8");
        Position to2 = Position.get("d7");
        Position to3 = Position.get("g8");
        Position to4 = Position.get("e7");

        King king = new King(false, from);

        assertTrue(king.isPassiveBound(to1, game));
        assertTrue(king.isPassiveBound(to2, game));
        assertFalse(king.isPassiveBound(to3, game));
        assertFalse(king.isPassiveBound(to4, game));

        des = "black 0 Bishop-white-c6 Rock-black-a8-0 "
                + "King-black-e8-0 Rock-black-h8-0";
        game.init(des);
        from = Position.get("e8");
        to1 = Position.get("a8");
        to2 = Position.get("d7");
        to3 = Position.get("h8");
        to4 = Position.get("e7");

        king = new King(false, from);

        assertTrue(king.isPassiveBound(to1, game));
        assertTrue(king.isPassiveBound(to2, game));
        assertTrue(king.isPassiveBound(to3, game));
        assertFalse(king.isPassiveBound(to4, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Rock-white-a1-0 King-white-f1-0 "
                + "Rock-white-h1-0";
        SimpleArrayBoard board = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure king = board.getFigure(Position.get("f1"));
        List<Move> moveIter = new LinkedList<>();
        king.getPossibleMoves(board, moveIter);
        assertEquals(7, moveIter.size());

        board.init(621);
        king = board.getFigure(board.getKingPosition(true));
        moveIter = new LinkedList<>();
        king.getPossibleMoves(board, moveIter);
        assertEquals(0, moveIter.size());

        ChessGame game = new ChessGame(621);
        game.move(Move.get("c2-c3"));
        game.move(Move.get("f7-f6"));
        game.move(Move.get("d1-c2"));
        game.move(Move.get("g8-c4"));
        game.move(Move.get("c2-h7"));
        Position pos = Position.get("e8");
        List<Move> movesFrom = FigureTest.getPossibleMovesFrom(game, pos);
        assertEquals(1, movesFrom.size());
    }

    @Test
    public void testDidRochade() {
        String des = "white 0 Rock-white-a1-0 King-white-e1-0 "
                + "King-black-e8-0";
        ChessGame game = new ChessGame(des);

        King king = (King) game.getFigure(Position.get("e1"));
        assertFalse(king.didRochade());
        game.move(Move.get("e1-a1"));
        assertTrue(king.didRochade());
    }
}

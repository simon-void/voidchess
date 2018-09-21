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
public class KingTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Rook-white-a1-0 King-white-e1-0 "
                + "Rook-white-h1-0";
        ChessGame game = new ChessGame(des);

        Position from = Position.Companion.byCode("e1");
        Position to1 = Position.Companion.byCode("f1");
        Position to2 = Position.Companion.byCode("g1");
        Position to3 = Position.Companion.byCode("d1");
        Position to4 = Position.Companion.byCode("c1");
        Position to5 = Position.Companion.byCode("d2");
        Position to6 = Position.Companion.byCode("e3");
        Position to7 = Position.Companion.byCode("a1");
        Position to8 = Position.Companion.byCode("h1");

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


        des = "black 0 Rook-black-a8-0 Knight-black-b8 "
                + "King-black-c8-0 Rook-black-h8-0";
        game = new ChessGame(des);

        from = Position.Companion.byCode("c8");
        to1 = Position.Companion.byCode("a8");
        to2 = Position.Companion.byCode("d8");
        to3 = Position.Companion.byCode("d7");
        to4 = Position.Companion.byCode("b8");
        to5 = Position.Companion.byCode("g8");
        to6 = Position.Companion.byCode("e6");
        to7 = Position.Companion.byCode("h8");

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
        from = Position.Companion.byCode("e1");
        to1 = Position.Companion.byCode("f1");
        king = new King(true, from);
        assertFalse(king.isReachable(to1, game));
    }

    @Test
    public void testGetReachableMoves() {
        ChessGame game = new ChessGame(621);
        Position from = Position.Companion.byCode("e1");
        Figure king = game.getFigure(from);

        List<Move> moveIter = new LinkedList<>();
        king.getReachableMoves(game, moveIter);
        assertEquals(0, moveIter.size());
    }

    @Test
    public void testIsPassiveBound() {
        String des = "black 0 Knight-white-b6 Rook-black-a8-0 "
                + "King-black-e8-0 Rook-black-h8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.Companion.byCode("e8");
        Position to1 = Position.Companion.byCode("c8");
        Position to2 = Position.Companion.byCode("d7");
        Position to3 = Position.Companion.byCode("g8");
        Position to4 = Position.Companion.byCode("e7");

        King king = new King(false, from);

        assertTrue(king.isPassiveBound(to1, game));
        assertTrue(king.isPassiveBound(to2, game));
        assertFalse(king.isPassiveBound(to3, game));
        assertFalse(king.isPassiveBound(to4, game));

        des = "black 0 Bishop-white-c6 Rook-black-a8-0 "
                + "King-black-e8-0 Rook-black-h8-0";
        game.init(des);
        from = Position.Companion.byCode("e8");
        to1 = Position.Companion.byCode("a8");
        to2 = Position.Companion.byCode("d7");
        to3 = Position.Companion.byCode("h8");
        to4 = Position.Companion.byCode("e7");

        king = new King(false, from);

        assertTrue(king.isPassiveBound(to1, game));
        assertTrue(king.isPassiveBound(to2, game));
        assertTrue(king.isPassiveBound(to3, game));
        assertFalse(king.isPassiveBound(to4, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Rook-white-a1-0 King-white-f1-0 "
                + "Rook-white-h1-0";
        SimpleArrayBoard board = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure king = board.getFigure(Position.Companion.byCode("f1"));
        List<Move> moveIter = new LinkedList<>();
        king.getPossibleMoves(board, moveIter);
        assertEquals(moveIter.size(), 5);

        board.init(621);
        king = board.getFigure(board.getKingPosition(true));
        moveIter = new LinkedList<>();
        king.getPossibleMoves(board, moveIter);
        assertEquals(moveIter.size(), 0);

        ChessGame game = new ChessGame(621);
        game.move(Move.Companion.byCode("c2-c3"));
        game.move(Move.Companion.byCode("f7-f6"));
        game.move(Move.Companion.byCode("d1-c2"));
        game.move(Move.Companion.byCode("g8-c4"));
        game.move(Move.Companion.byCode("c2-h7"));
        Position pos = Position.Companion.byCode("e8");
        List<Move> movesFrom = FigureTest.getPossibleMovesFrom(game, pos);
        assertEquals(movesFrom.size(), 1);
    }

    @Test
    public void testDidCastling() {
        String des = "white 0 Rook-white-a1-0 King-white-e1-0 "
                + "King-black-e8-0";
        ChessGame game = new ChessGame(des);

        King king = (King) game.getFigure(Position.Companion.byCode("e1"));
        assertFalse(king.didCastling());
        game.move(Move.Companion.byCode("e1-a1"));
        assertTrue(king.didCastling());
    }

    @Test
    public void testImmidiatlyCastlingInChess960Positions() {
        //siehe https://de.wikipedia.org/wiki/Chess960#Castlingregeln

        //Rook on a1, king on b1 so b1-a1 should be possible as a first move
        ChessGame game = new ChessGame(314);
        Position c1 = Position.Companion.byCode("c1");
        Position d1 = Position.Companion.byCode("d1");
        boolean isCastlingPossible = game.isMoveable(d1, c1, true);
        assertTrue(isCastlingPossible, "castling should be possible");

        //Rook on a1, king on b1 so b1-a1 should be be possible as a first move
        game = new ChessGame(759);
        Position a1 = Position.Companion.byCode("a1");
        Position b1 = Position.Companion.byCode("b1");
        isCastlingPossible = game.isMoveable(b1, a1, true);
        assertFalse(isCastlingPossible, "castling should be impossible possible");
    }
}

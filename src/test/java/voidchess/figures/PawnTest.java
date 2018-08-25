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
public class PawnTest {
    @Test
    public void testCanBeHitByEnpasent() {
        Move move = Move.get(Position.get("e2"), Position.get("e4"));
        Move other_move = Move.get(Position.get("d2"), Position.get("d4"));


        Pawn pawn = new Pawn(true, move.from);
        assertFalse(pawn.canBeHitByEnpasent());
        pawn.figureMoved(move);
        assertTrue(pawn.canBeHitByEnpasent());
        pawn.figureMoved(other_move);
        assertFalse(pawn.canBeHitByEnpasent());
    }

    @Test
    public void testIsReachable() {
        String des = "white 0 Pawn-white-a2-false Pawn-white-b3-false";
        ChessGame game = new ChessGame(des);

        Position from = Position.get("a2");
        Position to1 = Position.get("a3");
        Position to2 = Position.get("a4");
        Position to3 = Position.get("b3");
        Position to4 = Position.get("a1");

        Figure pawn = game.getFigure(from);
        assertTrue(pawn.isReachable(to1, game));
        assertTrue(pawn.isReachable(to2, game));
        assertFalse(pawn.isReachable(to3, game));
        assertFalse(pawn.isReachable(to4, game));
        assertFalse(pawn.isReachable(from, game));


        des = "black 0 Pawn-white-e4-true Pawn-white-c3-false Pawn-black-d4-false";
        game = new ChessGame(des);

        from = Position.get("d4");
        to1 = Position.get("d3");
        to2 = Position.get("e3");
        to3 = Position.get("c3");
        to4 = Position.get("d2");

        pawn = game.getFigure(from);
        assertTrue(pawn.isReachable(to1, game));
        assertTrue(pawn.isReachable(to2, game));
        assertTrue(pawn.isReachable(to3, game));
        assertFalse(pawn.isReachable(to4, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "black 0 Pawn-white-a4-true Pawn-black-b4-false "
                + "King-black-e8-0 Pawn-black-h7-false";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure pawn1 = game.getFigure(Position.get("b4"));
        List<Move> moveIter1 = new LinkedList<>();
        pawn1.getPossibleMoves(game, moveIter1);
        assertEquals(moveIter1.size(), 2);

        Pawn pawn2 = new Pawn(false, Position.get("h7"));
        List<Move> moveIter2 = new LinkedList<>();
        pawn2.getPossibleMoves(game, moveIter2);
        assertEquals(moveIter2.size(), 2);
    }
}

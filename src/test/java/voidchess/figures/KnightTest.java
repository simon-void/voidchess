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
public class KnightTest {
    @Test
    public void testIsReachable() {
        String des = "black 0 Pawn-white-c2-false Knight-black-d4 "
                + "Knight-black-f5";
        ChessGame game = new ChessGame(des);

        Position from = Position.byCode("d4");
        Position to1 = Position.byCode("b3");
        Position to2 = Position.byCode("b5");
        Position to3 = Position.byCode("c6");
        Position to4 = Position.byCode("e6");
        Position to5 = Position.byCode("c2");
        Position to6 = Position.byCode("f3");
        Position to7 = Position.byCode("d1");
        Position to8 = Position.byCode("f5");

        Knight knight = new Knight(false, from);
        assertTrue(knight.isReachable(to1, game));
        assertTrue(knight.isReachable(to2, game));
        assertTrue(knight.isReachable(to3, game));
        assertTrue(knight.isReachable(to4, game));
        assertTrue(knight.isReachable(to5, game));
        assertTrue(knight.isReachable(to6, game));
        assertFalse(knight.isReachable(to7, game));
        assertFalse(knight.isReachable(to8, game));
        assertFalse(knight.isReachable(from, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Knight-white-a1 King-white-e1-0 "
                + "Knight-white-g6";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure knight1 = game.getFigure(Position.byCode("a1"));
        List<Move> moveIter1 = new LinkedList<>();
        knight1.getPossibleMoves(game, moveIter1);
        assertEquals(moveIter1.size(), 2);
        Figure knight2 = game.getFigure(Position.byCode("g6"));
        List<Move> moveIter2 = new LinkedList<>();
        knight2.getPossibleMoves(game, moveIter2);
        assertEquals(moveIter2.size(), 6);
    }
}

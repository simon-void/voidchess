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
public class QueenTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Queen-white-e2";
        ChessGame game = new ChessGame(des);

        Position from = Position.get("e2");
        Position to1 = Position.get("e5");
        Position to2 = Position.get("c4");
        Position to3 = Position.get("d4");

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

        Figure queen = game.getFigure(Position.get("d1"));
        List<Move> moveIter = new LinkedList<>();
        queen.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 17);
    }
}

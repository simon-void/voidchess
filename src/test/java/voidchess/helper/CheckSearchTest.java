package voidchess.helper;

import org.testng.annotations.Test;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.figures.Pawn;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class CheckSearchTest {
    @Test
    public void testSignum() {
        assertEquals(1, CheckSearch.signum(4));
        assertEquals(0, CheckSearch.signum(0));
        assertEquals(-1, CheckSearch.signum(-34));
    }

    @Test
    public void testAreDiagonalPositions() {
        Position pos1 = Position.Companion.byCode("a1");
        Position pos2 = Position.Companion.byCode("a8");
        Position pos3 = Position.Companion.byCode("h1");

        assertTrue(CheckSearch.areDiagonalPositions(pos2, pos3));
        assertTrue(CheckSearch.areDiagonalPositions(pos3, pos2));
        assertFalse(CheckSearch.areDiagonalPositions(pos1, pos2));
        assertFalse(CheckSearch.areDiagonalPositions(pos1, pos3));
    }

    @Test
    public void testAreStraightPositions() {
        Position pos1 = Position.Companion.byCode("a1");
        Position pos2 = Position.Companion.byCode("a8");
        Position pos3 = Position.Companion.byCode("h1");

        assertFalse(CheckSearch.areStraightPositions(pos2, pos3));
        assertFalse(CheckSearch.areStraightPositions(pos3, pos2));
        assertTrue(CheckSearch.areStraightPositions(pos1, pos2));
        assertTrue(CheckSearch.areStraightPositions(pos1, pos3));
    }

    @Test
    public void testAnalyseCheck() {
        String des = "white 0 King-white-e4-4 King-black-e5-4";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));
        CheckStatus status = CheckSearch.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(1, possiblePositions.size());
        assertTrue(possiblePositions.get(0).equalsPosition(Position.Companion.byCode("e5")));

        des = "white 0 King-white-e4-4 King-black-e6-4";
        game.init(des);
        status = CheckSearch.analyseCheck(game, true);

        assertFalse(status.isCheck());
        assertFalse(status.onlyKingCanMove());

        des = "black 0 King-white-e1-0 Queen-white-e2 Knight-white-d6 King-black-e8-0";
        game.init(des);
        status = CheckSearch.analyseCheck(game, false);

        assertTrue(status.isCheck());
        assertTrue(status.onlyKingCanMove());

        des = "black 0 King-white-e1-0 Queen-white-e6 Knight-white-c6 King-black-e8-0";
        game.init(des);
        status = CheckSearch.analyseCheck(game, false);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        possiblePositions = status.getCheckInterceptPositions();
        assertEquals(2, possiblePositions.size());
        assertTrue(possiblePositions.contains(Position.Companion.byCode("e6")));
        assertTrue(possiblePositions.contains(Position.Companion.byCode("e7")));

        des = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-h4";
        game.init(des);
        status = CheckSearch.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        possiblePositions = status.getCheckInterceptPositions();
        assertEquals(3, possiblePositions.size());
        assertTrue(possiblePositions.contains(Position.Companion.byCode("h4")));
        assertTrue(possiblePositions.contains(Position.Companion.byCode("g3")));
        assertTrue(possiblePositions.contains(Position.Companion.byCode("f2")));
    }

    @Test
    void testAnalyseDiagonalCheckAfterEnpassent() {
        // black moved f7-f5 to counter the check from the diagonal, white played e5-f6
        String des = "black 0 King-white-e1-0 Queen-white-g4 Pawn-white-f6-false King-black-d7-1";

        ExtendedMove extendedMove = getEnpassentMove(Move.Companion.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 3);
    }

    @Test
    void testAnalyseStraightCheckAfterEnpassent() {
        // black moved f7-f5, white played e5-f6
        String des = "black 0 King-white-e1-0 Queen-white-e4 Pawn-white-f6-false King-black-e8-0";

        ExtendedMove extendedMove = getEnpassentMove(Move.Companion.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 4);
    }

    @Test
    void testAnalysePawnCheckAfterEnpassent() {
        // black moved f7-f5, white played e5-f6
        String des = "black 0 King-white-e1-0 Pawn-white-f6-false King-black-e7-1";

        ExtendedMove extendedMove = getEnpassentMove(Move.Companion.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 1);
    }

    private ExtendedMove getEnpassentMove(Move move) {
        boolean isWhiteMove = move.getTo().getRow() > move.getFrom().getRow();
        Pawn hitByEnpassant = new Pawn(!isWhiteMove, Position.Companion.get(move.getFrom().getRow(), move.getTo().getColumn()));

        ExtendedMove extendedMove = new ExtendedMove(move, null, hitByEnpassant, isWhiteMove, false, true, false);

        return extendedMove;
    }
}

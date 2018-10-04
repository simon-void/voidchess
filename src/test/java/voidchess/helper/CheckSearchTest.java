package voidchess.helper;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.figures.Pawn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class CheckSearchTest {

    @Test
    public void testAnalyseCheck() {
        String des = "white 0 King-white-e4-4 King-black-e5-4";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 1);
        assertTrue(possiblePositions.get(0).equalsPosition(Position.byCode("e5")));

        des = "white 0 King-white-e4-4 King-black-e6-4";
        game.init(des);
        status = CheckSearch.INSTANCE.analyseCheck(game, true);

        assertFalse(status.isCheck());
        assertFalse(status.onlyKingCanMove());

        des = "black 0 King-white-e1-0 Queen-white-e2 Knight-white-d6 King-black-e8-0";
        game.init(des);
        status = CheckSearch.INSTANCE.analyseCheck(game, false);

        assertTrue(status.isCheck());
        assertTrue(status.onlyKingCanMove());

        des = "black 0 King-white-e1-0 Queen-white-e6 Knight-white-c6 King-black-e8-0";
        game.init(des);
        status = CheckSearch.INSTANCE.analyseCheck(game, false);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 2);
        assertTrue(possiblePositions.contains(Position.byCode("e6")));
        assertTrue(possiblePositions.contains(Position.byCode("e7")));

        des = "white 0 King-white-e1-0 Rook-white-h2-1 Queen-black-h4";
        game.init(des);
        status = CheckSearch.INSTANCE.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 3);
        assertTrue(possiblePositions.contains(Position.byCode("h4")));
        assertTrue(possiblePositions.contains(Position.byCode("g3")));
        assertTrue(possiblePositions.contains(Position.byCode("f2")));
    }

    @Test(dataProvider = "getTestAnalyseCheckWithLastCheckData")
    public void testAnalyseCheckWithLastCheck(ChessGame game, List<String> moveCodes, int expectedNumberOfCurrentChecks) {
        List<Move> moves = moveCodes.stream().map(Move.Companion::byCode).collect(Collectors.toList());
        for(Move move: moves) {
            Position from = move.getFrom();
            Position to = move.getTo();
            boolean isWhiteTurn = game.isWhiteTurn();
            boolean isMovable = game.isMovable(from, to, isWhiteTurn);
            assertTrue(isMovable, move + " should be valid");
            game.move(move);
        }
        boolean isWhiteTurn = game.isWhiteTurn();
        ExtendedMove lastMove = game.getLastMove();
        CheckStatus checkStatus = CheckSearch.INSTANCE.analyseCheck(game, isWhiteTurn, lastMove);
        switch (expectedNumberOfCurrentChecks) {
            case 0: // no check
                assertFalse(checkStatus.isCheck(), "has at least one attacker");
                assertFalse(checkStatus.onlyKingCanMove(), "is double check");
                break;
            case 1: // single check
                assertTrue(checkStatus.isCheck(), "has at least one attacker");
                assertFalse(checkStatus.onlyKingCanMove(), "is double check");
                break;
            case 2: // double check
                assertTrue(checkStatus.isCheck(), "has at least one attacker");
                assertTrue(checkStatus.onlyKingCanMove(), "is double check");
                break;
            default: fail("illegal number of expected current checks. Expected 0-2, found: "+ expectedNumberOfCurrentChecks);
        }
    }

    @DataProvider
    public Object[][] getTestAnalyseCheckWithLastCheckData() {
        return new Object[][] {
                new Object[] {new ChessGame(518), Arrays.asList("e2-e4"), 0},
                new Object[] {new ChessGame(518), Arrays.asList("e2-e4", "d7-d5", "f1-b5", "c7-c6", "b5-c6"), 1},
                new Object[] {new ChessGame(518), Arrays.asList("e2-e4", "d7-d5", "f1-b5", "c7-c6", "c2-c3", "g8-h6", "d1-a4", "h6-g8", "b5-c6"), 1},
                new Object[] {new ChessGame(518), Arrays.asList("b1-a3", "d7-d5", "a3-b5", "g8-h6", "c2-c3", "h6-g8", "d1-a4", "g8-h6", "b5-c7"), 2},
        };
    }

    @Test
    void testAnalyseDiagonalCheckAfterEnpassent() {
        // black moved f7-f5 to counter the check from the diagonal, white played e5-f6
        String des = "black 0 King-white-e1-0 Queen-white-g4 Pawn-white-f6-false King-black-d7-1";

        ExtendedMove extendedMove = getEnpassentMove(Move.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 3);
    }

    @Test
    void testAnalyseStraightCheckAfterEnpassent() {
        // black moved f7-f5, white played e5-f6
        String des = "black 0 King-white-e1-0 Queen-white-e4 Pawn-white-f6-false King-black-e8-0";

        ExtendedMove extendedMove = getEnpassentMove(Move.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 4);
    }

    @Test
    void testAnalysePawnCheckAfterEnpassent() {
        // black moved f7-f5, white played e5-f6
        String des = "black 0 King-white-e1-0 Pawn-white-f6-false King-black-e7-1";

        ExtendedMove extendedMove = getEnpassentMove(Move.byCode("e5-f6"));
        LastMoveProvider moveProvider = mock(LastMoveProvider.class);
        when(moveProvider.getLastMove()).thenReturn(extendedMove);
        SimpleArrayBoard game = new SimpleArrayBoard(des, moveProvider);
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, false, extendedMove);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        List<Position> possiblePositions = status.getCheckInterceptPositions();
        assertEquals(possiblePositions.size(), 1);
    }

    @Test
    void testAnalyseDoubleStraightCheckAfterPawnPromotion() {
        // white king stands in the shadow of the pawn
        // so when the pawn promotes to queen or rook after taking a figure,
        // the king will be in check on two straight lines
        String des = "black 0 King-white-e1-5 Bishop-white-f1 Pawn-black-e2-false Queen-black-e5 King-black-e8-0";

        ChessGame game = new ChessGame(des);
        game.move(Move.get(Position.byCode("e2"), Position.byCode("f1")));
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertTrue(status.onlyKingCanMove(), "is double check");
    }

    @Test
    void testAnalyseSingleStraightCheckAfterPawnpromotion() {
        String des = "black 0 King-white-e1-5 Bishop-white-f1 Bishop-white-g1 Pawn-black-e2-false King-black-e8-0";

        ChessGame game = new ChessGame(des);
        game.move(Move.get(Position.byCode("e2"), Position.byCode("f1")));
        CheckStatus status = CheckSearch.INSTANCE.analyseCheck(game, true);

        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove(), "only king can move");
    }

    private ExtendedMove getEnpassentMove(Move move) {
        boolean isWhiteMove = move.getTo().getRow() > move.getFrom().getRow();
        Pawn hitByEnpassant = new Pawn(!isWhiteMove, Position.get(move.getFrom().getRow(), move.getTo().getColumn()));

        ExtendedMove extendedMove = new ExtendedMove(move, null, hitByEnpassant, isWhiteMove, false, true, false);

        return extendedMove;
    }
}

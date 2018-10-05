package voidchess.player.ki.concurrent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.player.ki.*;
import voidchess.player.ki.evaluation.EvaluatedMove;
import voidchess.ui.ComputerPlayerUI;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class SingleThreadStrategyTest {
    private SingleThreadStrategy strategy;

    @BeforeMethod
    public void setup() {
        ComputerPlayerUI uiMock = mock(ComputerPlayerUI.class);
        strategy = new SingleThreadStrategy(uiMock);
    }

    @Test
    public void testSortOrder() {
        ChessGame game = new ChessGame(518);
        Iterator<EvaluatedMove> moves = evaluate(1, game).descendingIterator();

        EvaluatedMove betterMove = moves.next();
        while (moves.hasNext()) {
            EvaluatedMove worseMove = moves.next();
            assertTrue(
                    betterMove.getValue().compareTo(worseMove.getValue()) >= 0,
                    "earlier move should be at least as good"
            );
            betterMove = worseMove;
        }
    }

    @Test(dataProvider = "bestMoveProvider")
    public void testFindBestMoveIn(int depth, ChessGame game, Move bestMove) {
        Move computedMove = getBestMoveInIn(depth, game);
        assertEquals(computedMove, bestMove, "best move");
    }

    @Test(dataProvider = "worstMoveProvider")
    public void testFindWorstMoveIn(int depth, ChessGame game, Move worstMove) {
        Move computedMove = getWorstMoveIn(depth, game);
        assertEquals(computedMove, worstMove, "worst move");
    }

    @DataProvider(name = "bestMoveProvider")
    public Object[][] getBestMoveBoardsProvider() {
        return new Object[][]{
                //best move with matt
                new Object[]{
                        1,
                        new ChessGame("black 0 King-white-h1-3 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0"),
                        Move.byCode("f4-f1")
                },
                //color inverted matt
                new Object[]{
                        1,
                        new ChessGame("white 0 King-black-h8-3 Pawn-black-g7-false Pawn-black-h7-false Rook-white-f4-2 King-white-e1-0"),
                        Move.byCode("f4-f8")
                },
                //best move with value
                new Object[]{
                        1,
                        new ChessGame("white 0 King-white-e1-0 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f2-2 King-black-e8-0"),
                        Move.byCode("e1-f2")
                },
                //best matt move in 2
                new Object[]{
                        2,
                        new ChessGame("white 0 Bishop-white-a1 Rook-black-b2-2 Knight-black-c2 Pawn-white-d6-false King-white-g6-8 King-black-h8-4"),
                        Move.byCode("d6-d7")
                },
                new Object[] {
                        1,
                        new ChessGame(518, "d2-d3", "d7-d6", "c1-g5", "e7-e6"),
                        Move.byCode("g5-d8")
                },
        };
    }

    @DataProvider(name = "worstMoveProvider")
    public Object[][] getWorstMoveBoardsProvider() {
        return new Object[][]{
                //worst move goes into matt
                new Object[]{
                        1,
                        new ChessGame("white 0 King-white-g1-2 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0"),
                        Move.byCode("g1-h1")
                },
                //color inverted worst move goes into matt
                new Object[]{
                        1,
                        new ChessGame("black 0 King-black-g8-2 Pawn-black-g7-false Pawn-black-h7-false Rook-white-f4-2 King-white-e1-0"),
                        Move.byCode("g8-h8")
                },
                //worst move by value
                new Object[]{
                        1,
                        new ChessGame("white 0 King-white-e1-0 Rook-black-f2-2 Queen-black-d1 King-black-e8-0"),
                        Move.byCode("e1-f2")
                },
                new Object[] {
                        1,
                        new ChessGame(518, "d2-d3", "d7-d6", "c1-g5"),
                        Move.byCode("e7-e6")
                },
        };
    }

    private Move getBestMoveInIn(int depth, ChessGameInterface game) {
        SortedSet<EvaluatedMove> moves = evaluate(depth, game);
        return moves.last().getMove();
    }

    private Move getWorstMoveIn(int depth, ChessGameInterface game) {
        SortedSet<EvaluatedMove> moves = evaluate(depth, game);
        return moves.first().getMove();
    }

    private NavigableSet<EvaluatedMove> evaluate(int depth, ChessGameInterface game) {
        SearchTreePruner pruner = new SimplePruner(depth, depth+1, depth+1);
        StaticEvaluationInterface staticEval = StaticEvaluation.INSTANCE;
        DynamicEvaluation dynEval = new DynamicEvaluation(pruner, staticEval);
        return strategy.evaluatePossibleMoves(game, dynEval);
    }
}

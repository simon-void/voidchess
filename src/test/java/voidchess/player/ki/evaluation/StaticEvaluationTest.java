package voidchess.player.ki.evaluation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.player.ki.evaluation.Ongoing;
import voidchess.player.ki.evaluation.StaticEvaluation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;


public class StaticEvaluationTest {
    private static final double OK_DELTA = 0.00000000001;

    @Test(dataProvider = "symmetricPositionProvider")
    public void testSymmetricPositionLeadsToEqualEvaluation(ChessGame game) {
        StaticEvaluation staticEval = StaticEvaluation.INSTANCE;
        Ongoing evalForWhite = staticEval.getPrimaryEvaluation(game, true);
        Ongoing evalForBlack = staticEval.getPrimaryEvaluation(game, false);
        assertEquals(evalForWhite.getPrimaryEvaluation(), evalForBlack.getPrimaryEvaluation());
        assertEquals(evalForWhite.getPrimaryEvaluation(), 0.0);

        staticEval.addSecondaryEvaluation(game, true, evalForWhite);
        staticEval.addSecondaryEvaluation(game, false, evalForBlack);
        assertEquals(evalForWhite.getCombinedEvaluation(), evalForBlack.getCombinedEvaluation(), OK_DELTA);
        assertEquals(evalForWhite.getCombinedEvaluation(), 0.0, OK_DELTA);
    }

    @DataProvider
    public Object[][] symmetricPositionProvider() {
        return new Object[][] {
                new Object[]{new ChessGame("black 0 King-white-e1-8 King-black-e8-8")},
                new Object[]{new ChessGame("black 0 Rook-white-a1-0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Knight-white-a1 King-white-e1-0 Knight-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Bishop-white-a1 King-white-e1-0 Bishop-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Queen-white-a1 King-white-e1-0 Queen-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Pawn-white-a2-false King-white-e1-0 Pawn-black-a7-false King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Pawn-white-e2-false King-white-e1-0 Pawn-black-e7-false King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Pawn-white-e2-false Pawn-white-f3-false King-white-e1-0 Pawn-black-e7-false Pawn-black-f6-false King-black-e8-0")},
                new Object[]{new ChessGame("white 0 "
                        + "King-white-e1-0 "
                        + "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
                        + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                        + "Pawn-white-g2-false Pawn-white-h2-false "
                        + "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
                        + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                        + "Pawn-black-g7-false Pawn-black-h7-false "
                        + "King-black-e8-0")},
                new Object[]{new ChessGame("white 0 "
                        + "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                        + "Queen-white-d1 King-white-e1-0 "
                        + "Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                        + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                        + "Queen-black-d8 King-black-e8-0 "
                        + "Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")},
                new Object[]{new ChessGame(518)},
                new Object[]{new ChessGame(333)},
                new Object[]{new ChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1", "e8-h8", "c2-c3", "c7-c6")},
        };
    }

    @Test(dataProvider = "asymmetricPositionProvider")
    public void testAsymmetricPositionLeadsToInverseEvaluation(ChessGame game) {
        StaticEvaluation staticEval = StaticEvaluation.INSTANCE;
        Ongoing evalForWhite = staticEval.getPrimaryEvaluation(game, true);
        Ongoing evalForBlack = staticEval.getPrimaryEvaluation(game, false);
        double primaryEvalForWhite = evalForWhite.getPrimaryEvaluation();
        double primaryEvalForBlack = evalForBlack.getPrimaryEvaluation();
        if(primaryEvalForWhite!=0 || primaryEvalForBlack!=0)
        assertEquals(primaryEvalForWhite, -primaryEvalForBlack);

        staticEval.addSecondaryEvaluation(game, true, evalForWhite);
        staticEval.addSecondaryEvaluation(game, false, evalForBlack);
        double combinedEvalForWhite = evalForWhite.getCombinedEvaluation();
        double combinedEvalForBlack = evalForBlack.getCombinedEvaluation();
        assertEquals(combinedEvalForWhite, -combinedEvalForBlack, OK_DELTA);
        // it's highly unlikely that an asymmetric position is considered equal
        assertNotEquals(combinedEvalForWhite, 0.0, OK_DELTA);
    }

    @DataProvider
    public Object[][] asymmetricPositionProvider() {
        return new Object[][] {
                new Object[]{new ChessGame("black 0 Rook-white-a1-0 King-white-e1-0 King-black-e8-0")},
                new Object[]{new ChessGame(518, "a2-a3")},
                new Object[]{new ChessGame(333, "d2-d4", "a8-b6", "e2-e4", "h8-g6")},
                new Object[]{new ChessGame(518, "e2-e4", "d7-d5", "e4-d5")},
                new Object[]{new ChessGame(518, "g2-g3", "g7-g6", "g1-f3", "g8-f6", "f1-g2", "f8-g7", "e1-h1")},
        };
    }
}

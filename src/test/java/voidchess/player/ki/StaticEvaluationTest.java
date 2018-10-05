package voidchess.player.ki;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.MoveResult;
import voidchess.helper.Move;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.Ongoing;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;


public class StaticEvaluationTest {

    @Test(dataProvider = "symmetricPositionProvider")
    public void testSymmetricPositionLeadsToEqualEvaluation(ChessGame game) {
        StaticEvaluation staticEval = StaticEvaluation.INSTANCE;
        Ongoing evalForWhite = staticEval.getPrimaryEvaluation(game, true);
        Ongoing evalForBlack = staticEval.getPrimaryEvaluation(game, false);
        assertTrue(evalForWhite.getPrimaryEvaluation() == 0.0);
        assertTrue(evalForBlack.getPrimaryEvaluation() == 0.0);

        staticEval.addSecondaryEvaluation(game, true, evalForWhite);
        staticEval.addSecondaryEvaluation(game, false, evalForBlack);
        assertTrue(evalForWhite.getCombinedEvaluation() == 0.0);
        assertTrue(evalForBlack.getCombinedEvaluation() == 0.0);
    }

    @DataProvider
    public Object[][] symmetricPositionProvider() {
        return new Object[][] {
                new Object[]{new ChessGame("black 0 Rook-white-a1-0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Knight-white-a1 King-white-e1-0 Knight-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Bishop-white-a1 King-white-e1-0 Bishop-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Queen-white-a1 King-white-e1-0 Queen-black-a8 King-black-e8-0")},
                new Object[]{new ChessGame("black 0 Pawn-white-a2-false King-white-e1-0 Pawn-black-a7-false King-black-e8-0")},
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
        assertEquals(combinedEvalForWhite, -combinedEvalForBlack);
        // it's highly unlikely that an asymmetric position is considered equal
        assertNotEquals(combinedEvalForWhite, 0.0);
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

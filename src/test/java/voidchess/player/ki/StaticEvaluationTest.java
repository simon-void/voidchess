package voidchess.player.ki;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.player.ki.evaluation.Evaluated;

import static org.testng.Assert.assertTrue;

/**
 * @author stephan
 */
public class StaticEvaluationTest {
    @Test
    public void testEvaluation() {
        String des = "black 0 Rock-white-a1-0 King-white-e1-0 "
                + "Rock-black-a8-0 King-black-e8-0 ";
        ChessGame game = new ChessGame(des);

        StaticEvaluation evaluation = new StaticEvaluation();
        assertTrue(getCombinedEvaluation(evaluation.getPrimaryEvaluation(game, true)) == 0);
        assertTrue(getCombinedEvaluation(evaluation.getPrimaryEvaluation(game, false)) == 0);

        des = "black 0 Rock-white-a1-0 King-white-e1-0 King-black-e8-0";
        game = new ChessGame(des);

        assertTrue(getCombinedEvaluation(evaluation.getPrimaryEvaluation(game, true)) > 0);
        assertTrue(getCombinedEvaluation(evaluation.getPrimaryEvaluation(game, false)) < 0);
    }

    private float getCombinedEvaluation(Evaluated evaluated) {
        return Float.parseFloat(evaluated.toString().replace(',', '.'));
    }
}

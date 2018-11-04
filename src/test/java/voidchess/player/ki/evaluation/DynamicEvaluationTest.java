package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.move.Move;
import voidchess.player.ki.evaluation.DynamicEvaluation;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.Ongoing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class DynamicEvaluationTest {
    @Test
    public void testEvaluateMoveHasNoSideEffects() {
        String des = "black 0 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false";
        ChessGame game = new ChessGame(des);
        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation();

        dynamicEvaluation.evaluateMove(game, Move.byCode("a6-b6"));
        // invariance: evaluateMove must not change the game configuration
        assertEquals(game.toString(), des);
    }

    @Test
    public void testMinMaxScheme() {
        ChessGame game = new ChessGame(518, "d2-d3", "d7-d6", "c1-g5");

        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation();

        Evaluated value = dynamicEvaluation.evaluateMove(game, Move.byCode("e7-e6")); // the queen can be taken via g5-d8

        // The best move for white is obviously taking the queen
        // so the value of the move e7-e6 should be around -6
        // because a queen(9P) is exchanged against a bishop(3P).
        // Actually worse than than -6 because the white queen will probably move to d2 to gain space with the
        // expected sequence (after e7-e6) g5-d8 e8-d8 d1-d2
        double combinedValue = ((Ongoing)value).getCombinedEvaluation();
        assertTrue(
                combinedValue<-6.0 && combinedValue>-7.5,  // the queen mobility isn't worth a pawn so combined value shouldn't be worse than -7
                "Min-Max-computation out of bounds. expected value [-6, -7.5] but is: " + combinedValue);
    }
}

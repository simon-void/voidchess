/*
 * Created on 15.10.2006
 */

package voidchess.player.ki;

import voidchess.board.ChessGame;
import voidchess.helper.Move;
import org.testng.annotations.Test;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedAsValue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DynamicEvaluationTest {
    @Test
    public void testEvaluateMove() {
        String des = "black 0 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false";
        ChessGame game = new ChessGame(des);
        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation();
        Move move = Move.get("a6-b6");

        dynamicEvaluation.evaluateMove(game, move);
        //Invariante: evaluateMove darf game nicht ändern
        assertEquals(des, game.toString());
    }

    @Test
    public void testMinMaxSheme() {
        String des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 " +
                "Queen-white-d1 King-white-e1-0 Bishop-white-c1 Knight-white-g1 Rock-white-h1-0 " +
                "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false " +
                "Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false " +
                "Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false " +
                "Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 " +
                "Queen-black-d8 King-black-e8-0 Bishop-black-c8 Knight-black-g8 Rock-black-h8-0";

        ChessGame game = new ChessGame(des);
        game.move(Move.get("d2-d3"));
        game.move(Move.get("d7-d6"));
        game.move(Move.get("c1-g5"));
        game.move(Move.get("e7-e6"));    //??Dame kann geschlagen werden

        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation();

        Evaluated value = dynamicEvaluation.evaluateMove(game, Move.get("g5-d8"));

        //Schlagen der Königing ist der beste Zug
        //der Wert des Zuges sollte um 6 liegen,
        //da ich eine Dame(9P) gegen einen Läufer(3P) getauscht habe
        //+Weiß kann durch den Damenzug noch Raum gewinnen
        assertTrue(
                value.compareTo(new EvaluatedAsValue(5.5f)) > 0,
                "Min-Max-Berechnung ist zu niedrig,Soll:~6.5,Ist:" + value);
        assertTrue(
                value.compareTo(new EvaluatedAsValue(7.5f)) < 0,
                "Min-Max-Berechnung ist zu hoch,Soll:~6.5,Ist:" + value);
    }

    @Test
    public void testFailingPosition() {
        ChessGame game = new ChessGame(518);
        game.move(Move.get("e2-e4"));
        game.move(Move.get("d7-d5"));
        game.move(Move.get("e4-e5"));

        SearchTreePruner pruner = new SimplePruner(2, 4, 3);
        assertEquals(game.countFigures(), 32, "figureCount");
        evaluateDynamic(game, Move.get("e8-d7"), pruner);
        assertEquals(game.countFigures(), 32, "figureCount");
    }

    private void evaluateDynamic(ChessGame game, Move move, SearchTreePruner pruner) {
        final String initDescription = game.toString();
        StaticEvaluationInterface strategy = new StaticEvaluation();
        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation(pruner, strategy);

        try {
            dynamicEvaluation.evaluateMove(game, move);
            //Invariante: evaluateMove darf game nicht ändern
            String msg = "after Move:" + move.toString() + " History:" + game.getHistory();
            assertEquals(initDescription, game.toString(), msg);
        } catch (Exception e) {
            String gamestring = game.toString();
            throw new RuntimeException(e.toString() + "-after Moves:" + game.getCompleteHistory() + " -leading to position:" + gamestring);
        } catch (AssertionError e) {
            AssertionError extendedE = new AssertionError(e.getMessage() + " History:" + game.getHistory());
            extendedE.setStackTrace(e.getStackTrace());
            throw extendedE;
        }
    }
}

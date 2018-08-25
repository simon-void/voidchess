/*
 * Created on 08.11.2006
 */

package voidchess.player.ki;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.helper.Position;
import voidchess.player.ki.evaluation.Evaluated;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StaticSpaceEvaluationTest {
    StaticSpaceEvaluation evaluation = new StaticSpaceEvaluation();

    @Test
    public void testEvaluation() {
        String des = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        Evaluated evaluateForWhite = evaluation.getPrimaryEvaluation(game, true);
        Evaluated evaluateForBlack = evaluation.getPrimaryEvaluation(game, false);

        assertTrue(getCombinedEvaluation(evaluateForWhite) < 0);
        assertTrue(getCombinedEvaluation(evaluateForBlack) > 0);
        assertEquals(getCombinedEvaluation(evaluateForBlack), -getCombinedEvaluation(evaluateForWhite));


        des = "black 0 King-white-e1-0 Rock-black-b8-0 King-black-e8-0";
        ChessGame game2 = new ChessGame(des);

        Evaluated evaluateForWhite2 = evaluation.getPrimaryEvaluation(game2, true);
        assertTrue(evaluateForWhite2.compareTo(evaluateForWhite) < 0);
    }

    private float getCombinedEvaluation(Evaluated evaluated) {
        return Float.parseFloat(evaluated.toString().replace(',', '.'));
    }

    @Test
    public void testGetMaxFigureDistance() {
        assertEquals(7, evaluation.getKingDistance(Position.get("e1"), Position.get("e8")));
        assertEquals(4, evaluation.getKingDistance(Position.get("b7"), Position.get("e8")));
    }

    @Test
    public void testCountRestSpace() {
        String des = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        List otherPositions = new LinkedList();
        otherPositions.add(Position.get("e8"));
        otherPositions.add(Position.get("a8"));
        Position kingPos = Position.get("e1");

        assertEquals(48, evaluation.countRestSpace(game, kingPos, otherPositions));

        des = "black 0 King-white-e1-0 Rock-black-e7-0 King-black-e8-0";
        game = new ChessGame(des);

        otherPositions.clear();
        otherPositions.add(Position.get("e8"));
        otherPositions.add(Position.get("e7"));

        assertEquals(43, evaluation.countRestSpace(game, kingPos, otherPositions));
    }
}

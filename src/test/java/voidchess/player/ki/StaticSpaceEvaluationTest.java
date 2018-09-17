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
        String des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        Evaluated evaluateForWhite = evaluation.getPrimaryEvaluation(game, true);
        Evaluated evaluateForBlack = evaluation.getPrimaryEvaluation(game, false);

        assertTrue(getCombinedEvaluation(evaluateForWhite) < 0);
        assertTrue(getCombinedEvaluation(evaluateForBlack) > 0);
        assertEquals(getCombinedEvaluation(evaluateForBlack), -getCombinedEvaluation(evaluateForWhite));


        des = "black 0 King-white-e1-0 Rook-black-b8-0 King-black-e8-0";
        ChessGame game2 = new ChessGame(des);

        Evaluated evaluateForWhite2 = evaluation.getPrimaryEvaluation(game2, true);
        assertTrue(evaluateForWhite2.compareTo(evaluateForWhite) < 0);
    }

    private float getCombinedEvaluation(Evaluated evaluated) {
        return Float.parseFloat(evaluated.toString().replace(',', '.'));
    }

    @Test
    public void testGetMaxFigureDistance() {
        assertEquals(evaluation.getKingDistance(Position.Companion.byCode("e1"), Position.Companion.byCode("e8")), 7);
        assertEquals(evaluation.getKingDistance(Position.Companion.byCode("b7"), Position.Companion.byCode("e8")), 4);
    }

    @Test
    public void testCountRestSpace() {
        String des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        List otherPositions = new LinkedList();
        otherPositions.add(Position.Companion.byCode("e8"));
        otherPositions.add(Position.Companion.byCode("a8"));
        Position kingPos = Position.Companion.byCode("e1");

        assertEquals(evaluation.countRestSpace(game, kingPos, otherPositions), 48);

        des = "black 0 King-white-e1-0 Rook-black-e7-0 King-black-e8-0";
        game = new ChessGame(des);

        otherPositions.clear();
        otherPositions.add(Position.Companion.byCode("e8"));
        otherPositions.add(Position.Companion.byCode("e7"));

        assertEquals(evaluation.countRestSpace(game, kingPos, otherPositions), 43);
    }
}

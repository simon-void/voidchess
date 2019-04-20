package voidchess.player.ki.evaluation;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.move.Position;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StaticSpaceEvaluationTest {
    private StaticSpaceEvaluation evaluation = new StaticSpaceEvaluation();

    @Test
    public void testEvaluation() {
        String des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        Evaluated evaluateForWhite = evaluation.getPrimaryEvaluation(game, true);
        Evaluated evaluateForBlack = evaluation.getPrimaryEvaluation(game, false);

        double combinedEvalForWhite = getCombinedEvaluation(evaluateForWhite);
        double combinedEvalForBlack = getCombinedEvaluation(evaluateForBlack);

        assertTrue(combinedEvalForWhite < 0);
        assertTrue(combinedEvalForBlack > 0);
        assertEquals(combinedEvalForBlack, -combinedEvalForWhite);


        des = "black 0 King-white-e1-0 Rook-black-b8-0 King-black-e8-0";
        ChessGame game2 = new ChessGame(des);

        Evaluated evaluateForWhite2 = evaluation.getPrimaryEvaluation(game2, true);
        assertTrue(evaluateForWhite2.compareTo(evaluateForWhite) < 0);
    }

    private double getCombinedEvaluation(Evaluated evaluated) {
        Ongoing ongoing = (Ongoing)evaluated;
        ongoing.setSecondaryEvaluation(0.0);
        return ongoing.getCombinedEvaluation();
    }

    @Test
    public void testGetMaxFigureDistance() {
        assertEquals(evaluation.getKingDistance(Position.byCode("e1"), Position.byCode("e8")), 7);
        assertEquals(evaluation.getKingDistance(Position.byCode("b7"), Position.byCode("e8")), 4);
    }

    @Test
    public void testCountRestSpace() {
        String des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        List<Position> otherPositions = new LinkedList<>();
        otherPositions.add(Position.byCode("e8"));
        otherPositions.add(Position.byCode("a8"));
        Position kingPos = Position.byCode("e1");

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos), otherPositions), 48);

        des = "black 0 King-white-e1-0 Rook-black-e7-0 King-black-e8-0";
        game = new ChessGame(des);

        otherPositions.clear();
        otherPositions.add(Position.byCode("e8"));
        otherPositions.add(Position.byCode("e7"));

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos), otherPositions), 43);
    }
}

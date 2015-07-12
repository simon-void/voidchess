package player.ki.concurrent;

import board.ChessGameInterface;
import helper.Move;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;
import player.ki.Evaluaded;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class SingleThreadStrategy extends AbstractConcurrencyStrategy {
    SingleThreadStrategy(AbstractComputerPlayerUI ui) {
        super(ui);
    }

    @Override
    public SortedSet<EvaluatedMove> evaluatePossibleMoves(ChessGameInterface game, DynamicEvaluation dynamicEvaluation) {
        final List<Move> possibleMoves = getPossibleMoves(game);

        final int totalNumberOfMoves = possibleMoves.size();
        showProgress(0, totalNumberOfMoves);

        int movesDone = 0;
        final SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
        for (Move move : possibleMoves) {
            Evaluaded value = dynamicEvaluation.evaluateMove(game, move);
            result.add(new EvaluatedMove(move, value));

            showProgress(++movesDone, totalNumberOfMoves);
        }

        return result;
    }
}

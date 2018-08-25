package voidchess.player.ki.concurrent;

import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.player.ki.AbstractComputerPlayerUI;
import voidchess.player.ki.DynamicEvaluation;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

class SingleThreadStrategy extends AbstractConcurrencyStrategy {
    SingleThreadStrategy(AbstractComputerPlayerUI ui) {
        super(ui);
    }

    @Override
    public NavigableSet<EvaluatedMove> evaluatePossibleMoves(ChessGameInterface game, DynamicEvaluation dynamicEvaluation) {
        final List<Move> possibleMoves = getPossibleMoves(game);

        final int totalNumberOfMoves = possibleMoves.size();
        showProgress(0, totalNumberOfMoves);

        int movesDone = 0;
        final NavigableSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
        for (Move move : possibleMoves) {
            Evaluated value = dynamicEvaluation.evaluateMove(game, move);
            result.add(new EvaluatedMove(move, value));

            showProgress(++movesDone, totalNumberOfMoves);
        }

        return result;
    }
}

package player.ki.concurrent;

import board.ChessGameInterface;
import helper.Move;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

abstract class AbstractConcurrencyStrategy implements ConcurrencyStrategy {
    final private AbstractComputerPlayerUI ui;
    final private List<Move> possibleMovesBuffer = new ArrayList<>(80);

    public AbstractConcurrencyStrategy(AbstractComputerPlayerUI ui) {
        this.ui = ui;
    }

    /* (non-Javadoc)
     * @see player.ki.concurrent.ConcurrencyStrategy#evaluatePossibleMoves(board.ChessGameInterface, player.ki.DynamicEvaluation)
     */
    public abstract SortedSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation);

    final protected void showProgress(int movesDone, final int totalNumberOfMoves) {
        ui.setProgress(movesDone, totalNumberOfMoves);
    }

    final protected List<Move> getPossibleMoves(ChessGameInterface game) {
        possibleMovesBuffer.clear();
        game.getPossibleMoves(possibleMovesBuffer);
        return possibleMovesBuffer;
    }
}

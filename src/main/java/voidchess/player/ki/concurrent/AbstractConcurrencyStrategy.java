package voidchess.player.ki.concurrent;

import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.player.ki.AbstractComputerPlayerUI;
import voidchess.player.ki.DynamicEvaluation;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

abstract class AbstractConcurrencyStrategy implements ConcurrencyStrategy {
    final private AbstractComputerPlayerUI ui;
    final private List<Move> possibleMovesBuffer = new ArrayList<>(80);

    public AbstractConcurrencyStrategy(AbstractComputerPlayerUI ui) {
        this.ui = ui;
    }

    /* (non-Javadoc)
     * @see voidchess.player.ki.concurrent.ConcurrencyStrategy#evaluatePossibleMoves(voidchess.board.ChessGameInterface, voidchess.player.ki.DynamicEvaluation)
     */
    public abstract NavigableSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation);

    final protected void showProgress(int movesDone, final int totalNumberOfMoves) {
        ui.setProgress(movesDone, totalNumberOfMoves);
    }

    final protected List<Move> getPossibleMoves(ChessGameInterface game) {
        possibleMovesBuffer.clear();
        game.getPossibleMoves(possibleMovesBuffer);
        return possibleMovesBuffer;
    }
}

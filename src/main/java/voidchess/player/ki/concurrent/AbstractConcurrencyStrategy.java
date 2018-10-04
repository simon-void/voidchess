package voidchess.player.ki.concurrent;

import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.ui.ComputerPlayerUI;
import voidchess.player.ki.DynamicEvaluation;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

abstract class AbstractConcurrencyStrategy implements ConcurrencyStrategy {
    final private ComputerPlayerUI ui;
    final private List<Move> possibleMovesBuffer = new ArrayList<>(80);

    AbstractConcurrencyStrategy(ComputerPlayerUI ui) {
        this.ui = ui;
    }

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

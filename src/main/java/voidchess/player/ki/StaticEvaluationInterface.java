package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.player.ki.evaluation.Evaluated;

/**
 * @author stephan
 */
public interface StaticEvaluationInterface {
    public abstract Evaluated getPrimaryEvaluation(ChessGameInterface game, boolean forWhite);

    public abstract void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation);
}
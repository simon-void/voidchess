package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.player.ki.evaluation.Evaluated;

/**
 * @author stephan
 */
public interface StaticEvaluationInterface {
    Evaluated getPrimaryEvaluation(ChessGameInterface game, boolean forWhite);

    void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation);
}
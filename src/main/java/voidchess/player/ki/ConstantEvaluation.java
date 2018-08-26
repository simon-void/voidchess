package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedAsDraw;

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 *
 * @author stephan
 */

public class ConstantEvaluation implements StaticEvaluationInterface {
    @Override
    public Evaluated getPrimaryEvaluation(ChessGameInterface game, boolean forWhite) {
        return EvaluatedAsDraw.INSTANCE;
    }

    @Override
    public void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation) {
        //do nothing (keep the secondary value = 0)
    }
}

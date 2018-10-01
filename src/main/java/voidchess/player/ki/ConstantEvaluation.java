package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.Draw;

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 */

public class ConstantEvaluation implements StaticEvaluationInterface {
    @Override
    public Evaluated getPrimaryEvaluation(ChessGameInterface game, boolean forWhite) {
        return Draw.INSTANCE;
    }

    @Override
    public void addSecondaryEvaluation(ChessGameInterface game, boolean forWhite, Evaluated withPrimaryEvaluation) {
        //do nothing (keep the secondary value = 0)
    }
}

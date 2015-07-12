package player.ki;

import board.ChessGameInterface;

/**
 * @author stephan
 */
interface StaticEvaluationInterface {
    public abstract Evaluaded evaluate(ChessGameInterface game, boolean forWhite);
}
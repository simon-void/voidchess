package voidchess.player.ki;

import voidchess.board.ChessGameInterface;

/**
 * @author stephan
 */
interface StaticEvaluationInterface {
    public abstract Evaluaded evaluate(ChessGameInterface game, boolean forWhite);
}
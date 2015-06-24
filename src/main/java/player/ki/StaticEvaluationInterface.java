package player.ki;

import board.ChessGameInterface;

/**
 * @author stephan
 */
interface StaticEvaluationInterface
{
	public abstract float evaluate(ChessGameInterface game, boolean forWhite);
}
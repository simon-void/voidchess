package player.ki.concurrent;

import java.util.SortedSet;

import player.ki.DynamicEvaluation;
import board.ChessGameInterface;

public interface ConcurrencyStrategy
{
	/**
	 * @param game
	 * @param dynamicEvaluation
	 * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer player".
	 *         The first element is the best choice for the computer player and the last element being the worst.
	 */
	public abstract SortedSet<EvaluatedMove> evaluatePossibleMoves(
			final ChessGameInterface game,
			final DynamicEvaluation dynamicEvaluation);

}
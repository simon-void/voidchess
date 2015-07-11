package player.ki.concurrent;

import board.ChessGameInterface;
import player.ki.DynamicEvaluation;

import java.util.SortedSet;

public interface ConcurrencyStrategy {
    /**
     * @param game
     * @param dynamicEvaluation
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer player".
     * The first element is the best choice for the computer player and the last element being the worst.
     */
    public abstract SortedSet<EvaluatedMove> evaluatePossibleMoves(
            final ChessGameInterface game,
            final DynamicEvaluation dynamicEvaluation);

}
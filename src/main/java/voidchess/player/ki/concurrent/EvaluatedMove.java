package voidchess.player.ki.concurrent;

import voidchess.helper.Move;
import voidchess.player.ki.Evaluaded;

public class EvaluatedMove implements Comparable<EvaluatedMove> {
    private final Move move;
    private final Evaluaded value;

    public EvaluatedMove(final Move move, final Evaluaded value) {
        assert move != null : "move parameter mustn't be null";

        this.move = move;
        this.value = value;
    }

    public Move getMove() {
        return move;
    }

    public Evaluaded getValue() {
        return value;
    }

    public int compareTo(EvaluatedMove other) {
        //moves with high evaluations should come first in a sorted list
        return other.value.compareTo(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EvaluatedMove)) return false;
        EvaluatedMove other = (EvaluatedMove) obj;

        return move.equalsMove(other.move);
    }

    @Override
    public int hashCode() {
        return move.hashCode();
    }
}

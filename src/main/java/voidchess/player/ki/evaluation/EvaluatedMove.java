package voidchess.player.ki.evaluation;

import voidchess.helper.Move;

public class EvaluatedMove implements Comparable<EvaluatedMove> {
    private final Move move;
    private final Evaluated value;

    public EvaluatedMove(final Move move, final Evaluated value) {
        assert move != null : "move parameter mustn't be null";

        this.move = move;
        this.value = value;
    }

    public Move getMove() {
        return move;
    }

    public Evaluated getValue() {
        return value;
    }

    public int compareTo(EvaluatedMove other) {
        //moves with high evaluations should come first in a sorted list
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        EvaluatedMove other = (EvaluatedMove) obj;

        return move.equalsMove(other.move);
    }

    @Override
    public int hashCode() {
        return move.hashCode();
    }
}

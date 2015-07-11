package player.ki.concurrent;

import helper.Move;

public class EvaluatedMove implements Comparable<EvaluatedMove> {
    private final Move move;
    private final float value;

    EvaluatedMove(final Move move, final float value) {
        assert move != null : "move parameter mustn't be null";

        this.move = move;
        this.value = value;
    }

    public Move getMove() {
        return move;
    }

    public float getValue() {
        return value;
    }

    public int compareTo(EvaluatedMove other) {
        if (value == other.value) return 0;

        if (value < other.value) return 1;
        else return -1;
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

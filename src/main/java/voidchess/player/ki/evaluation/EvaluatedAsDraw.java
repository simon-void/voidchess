package voidchess.player.ki.evaluation;

/**
 * Created by stephan on 13.07.2015.
 */
final public class EvaluatedAsDraw implements Evaluated {
    final public static Evaluated INSTANCE = new EvaluatedAsDraw();

    private EvaluatedAsDraw() {}

    @Override
    public boolean isMatt() {
        return false;
    }

    @Override
    public boolean isDraw() {
        return true;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public void setSecondaryEvaluation(float secondaryEvaluation) {
        throw new UnsupportedOperationException("DrawValue has no secondaryEvaluation");
    }

    @Override
    public boolean needsSecondaryEvaluation() {
        return false;
    }

    @Override
    public boolean isCloseToByPrimary(Evaluated other) {
        if(other.isDraw()) {
            return true;
        }
        return other.isCloseToByPrimary(this);
    }

    @Override
    public boolean isCloseToByCombined(Evaluated other) {
        if(other.isDraw()) {
            return true;
        }
        return other.isCloseToByCombined(this);
    }

    @Override
    public int compareTo(Evaluated other) {
        if(other.isDraw()) {
            return 0;
        }else{
            return -other.compareTo(this);
        }
    }

    @Override
    public String toString() {
        return EvaluatedAsValue.format(0f);
    }
}

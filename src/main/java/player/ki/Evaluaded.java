package player.ki;

import java.text.DecimalFormat;

/**
 * Created by stephan on 12.07.2015.
 */
final public class Evaluaded implements Comparable<Evaluaded> {
    final public static Evaluaded DRAW = new Evaluaded(0,0);
    final public static Evaluaded INITAL = new Evaluaded(Float.MIN_VALUE, 0);
    final private static float MAX_FLOAT_VALUE = 1000f;
    final private static float MIN_FLOAT_VALUE = -MAX_FLOAT_VALUE;
    final private static float PRIMARY_EQUALITY_CUTOF_RADIUS = 1f;
    final private static float EQUALITY_RADIUS = 0.5f;
    final private static float MAXIMAL_MATT_DEPTH = 100;

    final float primaryEvaluation;
    final float secondaryEvaluation;

    private Evaluaded(float primaryEvaluation, float secondaryEvaluation) {
        this.primaryEvaluation = primaryEvaluation;
        this.secondaryEvaluation = secondaryEvaluation;
    }

    public static Evaluaded fromValues(float primaryEvaluation, float secondaryEvaluation) {
        float combined = primaryEvaluation+secondaryEvaluation;
        //shouldn't happen but better be safe
        if (combined < MIN_FLOAT_VALUE) {
            primaryEvaluation = MIN_FLOAT_VALUE;
            secondaryEvaluation = 0f;
        }
        else if (combined > MAX_FLOAT_VALUE) {
            primaryEvaluation = MAX_FLOAT_VALUE;
            secondaryEvaluation = 0f;
        }
        return new Evaluaded(primaryEvaluation, secondaryEvaluation);
    }

    public static Evaluaded getOtherPlayerIsMatt(int depth) {
        assert depth < MAXIMAL_MATT_DEPTH && depth > 0;
        return new Evaluaded(MIN_FLOAT_VALUE - (MAXIMAL_MATT_DEPTH - depth),0);
    }

    public static Evaluaded getThisComputerPlayerIsMatt(int depth) {
        assert depth < MAXIMAL_MATT_DEPTH && depth > 0;
        return new Evaluaded(MAX_FLOAT_VALUE + (MAXIMAL_MATT_DEPTH - depth),0);
    }

    public boolean hasAlmostSameValue(Evaluaded other) {
        return Math.abs(getCombinedEvaluation() - other.getCombinedEvaluation()) <= EQUALITY_RADIUS;
    }

    @Override
    public int compareTo(Evaluaded o) {
        if(o==null) return 1;

        if(Math.abs(primaryEvaluation-o.primaryEvaluation)>PRIMARY_EQUALITY_CUTOF_RADIUS) {
            //compare only by primaryEvaluation
            return primaryEvaluation>o.primaryEvaluation ? 1 : -1;
        }else{
            //compare by combinedEvaluation
            float combinedEval = getCombinedEvaluation();
            float otherCombinedEval = o.getCombinedEvaluation();

            if(combinedEval>otherCombinedEval) return 1;
            if(combinedEval<otherCombinedEval) return -1;
            return 0;
        }
    }

    public float getCombinedEvaluation() {
        return primaryEvaluation+secondaryEvaluation;
    }

    @Override
    public String toString() {
        final float value = getCombinedEvaluation();
        if (value < MIN_FLOAT_VALUE) {
            return "ich bin matt in " + Math.round(value + MAXIMAL_MATT_DEPTH - MIN_FLOAT_VALUE);
        }
        if (value > MAX_FLOAT_VALUE) {
            return "du bist matt in " + Math.round(MAXIMAL_MATT_DEPTH + MAX_FLOAT_VALUE - value);
        }

        DecimalFormat formater = new DecimalFormat();
        formater.setMinimumFractionDigits(2);
        formater.setMaximumFractionDigits(2);
        return formater.format(value);
    }
}

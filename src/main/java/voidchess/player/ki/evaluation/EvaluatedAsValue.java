package voidchess.player.ki.evaluation;

import java.text.DecimalFormat;

/**
 * Created by stephan on 13.07.2015.
 */
final public class EvaluatedAsValue implements Evaluated {
    final static float PRIMARY_EQUALITY_CUTOF_RADIUS = 1f;
    final static float FINAL_EQUALITY_CUTOF_RADIUS = 0.5f;

    private boolean needsSecondaryEvaluationInitialized = true;
    final private float primaryEvaluation;
    private float secondaryEvaluation;

    public EvaluatedAsValue(float primaryEvaluation) {
        this.primaryEvaluation = primaryEvaluation;
    }

    @Override
    public void setSecondaryEvaluation(float secondaryEvaluation) {
        assert needsSecondaryEvaluationInitialized == true;

        this.secondaryEvaluation = secondaryEvaluation;
        needsSecondaryEvaluationInitialized = false;
    }

    @Override
    public boolean needsSecondaryEvaluation() {
        return needsSecondaryEvaluationInitialized;
    }

    @Override
    public boolean isCloseToByPrimary(Evaluated other) {
        if(other.isValue()) {
            float otherPrimaryEvaluation = ((EvaluatedAsValue) other).primaryEvaluation;
            return isAbsEvaluationEqualOrLess(primaryEvaluation-otherPrimaryEvaluation, PRIMARY_EQUALITY_CUTOF_RADIUS);
        }else if(other.isDraw()) {
            return isAbsEvaluationEqualOrLess(primaryEvaluation, PRIMARY_EQUALITY_CUTOF_RADIUS);
        }else {
            //EvaluateAsMatt
            return false;
        }
    }

    @Override
    public boolean isCloseToByCombined(Evaluated other) {
        if(other.isValue()) {
            float otherCombinedEvaluation = ((EvaluatedAsValue) other).getCombinedEvaluation();
            return isAbsEvaluationEqualOrLess(getCombinedEvaluation()-otherCombinedEvaluation, FINAL_EQUALITY_CUTOF_RADIUS);
        }else if(other.isDraw()) {
            return isAbsEvaluationEqualOrLess(getCombinedEvaluation(), FINAL_EQUALITY_CUTOF_RADIUS);
        }else {
            //EvaluateAsMatt
            return false;
        }
    }

    private boolean isAbsEvaluationEqualOrLess(float value, float okRadius) {
        value = Math.abs(value);
        return value<=okRadius;
    }

    @Override
    public int compareTo(Evaluated other) {
        if(other.isDraw()) {
            final float combinedEvaluation = getCombinedEvaluation();
            return (int)Math.signum(combinedEvaluation);
        } else if(other.isValue()){
            return compareWith((EvaluatedAsValue)other);
        } else {
            //EvaluateAsMatt
            return -other.compareTo(this);
        }
    }

    private int compareWith(EvaluatedAsValue other) {
        final float difference = getCombinedEvaluation()-other.getCombinedEvaluation();
        return (int)Math.signum(difference);
    }

    float getCombinedEvaluation() {
        final float combinedEvaluation = primaryEvaluation + secondaryEvaluation;
        return combinedEvaluation;
    }

    @Override
    public boolean isMatt() {
        return false;
    }

    @Override
    public boolean isDraw() {
        return false;
    }

    @Override
    public boolean isValue() {
        return true;
    }

    @Override
    public String toString() {
        return format(getCombinedEvaluation());
    }

    static String format(float value) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMinimumFractionDigits(2);
        formater.setMaximumFractionDigits(2);

        return formater.format(value);
    }
}

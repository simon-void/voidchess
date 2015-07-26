package voidchess.player.ki.evaluation;

/**
 * Created by stephan on 12.07.2015.
 */
public interface Evaluated extends Comparable<Evaluated> {
    boolean isMatt();
    boolean isDraw();
    boolean isValue();
    void setSecondaryEvaluation(float secondaryEvaluation);
    boolean needsSecondaryEvaluation();
    boolean isCloseToByPrimary(Evaluated other);
    boolean isCloseToByCombined(Evaluated other);
}

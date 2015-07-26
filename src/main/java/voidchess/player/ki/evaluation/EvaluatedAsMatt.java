package voidchess.player.ki.evaluation;

/**
 * Created by stephan on 13.07.2015.
 */
final public class EvaluatedAsMatt implements Evaluated {
    final private int depth;
    final private boolean isOtherMatt;

    public EvaluatedAsMatt(int depth, boolean isOtherMatt) {
        assert depth>0;

        this.depth = depth;
        this.isOtherMatt = isOtherMatt;
    }

    @Override
    public boolean isMatt() {
        return true;
    }

    @Override
    public boolean isDraw() {
        return false;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isCloseToByPrimary(Evaluated other) {
        if(other.isMatt()) {
            EvaluatedAsMatt otherMattValue = (EvaluatedAsMatt) other;
            return depth==otherMattValue.depth && isOtherMatt==otherMattValue.isOtherMatt;
        }
        return false;
    }

    @Override
    public boolean isCloseToByCombined(Evaluated other) {
        return isCloseToByPrimary(other);
    }

    @Override
    public int compareTo(Evaluated other) {
        if(other.isMatt()) {
            return compareWith((EvaluatedAsMatt)other);
        }else{
            if(isOtherMatt) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private int compareWith(EvaluatedAsMatt other) {
        if(isOtherMatt!=other.isOtherMatt) {
            if(isOtherMatt) {
                return 1;
            } else {
                return -1;
            }
        }
        if(isOtherMatt) {
            //both are isOtherMatt=true
            return other.depth-depth;
        }else{
            //both are isOtherMatt=false
            return depth-other.depth;
        }
    }

    @Override
    public void setSecondaryEvaluation(float secondaryEvaluation) {
        throw new UnsupportedOperationException("MattValue has no secondaryEvaluation");
    }

    @Override
    public boolean needsSecondaryEvaluation() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        if(isOtherMatt) {
            sb.append("Du bist ");
        }else{
            sb.append("Ich bin ");
        }
        sb.append("matt in ").append(depth).append('.');

        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Evaluated) {
            return compareTo((Evaluated)other)==0;
        }else {
            return false;
        }
    }
}

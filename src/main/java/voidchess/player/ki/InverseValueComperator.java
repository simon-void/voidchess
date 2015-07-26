package voidchess.player.ki;

import voidchess.player.ki.evaluation.Evaluated;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author stephan
 */
public class InverseValueComperator implements Serializable, Comparator<Evaluated> {
    public int compare(Evaluated first, Evaluated second) {
        return second.compareTo(first);
    }
}

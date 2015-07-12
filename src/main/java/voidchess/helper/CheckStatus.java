package voidchess.helper;

import java.util.Collections;
import java.util.List;

/**
 * @author stephan
 */
public class CheckStatus {
    final public static CheckStatus NO_CHECK = new CheckStatus(false, false);
    final public static CheckStatus DOUBLE_CHECK = new CheckStatus(true, true);

    private boolean isCheck;
    private boolean onlyKingCanMove;
    private List<Position> possiblePositions;

    private CheckStatus(boolean isCheck, boolean onlyKingCanMove) {
        this.isCheck = isCheck;
        this.onlyKingCanMove = onlyKingCanMove;
        possiblePositions = Collections.emptyList();
    }

    CheckStatus(List<Position> possiblePositions) {
        isCheck = true;
        onlyKingCanMove = false;
        this.possiblePositions = possiblePositions;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public boolean onlyKingCanMove() {
        return onlyKingCanMove;
    }

    public List<Position> getCheckInterceptPositions() {
        return possiblePositions;
    }
}

package voidchess.helper;

/**
 * @author stephan
 */
public class ChessGameSupervisorMock implements ChessGameSupervisor {
    private PawnPromotion defaultPawnTransform = PawnPromotion.QUEEN;

    public ChessGameSupervisorMock(PawnPromotion defaultPawnTransform) {
        this.defaultPawnTransform = defaultPawnTransform;
    }

    public PawnPromotion askForPawnChange(Position pawnPosition) {
        return defaultPawnTransform;
    }
}

package voidchess.player;

import voidchess.helper.PawnPromotion;
import voidchess.helper.Position;

/**
 * @author stephan
 */
public interface PlayerInterface {
    void setIsPlaying(boolean isPlaying);

    void play();

    PawnPromotion askForPawnPromotionType(Position pawnPosition);

    void setColor(boolean isWhite);
}

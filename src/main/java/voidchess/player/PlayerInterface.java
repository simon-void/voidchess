package voidchess.player;

import voidchess.helper.Position;

/**
 * @author stephan
 */
public interface PlayerInterface {
    void setIsPlaying(boolean isPlaying);

    void play();

    String askForPawnChange(Position pawnPosition);

    void setColor(boolean isWhite);
}

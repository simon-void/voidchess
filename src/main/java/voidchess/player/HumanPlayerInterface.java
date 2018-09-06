package voidchess.player;

import voidchess.helper.Position;

/**
 * @author stephan
 */
public interface HumanPlayerInterface
        extends PlayerInterface {
    void mouseMovedOver(Position pos);

    void mouseClickedOn(Position pos);
}

package voidchess.player;

import voidchess.helper.Position;

/**
 * @author stephan
 */
public interface HumanPlayerInterface
        extends PlayerInterface {
    public void mouseMovedOver(Position pos);

    public void mouseClickedOn(Position pos);
}

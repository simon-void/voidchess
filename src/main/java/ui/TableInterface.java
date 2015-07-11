package ui;

import helper.Move;
import player.PlayerInterface;

/**
 * @author stephan
 */
public interface TableInterface {
    public void startGame();

    public void stopGame(int endoption);

    public void move(Move move);

    public void setWhitePlayer(PlayerInterface player);

    public void setBlackPlayer(PlayerInterface player);
}

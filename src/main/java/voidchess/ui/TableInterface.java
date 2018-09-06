package voidchess.ui;

import voidchess.board.MoveResult;
import voidchess.helper.Move;
import voidchess.player.PlayerInterface;

/**
 * @author stephan
 */
public interface TableInterface {
    void startGame();
    void stopGame(MoveResult endoption);
    void move(Move move);
    void setWhitePlayer(PlayerInterface player);
    void setBlackPlayer(PlayerInterface player);
}

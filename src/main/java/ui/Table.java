package ui;

import board.ChessGameInterface;
import helper.ChessGameSupervisor;
import helper.Move;
import helper.Position;
import player.HumanPlayerInterface;
import player.PlayerInterface;

/**
 * @author stephan
 */
public class Table implements ChessGameSupervisor, TableInterface {
    private ChessGameInterface game;
    private ChessGameUI ui;
    private Chess960Panel panel960;
    private PlayerInterface whitePlayer, blackPlayer;
    private boolean whitePlayersTurn, resign;
    private ChessPanel parent;


    public Table(ChessGameInterface game, ChessGameUI ui, ChessPanel parent, Chess960Panel panel960) {
        this.game = game;
        this.ui = ui;
        this.parent = parent;
        this.panel960 = panel960;
    }

    public String askForPawnChange(Position pawnPosition) {
        if (whitePlayersTurn) return whitePlayer.askForPawnChange(pawnPosition);
        else return blackPlayer.askForPawnChange(pawnPosition);
    }

    public void move(Move move) {
        int endoption;
        synchronized (this) {
            if (resign) {
                stopGame(ChessGameInterface.RESIGN);
                return;
            }
            endoption = game.move(move);
            ui.repaintAfterMove(move);

            whitePlayersTurn = !whitePlayersTurn;
        }
        if (endoption == ChessGameInterface.NO_END) {
            if (whitePlayersTurn) {
                whitePlayer.play();
            } else {
                blackPlayer.play();
            }
        } else {
            stopGame(endoption);
        }

    }

    public void setWhitePlayer(PlayerInterface player) {
        whitePlayer = player;
    }

    public void setBlackPlayer(PlayerInterface player) {
        blackPlayer = player;
    }

    public void startGame() {
        whitePlayersTurn = true;
        resign = false;
        game.initGame(panel960.getPositionCode());
        ui.repaintAtOnce();

        whitePlayer.setIsPlaying(true);
        blackPlayer.setIsPlaying(true);

        whitePlayer.play();
    }

    public void stopGame(int endoption) {
        whitePlayer.setIsPlaying(false);
        blackPlayer.setIsPlaying(false);
        parent.gameover(endoption);
    }

    synchronized void resignGame() {
        if (whitePlayersTurn == whitePlayer instanceof HumanPlayerInterface) {
            stopGame(ChessGameInterface.RESIGN);
        } else {
            resign = true;
        }
    }
}

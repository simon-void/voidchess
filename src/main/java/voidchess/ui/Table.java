package voidchess.ui;

import voidchess.board.ChessGameInterface;
import voidchess.board.MoveResult;
import voidchess.helper.ChessGameSupervisor;
import voidchess.helper.Move;
import voidchess.helper.PawnPromotion;
import voidchess.helper.Position;
import voidchess.player.PlayerInterface;

/**
 * @author stephan
 */
public class Table implements ChessGameSupervisor, TableInterface {
    private ChessGameInterface game;
    private ChessboardUI ui;
    private Chess960Panel panel960;
    private PlayerInterface whitePlayer, blackPlayer;
    private boolean whitePlayersTurn, resign;
    private ChessPanel parent;


    Table(ChessGameInterface game, ChessboardUI ui, ChessPanel parent, Chess960Panel panel960) {
        this.game = game;
        this.ui = ui;
        this.parent = parent;
        this.panel960 = panel960;
    }

    public PawnPromotion askForPawnChange(Position pawnPosition) {
        if (whitePlayersTurn) return whitePlayer.askForPawnPromotionType(pawnPosition);
        else return blackPlayer.askForPawnPromotionType(pawnPosition);
    }

    public void move(Move move) {
        MoveResult endoption;
        synchronized (this) {
            if (resign) {
                stopGame(MoveResult.RESIGN);
                return;
            }
            endoption = game.move(move);
            ui.repaintAfterMove(move);

            whitePlayersTurn = !whitePlayersTurn;
        }
        if (endoption == MoveResult.NO_END) {
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

    public void stopGame(MoveResult endoption) {
        whitePlayer.setIsPlaying(false);
        blackPlayer.setIsPlaying(false);
        parent.gameover(endoption);
    }
}

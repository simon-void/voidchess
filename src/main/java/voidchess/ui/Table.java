package voidchess.ui;

import voidchess.board.ChessGameInterface;
import voidchess.board.MoveResult;
import voidchess.helper.*;
import voidchess.player.PlayerInterface;

/**
 * @author stephan
 */
public class Table implements ChessGameSupervisor, TableInterface {
    private ChessGameInterface game;
    private ChessboardComponent ui;
    private Chess960Panel panel960;
    private PlayerInterface whitePlayer, blackPlayer;
    private boolean whitePlayersTurn, resign;
    private ChessPanel parent;


    Table(ChessGameInterface game, ChessboardComponent ui, ChessPanel parent, Chess960Panel panel960) {
        this.game = game;
        this.ui = ui;
        this.parent = parent;
        this.panel960 = panel960;
    }

    public PawnPromotion askForPawnChange(Position pawnPosition) {
        if (whitePlayersTurn) return whitePlayer.askForPawnPromotionType(pawnPosition);
        else return blackPlayer.askForPawnPromotionType(pawnPosition);
    }

    @Override
    public void move(Move move) {
        MoveResult endoption;
        synchronized (this) {
            if (resign) {
                stopGame(MoveResult.RESIGN);
                return;
            }
            endoption = game.move(move);
            ExtendedMove extendedMove = game.getLastExtendedMove();
            ui.repaintAfterMove(extendedMove);

            whitePlayersTurn = !whitePlayersTurn;
        }
        if (endoption == MoveResult.NO_END) {
            boolean gaveCheck = game.isCheck(whitePlayersTurn);
            if (whitePlayersTurn) {
                whitePlayer.play();
                if(gaveCheck) {
                    blackPlayer.gaveCheck();
                }
            } else {
                blackPlayer.play();
                if(gaveCheck) {
                    whitePlayer.gaveCheck();
                }
            }
        } else {
            stopGame(endoption);
        }
    }

    @Override
    public void setWhitePlayer(PlayerInterface player) {
        whitePlayer = player;
        player.setColor(true);
    }

    @Override
    public void setBlackPlayer(PlayerInterface player) {
        blackPlayer = player;
        player.setColor(false);
    }

    @Override
    public void switchPlayer() {
        PlayerInterface formerWhitePlayer = whitePlayer;
        setWhitePlayer(blackPlayer);
        setBlackPlayer(formerWhitePlayer);
    }

    @Override
    public void startGame() {
        whitePlayersTurn = true;
        resign = false;
        game.initGame(panel960.getPositionCode());
        ui.repaintAtOnce();

        whitePlayer.gameStarts();
        blackPlayer.gameStarts();

        whitePlayer.play();
    }

    @Override
    public void stopGame(MoveResult endoption) {
        whitePlayer.gameEnds(endoption, !whitePlayersTurn);
        blackPlayer.gameEnds(endoption, !whitePlayersTurn);
        parent.gameover();
    }
}

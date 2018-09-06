package voidchess.player;

import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.helper.PawnPromotion;
import voidchess.helper.Position;
import voidchess.ui.ChessGameUI;
import voidchess.ui.TableInterface;

import javax.swing.*;

/**
 * @author stephan
 */
public class HumanPlayer
        implements HumanPlayerInterface {
    private Position from;
    private ChessGameInterface game;
    private ChessGameUI ui;
    private TableInterface table;
    private boolean isWhitePlayer;
    private boolean isMyTurn;

    public HumanPlayer(TableInterface table,
                       boolean isWhitePlayer,
                       ChessGameUI ui,
                       ChessGameInterface game) {
        this.table = table;
        this.game = game;
        this.ui = ui;
        this.isWhitePlayer = isWhitePlayer;
        isMyTurn = false;
        ui.addPlayer(this);
    }

    public void play() {
        isMyTurn = true;
    }

    public void move(Move move) {
        dropMarkedPositions();
        isMyTurn = false;
        table.move(move);
    }

    public void mouseMovedOver(Position pos) {
        if (!isMyTurn) return;

        if (from == null) {
            if (game.isSelectable(pos, isWhitePlayer)) {
                ui.markPosition(pos, true);
            } else {
                ui.markPosition(null, true);
            }
        } else {
            if (game.isMoveable(from, pos, isWhitePlayer)) {
                ui.markPosition(pos, false);
            } else {
                ui.markPosition(null, false);
            }
        }
    }

    public void mouseClickedOn(Position pos) {
        if (!isMyTurn) return;

        if (from == null) {
            if (game.isSelectable(pos, isWhitePlayer)) {
                from = pos;
            }
        } else {
            if (game.isMoveable(from, pos, isWhitePlayer)) {
                move(Move.Companion.get(from, pos));
            }
        }
    }

    public PawnPromotion askForPawnPromotionType(Position pawnPosition) {
        String[] figs = {"Queen", "Rock", "Bishop", "Knight"};
        String out = (String) JOptionPane.showInputDialog(null,
                "Promote pawn to what type?",
                "pawn promotion",
                JOptionPane.QUESTION_MESSAGE,
                null,
                figs,
                "Queen");

        switch (out) {
            case "Queen":
                return PawnPromotion.QUEEN;
            case "Rock":
                return PawnPromotion.ROCK;
            case "Knight":
                return PawnPromotion.KNIGHT;
            default:
                return PawnPromotion.BISHOP;
        }
    }

    public void setIsPlaying(boolean isPlaying) {
        isMyTurn = isPlaying && isWhitePlayer;

        if (!isPlaying) {
            //removes selected fields even if game was aborted
            dropMarkedPositions();
        }
    }

    private void dropMarkedPositions() {
        from = null;
        ui.markPosition(null, true);
        ui.markPosition(null, false);
    }

    public void setColor(boolean isWhite) {
        isWhitePlayer = isWhite;
    }
}

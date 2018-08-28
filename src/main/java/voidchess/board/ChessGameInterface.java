package voidchess.board;

import voidchess.helper.ChessGameSupervisor;
import voidchess.helper.Move;
import voidchess.helper.Position;

import java.util.List;

/**
 * @author stephan
 */
public interface ChessGameInterface extends BasicChessGameInterface {
    int NO_END = 0;
    int MATT = 1;
    int PATT = 2;
    int DRAW = 3;
    int THREE_TIMES_SAME_POSITION = 4;
    int FIFTY_MOVES_NO_HIT = 5;
    int RESIGN = 6;

    void initGame(int chess960);

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    boolean isStandardGame();

    List<ChessGameInterface> copyGame(int neededInstances);

    boolean isWhiteTurn();

    int countFigures();

    boolean hasHitFigure();

    boolean isCheck(boolean isWhite);

    boolean isSelectable(Position pos, boolean whitePlayer);

    boolean isMoveable(Position from, Position to, boolean whitePlayer);

    boolean isFreeArea(Position pos);

    int move(Move move);

    void getPossibleMoves(List<Move> possibleMoves);

    int countReachableMoves(boolean isWhite);

    ChessGameSupervisor suspendInteractiveSupervisor();

    void useSupervisor(ChessGameSupervisor supervisor);

    void undo();

    /**
     * @return all the moves played so far in a string representation
     */
    String getCompleteHistory();
}
package voidchess.board;

import voidchess.helper.ChessGameSupervisor;
import voidchess.helper.Move;
import voidchess.helper.Position;

import java.util.List;

/**
 * @author stephan
 */
public interface ChessGameInterface extends BasicChessGameInterface {
    final public static int NO_END = 0;
    final public static int MATT = 1;
    final public static int PATT = 2;
    final public static int DRAW = 3;
    final public static int THREE_TIMES_SAME_POSITION = 4;
    final public static int FIFTY_MOVES_NO_HIT = 5;
    final public static int RESIGN = 6;

    public void initGame(int chess960);

    /**
     * returns true if this game was started with the standard distribution of figures
     * (chess960 code: 518)
     */
    public boolean isStandardGame();

    public List<ChessGameInterface> copyGame(int neededInstances);

    public boolean isWhiteTurn();

    public int countFigures();

    public boolean hasHitFigure();

    public boolean isCheck(boolean isWhite);

    public boolean isSelectable(Position pos, boolean whitePlayer);

    public boolean isMoveable(Position from, Position to, boolean whitePlayer);

    public boolean isFreeArea(Position pos);

    public int move(Move move);

    public void getPossibleMoves(List<Move> possibleMoves);

    public int countReachableMoves(boolean isWhite);

    //	public void useNormalSupervisor();
    public ChessGameSupervisor suspendInteractiveSupervisor();

    public void useSupervisor(ChessGameSupervisor supervisor);

    public void undo();

    /**
     * @return all the moves played so far in a string representation
     */
    public String getCompleteHistory();
}
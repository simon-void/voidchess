package voidchess.figures;

import voidchess.helper.Position;

import java.util.StringTokenizer;

/**
 * @author stephan
 */
final public class FigureFactory {
    public Figure getKing(Position pos, boolean isWhite) {
        return new King(isWhite, pos);
    }

    private Figure getKing(Position pos, boolean isWhite, int stepsTaken, boolean didRochade) {
        return new King(isWhite, pos, stepsTaken, didRochade);
    }

    public Figure getQueen(Position pos, boolean isWhite) {
        return new Queen(isWhite, pos);
    }

    public Figure getBishop(Position pos, boolean isWhite) {
        return new Bishop(isWhite, pos);
    }

    public Figure getKnight(Position pos, boolean isWhite) {
        return new Knight(isWhite, pos);
    }

    public Figure getRock(Position pos, boolean isWhite) {
        return new Rock(isWhite, pos);
    }

    private Figure getRock(Position pos, boolean isWhite, int stepsTaken) {
        return new Rock(isWhite, pos, stepsTaken);
    }

    public Figure getPawn(Position pos, boolean isWhite) {
        return new Pawn(isWhite, pos);
    }

    private Figure getPawn(Position pos, boolean isWhite, boolean canBeHitByEnpasent) {
        return new Pawn(isWhite, pos, canBeHitByEnpasent);
    }

    public Figure getFigureByString(String description) {
        StringTokenizer st = new StringTokenizer(description, "-", false);
        String type = st.nextToken();
        boolean isWhite = st.nextToken().equals("white");
        Position pos = Position.Companion.get(st.nextToken());

        if (type.equals("Knight")) return getKnight(pos, isWhite);
        if (type.equals("Bishop")) return getBishop(pos, isWhite);
        if (type.equals("Queen")) return getQueen(pos, isWhite);

        if (type.equals("Pawn")) {
            boolean readyForRochadeOrEnpasent = st.nextToken().equals("true");
            return getPawn(pos, isWhite, readyForRochadeOrEnpasent);
        }

        int stepsTaken = Integer.parseInt(st.nextToken());
        if (type.equals("Rock")) return getRock(pos, isWhite, stepsTaken);

        boolean didRochade = st.hasMoreTokens() && st.nextToken().equals("true");
        if (type.equals("King")) return getKing(pos, isWhite, stepsTaken, didRochade);

        throw new IllegalArgumentException("figure description misformated");
    }
}

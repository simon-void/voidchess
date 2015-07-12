package voidchess.helper;

/**
 * @author stephan
 */
public class Move {
    final private static Move[][][][] moves = new Move[8][8][8][8];

    static {
        for (int fromrow = 0; fromrow < 8; fromrow++) {
            for (int fromcolumn = 0; fromcolumn < 8; fromcolumn++) {
                for (int torow = 0; torow < 8; torow++) {
                    for (int tocolumn = 0; tocolumn < 8; tocolumn++) {
                        Position fromPos = Position.get(fromrow, fromcolumn);
                        Position toPos = Position.get(torow, tocolumn);
                        moves[fromrow][fromcolumn][torow][tocolumn] = new Move(fromPos, toPos);
                    }
                }
            }
        }
    }

    final public Position from;
    final public Position to;

    public static Move get(Position from, Position to) {
        return moves[from.row][from.column][to.row][to.column];
    }

    public static Move get(String code) {
        int fromcolumn = (int) code.charAt(0) - 97;
        int fromrow = (int) code.charAt(1) - 49;
        int tocolumn = (int) code.charAt(3) - 97;
        int torow = (int) code.charAt(4) - 49;

        return moves[fromrow][fromcolumn][torow][tocolumn];
    }

    protected Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(5);
        buffer.append(from.toString());
        buffer.append("-");
        buffer.append(to.toString());
        return buffer.toString();
    }

    public boolean equalsMove(Move other) {
        return toString().equals(other.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Move)) return false;
        return equalsMove((Move) obj);
    }

    @Override
    public int hashCode() {
        return to.hashCode();
    }
}

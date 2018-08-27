package voidchess.helper;

/**
 * @author stephan
 */
public class Move {
    final private static Move[] moves = new Move[64*64];

    static {
        for (int moveIndex = 0; moveIndex < moves.length; moveIndex++) {
            int fromIndex = moveIndex % 64;
            int toIndex = moveIndex / 64;
            Position fromPos = Position.Companion.byIndex(fromIndex);
            Position toPos = Position.Companion.byIndex(toIndex);
            moves[moveIndex] = new Move(fromPos, toPos);
        }
    }

    final public Position from;
    final public Position to;

    public static Move get(Position from, Position to) {
        return moves[getMoveIndex(from.getIndex(), to.getIndex())];
    }

    public static Move byCode(String code) {
        int fromColumn = (int) code.charAt(0) - 97;
        int fromRow = (int) code.charAt(1) - 49;
        int toColumn = (int) code.charAt(3) - 97;
        int toRow = (int) code.charAt(4) - 49;

        return get(
                Position.Companion.get(fromRow, fromColumn),
                Position.Companion.get(toRow, toColumn)
        );
    }

    public static boolean isValid(String code) {
        if (code == null || code.length() != 5 || code.charAt(2) != '-') {
            return false;
        }

        int fromColumn = (int) code.charAt(0) - 97;
        int fromRow = (int) code.charAt(1) - 49;
        int toColumn = (int) code.charAt(3) - 97;
        int toRow = (int) code.charAt(4) - 49;

        return liesIn07Range(fromColumn) &&
                liesIn07Range(fromRow) &&
                liesIn07Range(toColumn) &&
                liesIn07Range(toRow);
    }

    private static boolean liesIn07Range(int number) {
        return number >= 0 && number <= 7;
    }

    // optimized from: fromIndex + toIndex * 64
    private static int getMoveIndex(int fromIndex, int toIndex) {
      return fromIndex + (toIndex<<6);
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

package voidchess.helper;

/**
 * @author stephan
 */
final public class Position {
    final private static Position[] positions = new Position[64];

    static {
        for (byte row = 0; row < 8; row++) {
            for (byte column = 0; column < 8; column++) {
                Position pos = new Position(row, column);
                positions[pos.linearIndex] = pos;
            }
        }
    }

    final public int row, column, linearIndex;

    private Position(int row, int column) {
        assert !notInBounds(row, column)
                : "This is no position on a chessboard";

        this.row = row;
        this.column = column;
        this.linearIndex = row + (column << 3); // optimized from row + column * 8
    }

    public static boolean notInBounds(int row, int column) {
        return row > 7 || row < 0 || column > 7 || column < 0;
    }

    public static Position get(String code) {
        assert code.length() == 2
                : "argument is supposed to be something like \"a8\"";

        int column = (int) code.charAt(0) - 97;
        int row = (int) code.charAt(1) - 49;

        assert !notInBounds(row, column)
                : "This is no position on a chessboard";

        return get(row, column);
    }

    public static Position get(int row, int column) {
        assert !notInBounds(row, column)
                : "This is no position on a chessboard";

        return positions[row + (column << 3)];
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(2);
        buffer.append((char) (column + 97));
        buffer.append(row + 1);
        return buffer.toString();
    }

    public boolean equalsPosition(Position other) {
        return row == other.row && column == other.column;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        return equalsPosition((Position) obj);
    }

    @Override
    public int hashCode() {
        return linearIndex;
    }
}
package helper;

/**
 * @author stephan
 */
final public class Position {
    final private static Position[][] positions;

    static {
        positions = new Position[8][8];
        for (byte row = 0; row < 8; row++) {
            for (byte column = 0; column < 8; column++) {
                positions[row][column] = new Position(row, column);
            }
        }
    }

    final public int row, column;
    final private int hashcode;

    private Position(int row, int column) {
        assert !notInBounds(row, column)
                : "This is no position on a chessboard";

        this.row = row;
        this.column = column;
        this.hashcode = row + column * 8;
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

        return positions[row][column];
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
        return hashcode;
    }
}
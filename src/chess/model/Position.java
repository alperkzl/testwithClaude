package chess.model;

import java.util.Objects;

public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Position offset(int dRow, int dCol) {
        return new Position(row + dRow, col + dCol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        char file = (char) ('a' + col);
        int rank = row + 1;
        return "" + file + rank;
    }

    public static Position fromAlgebraic(String notation) {
        if (notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }
        int col = notation.charAt(0) - 'a';
        int row = notation.charAt(1) - '1';
        Position pos = new Position(row, col);
        if (!pos.isValid()) {
            throw new IllegalArgumentException("Invalid position: " + notation);
        }
        return pos;
    }
}

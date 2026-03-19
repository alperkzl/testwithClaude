package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.List;

public abstract class Piece {
    protected final Color color;
    protected Position position;

    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public abstract PieceType getType();

    /**
     * Returns candidate moves for this piece based on its movement rules.
     * These are NOT filtered for king-safety — that is done by MoveValidator.
     */
    public abstract List<Position> getCandidateMoves(Board board);

    /**
     * Returns a copy of this piece (used for promotion replacement, etc.).
     */
    public abstract Piece copy();

    /**
     * Helper: slides along a direction until blocked. Used by Queen, Rook, Bishop.
     */
    protected void addSlidingMoves(Board board, List<Position> moves, int dRow, int dCol) {
        Position current = position.offset(dRow, dCol);
        while (current.isValid()) {
            Piece occupant = board.getPieceAt(current);
            if (occupant == null) {
                moves.add(current);
            } else {
                if (occupant.getColor() != this.color) {
                    moves.add(current);
                }
                break;
            }
            current = current.offset(dRow, dCol);
        }
    }

    @Override
    public String toString() {
        return color.name().charAt(0) + "_" + getType().name();
    }
}

package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    public PieceType getType() {
        return PieceType.PAWN;
    }

    @Override
    public List<Position> getCandidateMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? 1 : -1;
        int startRow = (color == Color.WHITE) ? 1 : 6;

        // Single push
        Position oneStep = position.offset(direction, 0);
        if (oneStep.isValid() && board.getPieceAt(oneStep) == null) {
            moves.add(oneStep);

            // Double push from starting row
            if (position.getRow() == startRow) {
                Position twoStep = position.offset(2 * direction, 0);
                if (twoStep.isValid() && board.getPieceAt(twoStep) == null) {
                    moves.add(twoStep);
                }
            }
        }

        // Diagonal captures
        for (int dCol : new int[]{-1, 1}) {
            Position diagTarget = position.offset(direction, dCol);
            if (diagTarget.isValid()) {
                Piece occupant = board.getPieceAt(diagTarget);
                if (occupant != null && occupant.getColor() != this.color) {
                    moves.add(diagTarget);
                }
            }
        }

        // En passant
        Position enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget != null) {
            for (int dCol : new int[]{-1, 1}) {
                Position diagTarget = position.offset(direction, dCol);
                if (diagTarget.equals(enPassantTarget)) {
                    moves.add(diagTarget);
                }
            }
        }

        return moves;
    }

    @Override
    public Piece copy() {
        return new Pawn(color, position);
    }
}

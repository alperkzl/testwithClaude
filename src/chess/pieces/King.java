package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public PieceType getType() {
        return PieceType.KING;
    }

    @Override
    public List<Position> getCandidateMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] offsets = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
        };

        for (int[] offset : offsets) {
            Position target = position.offset(offset[0], offset[1]);
            if (target.isValid()) {
                Piece occupant = board.getPieceAt(target);
                if (occupant == null || occupant.getColor() != this.color) {
                    moves.add(target);
                }
            }
        }

        // Castling candidates are added here as raw candidates.
        // MoveValidator will verify legality (not through check, etc.)
        int baseRow = (color == Color.WHITE) ? 0 : 7;
        if (position.getRow() == baseRow && position.getCol() == 4) {
            // Kingside
            if (board.canCastleKingside(color)) {
                Position kingSideDest = new Position(baseRow, 6);
                moves.add(kingSideDest);
            }
            // Queenside
            if (board.canCastleQueenside(color)) {
                Position queenSideDest = new Position(baseRow, 2);
                moves.add(queenSideDest);
            }
        }

        return moves;
    }

    @Override
    public Piece copy() {
        return new King(color, position);
    }
}

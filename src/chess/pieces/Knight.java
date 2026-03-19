package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    public PieceType getType() {
        return PieceType.KNIGHT;
    }

    @Override
    public List<Position> getCandidateMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] offsets = {
            {-2, -1}, {-2, 1},
            {-1, -2}, {-1, 2},
            { 1, -2}, { 1, 2},
            { 2, -1}, { 2, 1}
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
        return moves;
    }

    @Override
    public Piece copy() {
        return new Knight(color, position);
    }
}

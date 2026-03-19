package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    public PieceType getType() {
        return PieceType.QUEEN;
    }

    @Override
    public List<Position> getCandidateMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        // All 8 directions
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
        };
        for (int[] dir : directions) {
            addSlidingMoves(board, moves, dir[0], dir[1]);
        }
        return moves;
    }

    @Override
    public Piece copy() {
        return new Queen(color, position);
    }
}

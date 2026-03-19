package chess.pieces;

import chess.model.Board;
import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Color color, Position position) {
        super(color, position);
    }

    @Override
    public PieceType getType() {
        return PieceType.ROOK;
    }

    @Override
    public List<Position> getCandidateMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            addSlidingMoves(board, moves, dir[0], dir[1]);
        }
        return moves;
    }

    @Override
    public Piece copy() {
        return new Rook(color, position);
    }
}

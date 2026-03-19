package chess.engine;

import chess.model.Board;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Game;
import chess.model.PieceType;
import chess.model.Position;
import chess.pieces.Piece;

import java.util.List;

/**
 * Static position evaluator. Returns a score from White's perspective
 * (positive = White is better, negative = Black is better).
 */
public class BoardEvaluator {

    // Material values in centipawns
    private static final int PAWN_VALUE   = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE   = 500;
    private static final int QUEEN_VALUE  = 900;
    private static final int KING_VALUE   = 20000;

    // Piece-square tables (from White's perspective, rank 0 = row 0 = rank 1)
    // Values are centipawn bonuses for placing a piece on that square.

    private static final int[][] PAWN_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0},  // rank 1 (starting row, shouldn't have pawns)
        {  5, 10, 10,-20,-20, 10, 10,  5},  // rank 2
        {  5, -5,-10,  0,  0,-10, -5,  5},  // rank 3
        {  0,  0,  0, 20, 20,  0,  0,  0},  // rank 4
        {  5,  5, 10, 25, 25, 10,  5,  5},  // rank 5
        { 10, 10, 20, 30, 30, 20, 10, 10},  // rank 6
        { 50, 50, 50, 50, 50, 50, 50, 50},  // rank 7
        {  0,  0,  0,  0,  0,  0,  0,  0},  // rank 8 (promotion row)
    };

    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50},
    };

    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20},
    };

    private static final int[][] ROOK_TABLE = {
        {  0,  0,  0,  5,  5,  0,  0,  0},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        {  5, 10, 10, 10, 10, 10, 10,  5},
        {  0,  0,  0,  0,  0,  0,  0,  0},
    };

    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {  0,  0,  5,  5,  5,  5,  0, -5},
        { -5,  0,  5,  5,  5,  5,  0, -5},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20},
    };

    private static final int[][] KING_MIDDLEGAME_TABLE = {
        { 20, 30, 10,  0,  0, 10, 30, 20},
        { 20, 20,  0,  0,  0,  0, 20, 20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
    };

    public static final int CHECKMATE_SCORE = 100000;

    /**
     * Evaluate the game state. Returns score in centipawns from White's perspective.
     */
    public int evaluate(Game game) {
        GameState state = game.getGameState();

        // Terminal states
        if (state == GameState.CHECKMATE) {
            // Current turn is in checkmate — the other side wins
            return game.getCurrentTurn() == Color.WHITE ? -CHECKMATE_SCORE : CHECKMATE_SCORE;
        }
        if (state == GameState.STALEMATE || state == GameState.DRAW_FIFTY_MOVE
                || state == GameState.DRAW_INSUFFICIENT_MATERIAL) {
            return 0;
        }

        Board board = game.getBoard();
        int score = 0;

        // Evaluate each piece
        List<Piece> whitePieces = board.getPieces(Color.WHITE);
        List<Piece> blackPieces = board.getPieces(Color.BLACK);

        for (Piece piece : whitePieces) {
            score += materialValue(piece.getType());
            score += positionalBonus(piece.getType(), piece.getPosition(), Color.WHITE);
        }

        for (Piece piece : blackPieces) {
            score -= materialValue(piece.getType());
            score -= positionalBonus(piece.getType(), piece.getPosition(), Color.BLACK);
        }

        return score;
    }

    private int materialValue(PieceType type) {
        switch (type) {
            case PAWN:   return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK:   return ROOK_VALUE;
            case QUEEN:  return QUEEN_VALUE;
            case KING:   return KING_VALUE;
            default:     return 0;
        }
    }

    private int positionalBonus(PieceType type, Position pos, Color color) {
        int row = (color == Color.WHITE) ? pos.getRow() : (7 - pos.getRow());
        int col = pos.getCol();

        switch (type) {
            case PAWN:   return PAWN_TABLE[row][col];
            case KNIGHT: return KNIGHT_TABLE[row][col];
            case BISHOP: return BISHOP_TABLE[row][col];
            case ROOK:   return ROOK_TABLE[row][col];
            case QUEEN:  return QUEEN_TABLE[row][col];
            case KING:   return KING_MIDDLEGAME_TABLE[row][col];
            default:     return 0;
        }
    }
}

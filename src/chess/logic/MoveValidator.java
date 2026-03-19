package chess.logic;

import chess.model.*;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates moves by filtering candidate moves for king-safety,
 * and handles special move validation (castling through check, etc.).
 */
public class MoveValidator {

    private final Board board;

    public MoveValidator(Board board) {
        this.board = board;
    }

    /**
     * Returns all legal target positions for the piece at the given position.
     */
    public List<Position> getLegalMoves(Position from) {
        Piece piece = board.getPieceAt(from);
        if (piece == null) {
            return new ArrayList<>();
        }

        List<Position> candidates = piece.getCandidateMoves(board);
        List<Position> legalMoves = new ArrayList<>();

        for (Position to : candidates) {
            if (isMoveLegal(piece, from, to)) {
                legalMoves.add(to);
            }
        }

        return legalMoves;
    }

    /**
     * Checks whether moving piece from -> to is legal (doesn't leave own king in check).
     * Also validates special castling rules.
     */
    private boolean isMoveLegal(Piece piece, Position from, Position to) {
        Color color = piece.getColor();

        // Special castling validation
        if (piece instanceof King) {
            int colDiff = to.getCol() - from.getCol();

            // Kingside castle
            if (colDiff == 2 && to.getRow() == from.getRow()) {
                return isCastleLegal(color, true);
            }
            // Queenside castle
            if (colDiff == -2 && to.getRow() == from.getRow()) {
                return isCastleLegal(color, false);
            }
        }

        // Simulate the move and check if own king is in check
        return !leavesKingInCheck(piece, from, to);
    }

    /**
     * Validates castling: king must not be in check, must not pass through
     * or land on an attacked square.
     */
    private boolean isCastleLegal(Color color, boolean kingside) {
        // Can't castle while in check
        if (board.isKingInCheck(color)) {
            return false;
        }

        int row = (color == Color.WHITE) ? 0 : 7;
        int kingCol = 4;

        if (kingside) {
            // King passes through f-file and lands on g-file
            Position f = new Position(row, 5);
            Position g = new Position(row, 6);
            return !board.isSquareAttackedBy(f, color.opposite())
                && !board.isSquareAttackedBy(g, color.opposite());
        } else {
            // King passes through d-file and lands on c-file
            Position d = new Position(row, 3);
            Position c = new Position(row, 2);
            return !board.isSquareAttackedBy(d, color.opposite())
                && !board.isSquareAttackedBy(c, color.opposite());
        }
    }

    /**
     * Simulates a move and checks if the moving side's king is left in check.
     * Handles en passant capture specially.
     */
    private boolean leavesKingInCheck(Piece piece, Position from, Position to) {
        Color color = piece.getColor();

        // Save state
        Piece capturedPiece = board.getPieceAt(to);
        Piece enPassantCaptured = null;
        Position enPassantTarget = board.getEnPassantTarget();

        // Handle en passant capture
        boolean isEnPassant = piece instanceof Pawn
                && to.equals(enPassantTarget)
                && capturedPiece == null;

        Position enPassantCapturePos = null;
        if (isEnPassant) {
            int capturedRow = from.getRow();
            enPassantCapturePos = new Position(capturedRow, to.getCol());
            enPassantCaptured = board.removePiece(enPassantCapturePos);
        }

        // Execute temporary move
        board.removePiece(from);
        piece.setPosition(to);
        board.placePiece(piece);

        // Check if own king is now in check
        boolean inCheck = board.isKingInCheck(color);

        // Undo temporary move
        board.removePiece(to);
        piece.setPosition(from);
        board.placePiece(piece);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece);
        }
        if (enPassantCaptured != null) {
            board.placePiece(enPassantCaptured);
        }

        return inCheck;
    }

    /**
     * Returns true if the given color has any legal moves.
     */
    public boolean hasAnyLegalMoves(Color color) {
        for (Piece piece : board.getPieces(color)) {
            if (!getLegalMoves(piece.getPosition()).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for insufficient material draw.
     * K vs K, K+B vs K, K+N vs K, K+B vs K+B (same color bishops).
     */
    public boolean isInsufficientMaterial() {
        List<Piece> whitePieces = board.getPieces(Color.WHITE);
        List<Piece> blackPieces = board.getPieces(Color.BLACK);

        // Strip kings
        List<Piece> whiteNonKing = new ArrayList<>();
        List<Piece> blackNonKing = new ArrayList<>();
        for (Piece p : whitePieces) {
            if (p.getType() != PieceType.KING) whiteNonKing.add(p);
        }
        for (Piece p : blackPieces) {
            if (p.getType() != PieceType.KING) blackNonKing.add(p);
        }

        int wCount = whiteNonKing.size();
        int bCount = blackNonKing.size();

        // K vs K
        if (wCount == 0 && bCount == 0) return true;

        // K+B vs K or K+N vs K
        if (wCount == 0 && bCount == 1) {
            PieceType type = blackNonKing.get(0).getType();
            return type == PieceType.BISHOP || type == PieceType.KNIGHT;
        }
        if (bCount == 0 && wCount == 1) {
            PieceType type = whiteNonKing.get(0).getType();
            return type == PieceType.BISHOP || type == PieceType.KNIGHT;
        }

        // K+B vs K+B (same colored square bishops)
        if (wCount == 1 && bCount == 1) {
            Piece wp = whiteNonKing.get(0);
            Piece bp = blackNonKing.get(0);
            if (wp.getType() == PieceType.BISHOP && bp.getType() == PieceType.BISHOP) {
                boolean wLight = (wp.getPosition().getRow() + wp.getPosition().getCol()) % 2 == 0;
                boolean bLight = (bp.getPosition().getRow() + bp.getPosition().getCol()) % 2 == 0;
                return wLight == bLight;
            }
        }

        return false;
    }
}

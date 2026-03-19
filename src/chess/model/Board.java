package chess.model;

import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] grid;

    // Castling rights
    private boolean whiteCanCastleKingside = true;
    private boolean whiteCanCastleQueenside = true;
    private boolean blackCanCastleKingside = true;
    private boolean blackCanCastleQueenside = true;

    // En passant target square (the square a pawn skipped over)
    private Position enPassantTarget;

    public Board() {
        grid = new Piece[8][8];
    }

    /**
     * Sets up the standard starting position.
     */
    public void setupInitialPosition() {
        // White pieces (row 0 = rank 1)
        placePiece(new Rook(Color.WHITE, new Position(0, 0)));
        placePiece(new Knight(Color.WHITE, new Position(0, 1)));
        placePiece(new Bishop(Color.WHITE, new Position(0, 2)));
        placePiece(new Queen(Color.WHITE, new Position(0, 3)));
        placePiece(new King(Color.WHITE, new Position(0, 4)));
        placePiece(new Bishop(Color.WHITE, new Position(0, 5)));
        placePiece(new Knight(Color.WHITE, new Position(0, 6)));
        placePiece(new Rook(Color.WHITE, new Position(0, 7)));
        for (int col = 0; col < 8; col++) {
            placePiece(new Pawn(Color.WHITE, new Position(1, col)));
        }

        // Black pieces (row 7 = rank 8)
        placePiece(new Rook(Color.BLACK, new Position(7, 0)));
        placePiece(new Knight(Color.BLACK, new Position(7, 1)));
        placePiece(new Bishop(Color.BLACK, new Position(7, 2)));
        placePiece(new Queen(Color.BLACK, new Position(7, 3)));
        placePiece(new King(Color.BLACK, new Position(7, 4)));
        placePiece(new Bishop(Color.BLACK, new Position(7, 5)));
        placePiece(new Knight(Color.BLACK, new Position(7, 6)));
        placePiece(new Rook(Color.BLACK, new Position(7, 7)));
        for (int col = 0; col < 8; col++) {
            placePiece(new Pawn(Color.BLACK, new Position(6, col)));
        }

        whiteCanCastleKingside = true;
        whiteCanCastleQueenside = true;
        blackCanCastleKingside = true;
        blackCanCastleQueenside = true;
        enPassantTarget = null;
    }

    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) return null;
        return grid[pos.getRow()][pos.getCol()];
    }

    public void placePiece(Piece piece) {
        Position pos = piece.getPosition();
        grid[pos.getRow()][pos.getCol()] = piece;
    }

    public Piece removePiece(Position pos) {
        Piece piece = grid[pos.getRow()][pos.getCol()];
        grid[pos.getRow()][pos.getCol()] = null;
        return piece;
    }

    public void movePiece(Position from, Position to) {
        Piece piece = removePiece(from);
        if (piece != null) {
            piece.setPosition(to);
            placePiece(piece);
        }
    }

    /**
     * Returns all pieces of the given color currently on the board.
     */
    public List<Piece> getPieces(Color color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    /**
     * Finds the king of the given color.
     */
    public King findKing(Color color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece instanceof King && piece.getColor() == color) {
                    return (King) piece;
                }
            }
        }
        throw new IllegalStateException("No king found for " + color);
    }

    /**
     * Checks if a square is attacked by any piece of the given color.
     */
    public boolean isSquareAttackedBy(Position square, Color attackerColor) {
        for (Piece piece : getPieces(attackerColor)) {
            // For king, check direct adjacency to avoid infinite recursion
            if (piece instanceof King) {
                int rowDiff = Math.abs(piece.getPosition().getRow() - square.getRow());
                int colDiff = Math.abs(piece.getPosition().getCol() - square.getCol());
                if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0)) {
                    return true;
                }
            } else {
                List<Position> attacks = piece.getCandidateMoves(this);
                if (attacks.contains(square)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isKingInCheck(Color color) {
        King king = findKing(color);
        return isSquareAttackedBy(king.getPosition(), color.opposite());
    }

    // Castling rights getters and setters

    public boolean canCastleKingside(Color color) {
        if (color == Color.WHITE) {
            if (!whiteCanCastleKingside) return false;
            // Squares between king and rook must be empty
            return getPieceAt(new Position(0, 5)) == null
                && getPieceAt(new Position(0, 6)) == null;
        } else {
            if (!blackCanCastleKingside) return false;
            return getPieceAt(new Position(7, 5)) == null
                && getPieceAt(new Position(7, 6)) == null;
        }
    }

    public boolean canCastleQueenside(Color color) {
        if (color == Color.WHITE) {
            if (!whiteCanCastleQueenside) return false;
            return getPieceAt(new Position(0, 1)) == null
                && getPieceAt(new Position(0, 2)) == null
                && getPieceAt(new Position(0, 3)) == null;
        } else {
            if (!blackCanCastleQueenside) return false;
            return getPieceAt(new Position(7, 1)) == null
                && getPieceAt(new Position(7, 2)) == null
                && getPieceAt(new Position(7, 3)) == null;
        }
    }

    public boolean getCastleKingside(Color color) {
        return color == Color.WHITE ? whiteCanCastleKingside : blackCanCastleKingside;
    }

    public boolean getCastleQueenside(Color color) {
        return color == Color.WHITE ? whiteCanCastleQueenside : blackCanCastleQueenside;
    }

    public void setCastleKingside(Color color, boolean value) {
        if (color == Color.WHITE) whiteCanCastleKingside = value;
        else blackCanCastleKingside = value;
    }

    public void setCastleQueenside(Color color, boolean value) {
        if (color == Color.WHITE) whiteCanCastleQueenside = value;
        else blackCanCastleQueenside = value;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(Position target) {
        this.enPassantTarget = target;
    }
}

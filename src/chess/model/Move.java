package chess.model;

import chess.pieces.Piece;

public class Move {
    private final Position from;
    private final Position to;
    private final Piece pieceMoved;
    private final Piece pieceCaptured;
    private final MoveType moveType;
    private final PieceType promotionPiece;

    // State needed for undo
    private final boolean couldCastleKingsideBefore;
    private final boolean couldCastleQueensideBefore;
    private final Position enPassantTargetBefore;
    private int halfMoveClockBefore;

    public Move(Position from, Position to, Piece pieceMoved, Piece pieceCaptured,
                MoveType moveType, PieceType promotionPiece,
                boolean couldCastleKingside, boolean couldCastleQueenside,
                Position enPassantTarget) {
        this.from = from;
        this.to = to;
        this.pieceMoved = pieceMoved;
        this.pieceCaptured = pieceCaptured;
        this.moveType = moveType;
        this.promotionPiece = promotionPiece;
        this.couldCastleKingsideBefore = couldCastleKingside;
        this.couldCastleQueensideBefore = couldCastleQueenside;
        this.enPassantTargetBefore = enPassantTarget;
    }

    public static Move normal(Position from, Position to, Piece moved, Piece captured,
                              boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, moved, captured, MoveType.NORMAL, null,
                castleK, castleQ, enPassant);
    }

    public static Move pawnDoubleMove(Position from, Position to, Piece moved,
                                      boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, moved, null, MoveType.PAWN_DOUBLE_MOVE, null,
                castleK, castleQ, enPassant);
    }

    public static Move enPassant(Position from, Position to, Piece moved, Piece captured,
                                 boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, moved, captured, MoveType.EN_PASSANT, null,
                castleK, castleQ, enPassant);
    }

    public static Move castleKingside(Position from, Position to, Piece king,
                                      boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, king, null, MoveType.CASTLE_KINGSIDE, null,
                castleK, castleQ, enPassant);
    }

    public static Move castleQueenside(Position from, Position to, Piece king,
                                       boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, king, null, MoveType.CASTLE_QUEENSIDE, null,
                castleK, castleQ, enPassant);
    }

    public static Move promotion(Position from, Position to, Piece moved, Piece captured,
                                 PieceType promoPiece,
                                 boolean castleK, boolean castleQ, Position enPassant) {
        return new Move(from, to, moved, captured, MoveType.PROMOTION, promoPiece,
                castleK, castleQ, enPassant);
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPieceMoved() { return pieceMoved; }
    public Piece getPieceCaptured() { return pieceCaptured; }
    public MoveType getMoveType() { return moveType; }
    public PieceType getPromotionPiece() { return promotionPiece; }
    public boolean couldCastleKingsideBefore() { return couldCastleKingsideBefore; }
    public boolean couldCastleQueensideBefore() { return couldCastleQueensideBefore; }
    public Position getEnPassantTargetBefore() { return enPassantTargetBefore; }
    public int getHalfMoveClockBefore() { return halfMoveClockBefore; }
    public void setHalfMoveClockBefore(int clock) { this.halfMoveClockBefore = clock; }

    @Override
    public String toString() {
        String base = pieceMoved.getType().name() + " " + from + " -> " + to;
        if (moveType != MoveType.NORMAL) {
            base += " (" + moveType + ")";
        }
        if (pieceCaptured != null) {
            base += " x" + pieceCaptured.getType().name();
        }
        return base;
    }
}

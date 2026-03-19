package chess.model;

import chess.logic.MoveValidator;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game orchestrator. Acts as the facade for the GUI layer.
 */
public class Game {

    private final Board board;
    private final MoveValidator moveValidator;
    private final List<Move> moveHistory;
    private Color currentTurn;
    private GameState gameState;
    private int halfMoveClock; // for 50-move rule (counts half-moves without capture or pawn move)

    public Game() {
        board = new Board();
        board.setupInitialPosition();
        moveValidator = new MoveValidator(board);
        moveHistory = new ArrayList<>();
        currentTurn = Color.WHITE;
        gameState = GameState.IN_PROGRESS;
        halfMoveClock = 0;
    }

    /**
     * Returns legal target positions for the piece at the given position.
     * Returns empty list if it's not the current player's piece.
     */
    public List<Position> getLegalMoves(Position from) {
        Piece piece = board.getPieceAt(from);
        if (piece == null || piece.getColor() != currentTurn) {
            return new ArrayList<>();
        }
        return moveValidator.getLegalMoves(from);
    }

    /**
     * Attempts to make a move from -> to.
     * For promotions, promotionChoice must be specified.
     * Returns the executed Move if successful, null if illegal.
     */
    public Move makeMove(Position from, Position to, PieceType promotionChoice) {
        if (gameState == GameState.CHECKMATE || gameState == GameState.STALEMATE
                || gameState == GameState.DRAW_FIFTY_MOVE
                || gameState == GameState.DRAW_INSUFFICIENT_MATERIAL) {
            return null; // game is over
        }

        Piece piece = board.getPieceAt(from);
        if (piece == null || piece.getColor() != currentTurn) {
            return null;
        }

        List<Position> legalMoves = moveValidator.getLegalMoves(from);
        if (!legalMoves.contains(to)) {
            return null;
        }

        // Build the move object
        Move move = buildMove(piece, from, to, promotionChoice);

        // Save clock state for undo, then execute
        move.setHalfMoveClockBefore(halfMoveClock);
        executeMove(move);

        // Update game state
        moveHistory.add(move);
        currentTurn = currentTurn.opposite();
        updateGameState();

        return move;
    }

    /**
     * Convenience overload for non-promotion moves.
     */
    public Move makeMove(Position from, Position to) {
        return makeMove(from, to, null);
    }

    /**
     * Undoes the last move.
     */
    public Move undoMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        reverseMove(lastMove);
        halfMoveClock = lastMove.getHalfMoveClockBefore();
        currentTurn = currentTurn.opposite();
        updateGameState();

        return lastMove;
    }

    private Move buildMove(Piece piece, Position from, Position to, PieceType promotionChoice) {
        boolean castleK = board.getCastleKingside(currentTurn);
        boolean castleQ = board.getCastleQueenside(currentTurn);
        Position epTarget = board.getEnPassantTarget();

        // Determine move type
        if (piece instanceof King) {
            int colDiff = to.getCol() - from.getCol();
            if (colDiff == 2) {
                return Move.castleKingside(from, to, piece, castleK, castleQ, epTarget);
            }
            if (colDiff == -2) {
                return Move.castleQueenside(from, to, piece, castleK, castleQ, epTarget);
            }
        }

        if (piece instanceof Pawn) {
            int rowDiff = Math.abs(to.getRow() - from.getRow());

            // En passant
            if (to.equals(epTarget)) {
                int capturedRow = from.getRow();
                Position capturedPos = new Position(capturedRow, to.getCol());
                Piece captured = board.getPieceAt(capturedPos);
                return Move.enPassant(from, to, piece, captured, castleK, castleQ, epTarget);
            }

            // Double move
            if (rowDiff == 2) {
                return Move.pawnDoubleMove(from, to, piece, castleK, castleQ, epTarget);
            }

            // Promotion
            int promoRow = (piece.getColor() == Color.WHITE) ? 7 : 0;
            if (to.getRow() == promoRow) {
                PieceType promo = (promotionChoice != null) ? promotionChoice : PieceType.QUEEN;
                Piece captured = board.getPieceAt(to);
                return Move.promotion(from, to, piece, captured, promo, castleK, castleQ, epTarget);
            }
        }

        // Normal move
        Piece captured = board.getPieceAt(to);
        return Move.normal(from, to, piece, captured, castleK, castleQ, epTarget);
    }

    private void executeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPieceMoved();

        switch (move.getMoveType()) {
            case CASTLE_KINGSIDE: {
                int row = from.getRow();
                board.movePiece(from, to);
                board.movePiece(new Position(row, 7), new Position(row, 5));
                board.setCastleKingside(piece.getColor(), false);
                board.setCastleQueenside(piece.getColor(), false);
                board.setEnPassantTarget(null);
                break;
            }
            case CASTLE_QUEENSIDE: {
                int row = from.getRow();
                board.movePiece(from, to);
                board.movePiece(new Position(row, 0), new Position(row, 3));
                board.setCastleKingside(piece.getColor(), false);
                board.setCastleQueenside(piece.getColor(), false);
                board.setEnPassantTarget(null);
                break;
            }
            case EN_PASSANT: {
                int capturedRow = from.getRow();
                board.removePiece(new Position(capturedRow, to.getCol()));
                board.movePiece(from, to);
                board.setEnPassantTarget(null);
                halfMoveClock = 0;
                break;
            }
            case PAWN_DOUBLE_MOVE: {
                board.movePiece(from, to);
                int epRow = (from.getRow() + to.getRow()) / 2;
                board.setEnPassantTarget(new Position(epRow, from.getCol()));
                halfMoveClock = 0;
                break;
            }
            case PROMOTION: {
                if (move.getPieceCaptured() != null) {
                    board.removePiece(to);
                }
                board.removePiece(from);
                Piece promoted = createPromotedPiece(move.getPromotionPiece(), piece.getColor(), to);
                board.placePiece(promoted);
                board.setEnPassantTarget(null);
                halfMoveClock = 0;
                break;
            }
            case NORMAL:
            default: {
                boolean isCapture = move.getPieceCaptured() != null;
                boolean isPawn = piece instanceof Pawn;

                if (isCapture) {
                    board.removePiece(to);
                }
                board.movePiece(from, to);
                board.setEnPassantTarget(null);

                if (isCapture || isPawn) {
                    halfMoveClock = 0;
                } else {
                    halfMoveClock++;
                }
                break;
            }
        }

        // Update castling rights based on what moved or was captured
        updateCastlingRights(move);
    }

    private void updateCastlingRights(Move move) {
        Piece piece = move.getPieceMoved();
        Position from = move.getFrom();

        // King moved -> lose both castling rights
        if (piece instanceof King) {
            board.setCastleKingside(piece.getColor(), false);
            board.setCastleQueenside(piece.getColor(), false);
        }

        // Rook moved -> lose that side's castling right
        if (piece instanceof Rook) {
            int baseRow = (piece.getColor() == Color.WHITE) ? 0 : 7;
            if (from.getRow() == baseRow) {
                if (from.getCol() == 0) board.setCastleQueenside(piece.getColor(), false);
                if (from.getCol() == 7) board.setCastleKingside(piece.getColor(), false);
            }
        }

        // Rook captured -> lose opponent's castling right for that rook
        Piece captured = move.getPieceCaptured();
        if (captured instanceof Rook) {
            Color capturedColor = captured.getColor();
            int baseRow = (capturedColor == Color.WHITE) ? 0 : 7;
            // After the move, the captured rook's position is where it was
            // For en passant the captured piece position is special, but rooks can't be en-passant captured
            if (move.getMoveType() == MoveType.EN_PASSANT) return;
            Position pos = move.getTo(); // rook was at the target square
            if (pos.getRow() == baseRow) {
                if (pos.getCol() == 0) board.setCastleQueenside(capturedColor, false);
                if (pos.getCol() == 7) board.setCastleKingside(capturedColor, false);
            }
        }
    }

    private void reverseMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPieceMoved();

        // Restore castling rights
        board.setCastleKingside(piece.getColor(), move.couldCastleKingsideBefore());
        board.setCastleQueenside(piece.getColor(), move.couldCastleQueensideBefore());
        board.setEnPassantTarget(move.getEnPassantTargetBefore());

        switch (move.getMoveType()) {
            case CASTLE_KINGSIDE: {
                int row = from.getRow();
                board.movePiece(to, from); // king back
                board.movePiece(new Position(row, 5), new Position(row, 7)); // rook back
                break;
            }
            case CASTLE_QUEENSIDE: {
                int row = from.getRow();
                board.movePiece(to, from);
                board.movePiece(new Position(row, 3), new Position(row, 0));
                break;
            }
            case EN_PASSANT: {
                board.movePiece(to, from);
                // Restore captured pawn
                Piece captured = move.getPieceCaptured();
                int capturedRow = from.getRow();
                captured.setPosition(new Position(capturedRow, to.getCol()));
                board.placePiece(captured);
                break;
            }
            case PROMOTION: {
                board.removePiece(to);
                piece.setPosition(from);
                board.placePiece(piece); // restore original pawn
                if (move.getPieceCaptured() != null) {
                    Piece captured = move.getPieceCaptured();
                    captured.setPosition(to);
                    board.placePiece(captured);
                }
                break;
            }
            case PAWN_DOUBLE_MOVE:
            case NORMAL:
            default: {
                board.movePiece(to, from);
                if (move.getPieceCaptured() != null) {
                    Piece captured = move.getPieceCaptured();
                    captured.setPosition(to);
                    board.placePiece(captured);
                }
                break;
            }
        }
    }

    private Piece createPromotedPiece(PieceType type, Color color, Position pos) {
        switch (type) {
            case QUEEN:  return new Queen(color, pos);
            case ROOK:   return new Rook(color, pos);
            case BISHOP: return new Bishop(color, pos);
            case KNIGHT: return new Knight(color, pos);
            default: return new Queen(color, pos);
        }
    }

    private void updateGameState() {
        boolean inCheck = board.isKingInCheck(currentTurn);
        boolean hasLegalMoves = moveValidator.hasAnyLegalMoves(currentTurn);

        if (!hasLegalMoves) {
            gameState = inCheck ? GameState.CHECKMATE : GameState.STALEMATE;
        } else if (inCheck) {
            gameState = GameState.CHECK;
        } else if (halfMoveClock >= 100) {
            gameState = GameState.DRAW_FIFTY_MOVE;
        } else if (moveValidator.isInsufficientMaterial()) {
            gameState = GameState.DRAW_INSUFFICIENT_MATERIAL;
        } else {
            gameState = GameState.IN_PROGRESS;
        }
    }

    // Public getters for the GUI

    public Board getBoard() { return board; }
    public Color getCurrentTurn() { return currentTurn; }
    public GameState getGameState() { return gameState; }
    public List<Move> getMoveHistory() { return new ArrayList<>(moveHistory); }

    public boolean isGameOver() {
        return gameState == GameState.CHECKMATE
            || gameState == GameState.STALEMATE
            || gameState == GameState.DRAW_FIFTY_MOVE
            || gameState == GameState.DRAW_INSUFFICIENT_MATERIAL;
    }

    /**
     * Checks if a move from -> to would be a promotion (for the GUI to ask the user).
     */
    public boolean isPromotionMove(Position from, Position to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null || !(piece instanceof Pawn)) return false;
        int promoRow = (piece.getColor() == Color.WHITE) ? 7 : 0;
        return to.getRow() == promoRow;
    }
}

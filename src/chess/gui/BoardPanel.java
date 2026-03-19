package chess.gui;

import chess.model.Game;
import chess.model.GameState;
import chess.model.Move;
import chess.model.PieceType;
import chess.model.Position;
import chess.pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the 8x8 chess board, handles piece selection and move input.
 */
public class BoardPanel extends JPanel {

    private static final int SQUARE_SIZE = 80;
    private static final int BOARD_SIZE = SQUARE_SIZE * 8;

    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED_COLOR = new java.awt.Color(130, 170, 100, 180);
    private static final java.awt.Color LEGAL_MOVE_COLOR = new java.awt.Color(100, 100, 100, 80);
    private static final java.awt.Color LAST_MOVE_FROM = new java.awt.Color(205, 210, 106, 150);
    private static final java.awt.Color LAST_MOVE_TO = new java.awt.Color(170, 185, 80, 150);
    private static final java.awt.Color CHECK_COLOR = new java.awt.Color(235, 50, 50, 160);

    private static final Map<String, String> PIECE_UNICODE = new HashMap<>();

    static {
        PIECE_UNICODE.put("W_KING",   "\u2654");
        PIECE_UNICODE.put("W_QUEEN",  "\u2655");
        PIECE_UNICODE.put("W_ROOK",   "\u2656");
        PIECE_UNICODE.put("W_BISHOP", "\u2657");
        PIECE_UNICODE.put("W_KNIGHT", "\u2658");
        PIECE_UNICODE.put("W_PAWN",   "\u2659");
        PIECE_UNICODE.put("B_KING",   "\u265A");
        PIECE_UNICODE.put("B_QUEEN",  "\u265B");
        PIECE_UNICODE.put("B_ROOK",   "\u265C");
        PIECE_UNICODE.put("B_BISHOP", "\u265D");
        PIECE_UNICODE.put("B_KNIGHT", "\u265E");
        PIECE_UNICODE.put("B_PAWN",   "\u265F");
    }

    private final Game game;
    private final GameEventListener listener;

    private Position selectedSquare;
    private List<Position> legalMoves = new ArrayList<>();
    private boolean boardFlipped = false;
    private boolean inputEnabled = true;

    public BoardPanel(Game game, GameEventListener listener) {
        this.game = game;
        this.listener = listener;

        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    private void handleClick(int x, int y) {
        if (!inputEnabled) return;

        Position clicked = toPosition(x, y);
        if (clicked == null || !clicked.isValid()) return;

        if (game.isGameOver()) return;

        if (selectedSquare == null) {
            // Select a piece
            Piece piece = game.getBoard().getPieceAt(clicked);
            if (piece != null && piece.getColor() == game.getCurrentTurn()) {
                selectedSquare = clicked;
                legalMoves = game.getLegalMoves(clicked);
                repaint();
            }
        } else {
            if (legalMoves.contains(clicked)) {
                // Make the move
                if (game.isPromotionMove(selectedSquare, clicked)) {
                    PieceType promoChoice = PromotionDialog.show(this, game.getCurrentTurn());
                    game.makeMove(selectedSquare, clicked, promoChoice);
                } else {
                    game.makeMove(selectedSquare, clicked);
                }
                clearSelection();
                listener.onMoveMade();
            } else {
                // Clicked on own piece -> reselect; otherwise clear
                Piece piece = game.getBoard().getPieceAt(clicked);
                if (piece != null && piece.getColor() == game.getCurrentTurn()) {
                    selectedSquare = clicked;
                    legalMoves = game.getLegalMoves(clicked);
                } else {
                    clearSelection();
                }
                repaint();
            }
        }
    }

    private void clearSelection() {
        selectedSquare = null;
        legalMoves = new ArrayList<>();
        repaint();
    }

    public void resetSelection() {
        clearSelection();
    }

    public void setFlipped(boolean flipped) {
        this.boardFlipped = flipped;
        repaint();
    }

    public boolean isFlipped() {
        return boardFlipped;
    }

    /**
     * Converts pixel coordinates to a board Position.
     */
    private Position toPosition(int x, int y) {
        int col = x / SQUARE_SIZE;
        int row = 7 - (y / SQUARE_SIZE);
        if (boardFlipped) {
            col = 7 - col;
            row = 7 - row;
        }
        return new Position(row, col);
    }

    /**
     * Converts a board Position to pixel x coordinate.
     */
    private int toPixelX(Position pos) {
        int col = boardFlipped ? (7 - pos.getCol()) : pos.getCol();
        return col * SQUARE_SIZE;
    }

    /**
     * Converts a board Position to pixel y coordinate.
     */
    private int toPixelY(Position pos) {
        int row = boardFlipped ? pos.getRow() : (7 - pos.getRow());
        return row * SQUARE_SIZE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2);
        drawLastMoveHighlight(g2);
        drawCheckHighlight(g2);
        drawSelectionHighlight(g2);
        drawLegalMoveIndicators(g2);
        drawPieces(g2);
        drawCoordinates(g2);
    }

    private void drawBoard(Graphics2D g) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                boolean isLight = (row + col) % 2 == 0;
                g.setColor(isLight ? DARK_SQUARE : LIGHT_SQUARE);
                g.fillRect(toPixelX(pos), toPixelY(pos), SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawLastMoveHighlight(Graphics2D g) {
        List<Move> history = game.getMoveHistory();
        if (!history.isEmpty()) {
            Move lastMove = history.get(history.size() - 1);
            g.setColor(LAST_MOVE_FROM);
            g.fillRect(toPixelX(lastMove.getFrom()), toPixelY(lastMove.getFrom()),
                    SQUARE_SIZE, SQUARE_SIZE);
            g.setColor(LAST_MOVE_TO);
            g.fillRect(toPixelX(lastMove.getTo()), toPixelY(lastMove.getTo()),
                    SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    private void drawCheckHighlight(Graphics2D g) {
        if (game.getGameState() == GameState.CHECK || game.getGameState() == GameState.CHECKMATE) {
            Position kingPos = game.getBoard().findKing(game.getCurrentTurn()).getPosition();
            g.setColor(CHECK_COLOR);
            g.fillRect(toPixelX(kingPos), toPixelY(kingPos), SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    private void drawSelectionHighlight(Graphics2D g) {
        if (selectedSquare != null) {
            g.setColor(SELECTED_COLOR);
            g.fillRect(toPixelX(selectedSquare), toPixelY(selectedSquare),
                    SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    private void drawLegalMoveIndicators(Graphics2D g) {
        g.setColor(LEGAL_MOVE_COLOR);
        for (Position pos : legalMoves) {
            int cx = toPixelX(pos) + SQUARE_SIZE / 2;
            int cy = toPixelY(pos) + SQUARE_SIZE / 2;

            Piece occupant = game.getBoard().getPieceAt(pos);
            if (occupant != null) {
                // Draw a ring for captures
                g.setStroke(new BasicStroke(3));
                int padding = 4;
                ((Graphics2D) g).drawOval(toPixelX(pos) + padding, toPixelY(pos) + padding,
                        SQUARE_SIZE - 2 * padding, SQUARE_SIZE - 2 * padding);
            } else {
                // Draw a dot for empty squares
                int dotRadius = 8;
                g.fillOval(cx - dotRadius, cy - dotRadius, dotRadius * 2, dotRadius * 2);
            }
        }
    }

    private void drawPieces(Graphics2D g) {
        Font pieceFont = new Font("Serif", Font.PLAIN, 60);
        g.setFont(pieceFont);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = game.getBoard().getPieceAt(pos);
                if (piece != null) {
                    String key = (piece.getColor() == chess.model.Color.WHITE ? "W_" : "B_")
                            + piece.getType().name();
                    String symbol = PIECE_UNICODE.get(key);

                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(symbol);
                    int textHeight = fm.getAscent();
                    int px = toPixelX(pos) + (SQUARE_SIZE - textWidth) / 2;
                    int py = toPixelY(pos) + (SQUARE_SIZE + textHeight) / 2 - fm.getDescent();

                    // Draw shadow for better visibility
                    g.setColor(new java.awt.Color(0, 0, 0, 60));
                    g.drawString(symbol, px + 1, py + 1);

                    g.setColor(java.awt.Color.BLACK);
                    g.drawString(symbol, px, py);
                }
            }
        }
    }

    private void drawCoordinates(Graphics2D g) {
        Font coordFont = new Font("SansSerif", Font.BOLD, 12);
        g.setFont(coordFont);

        for (int i = 0; i < 8; i++) {
            // File labels (a-h) along bottom
            int col = boardFlipped ? (7 - i) : i;
            String file = String.valueOf((char) ('a' + col));
            boolean isLight = (0 + i) % 2 == 0;
            g.setColor(isLight ? LIGHT_SQUARE : DARK_SQUARE);
            g.drawString(file, i * SQUARE_SIZE + SQUARE_SIZE - 14, BOARD_SIZE - 4);

            // Rank labels (1-8) along left
            int row = boardFlipped ? i : (7 - i);
            String rank = String.valueOf(row + 1);
            boolean isLightRank = (row + 0) % 2 == 0;
            g.setColor(isLightRank ? LIGHT_SQUARE : DARK_SQUARE);
            g.drawString(rank, 3, i * SQUARE_SIZE + 16);
        }
    }

    /**
     * Callback interface for notifying the parent frame of events.
     */
    public interface GameEventListener {
        void onMoveMade();
    }
}

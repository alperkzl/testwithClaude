package chess.gui;

import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceType;
import chess.pieces.Pawn;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

/**
 * Displays the move history in standard algebraic-like notation.
 */
public class MoveHistoryPanel extends JPanel {

    private final JTextPane textPane;

    public MoveHistoryPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("  Move History", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        add(title, BorderLayout.NORTH);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textPane.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes the move history display from the game's move list.
     */
    public void updateMoves(List<Move> moves) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                int moveNum = (i / 2) + 1;
                sb.append(String.format("%3d. ", moveNum));
            }

            sb.append(formatMove(moves.get(i)));

            if (i % 2 == 0) {
                sb.append("    ");
            } else {
                sb.append("\n");
            }
        }

        textPane.setText(sb.toString());

        // Auto-scroll to bottom
        textPane.setCaretPosition(textPane.getDocument().getLength());
    }

    private String formatMove(Move move) {
        if (move.getMoveType() == MoveType.CASTLE_KINGSIDE) return "O-O";
        if (move.getMoveType() == MoveType.CASTLE_QUEENSIDE) return "O-O-O";

        StringBuilder sb = new StringBuilder();

        // Piece letter (pawns have no letter)
        if (!(move.getPieceMoved() instanceof Pawn)) {
            sb.append(pieceTypeLetter(move.getPieceMoved().getType()));
        }

        // Source file for pawn captures
        if (move.getPieceMoved() instanceof Pawn && move.getPieceCaptured() != null) {
            sb.append((char) ('a' + move.getFrom().getCol()));
        }

        // Capture indicator
        if (move.getPieceCaptured() != null) {
            sb.append('x');
        }

        // Destination square
        sb.append(move.getTo().toString());

        // Promotion
        if (move.getMoveType() == MoveType.PROMOTION) {
            sb.append('=');
            sb.append(pieceTypeLetter(move.getPromotionPiece()));
        }

        return sb.toString();
    }

    private char pieceTypeLetter(PieceType type) {
        switch (type) {
            case KING:   return 'K';
            case QUEEN:  return 'Q';
            case ROOK:   return 'R';
            case BISHOP: return 'B';
            case KNIGHT: return 'N';
            default:     return '?';
        }
    }
}

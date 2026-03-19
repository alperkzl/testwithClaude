package chess.gui;

import chess.model.Color;
import chess.model.PieceType;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for choosing a promotion piece.
 */
public class PromotionDialog {

    private static final String[] WHITE_SYMBOLS = {"\u2655", "\u2656", "\u2657", "\u2658"};
    private static final String[] BLACK_SYMBOLS = {"\u265B", "\u265C", "\u265D", "\u265E"};
    private static final PieceType[] PIECE_TYPES = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    /**
     * Shows a promotion dialog and returns the selected piece type.
     * Defaults to QUEEN if the dialog is closed without selection.
     */
    public static PieceType show(Component parent, Color color) {
        String[] symbols = (color == Color.WHITE) ? WHITE_SYMBOLS : BLACK_SYMBOLS;

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup group = new ButtonGroup();
        JToggleButton[] buttons = new JToggleButton[4];

        for (int i = 0; i < 4; i++) {
            JToggleButton btn = new JToggleButton(symbols[i]);
            btn.setFont(new Font("Serif", Font.PLAIN, 48));
            btn.setPreferredSize(new Dimension(70, 70));
            btn.setFocusPainted(false);
            buttons[i] = btn;
            group.add(btn);
            panel.add(btn);
        }

        // Default to queen
        buttons[0].setSelected(true);

        int result = JOptionPane.showConfirmDialog(
                parent, panel, "Choose promotion piece",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < 4; i++) {
                if (buttons[i].isSelected()) {
                    return PIECE_TYPES[i];
                }
            }
        }

        return PieceType.QUEEN;
    }
}

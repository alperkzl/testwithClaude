package chess;

import chess.gui.ChessWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            ChessWindow window = new ChessWindow();
            window.setVisible(true);
        });
    }
}

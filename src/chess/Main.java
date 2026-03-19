package chess;

import chess.gui.ChessWindow;
import chess.gui.GameModeDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            GameModeDialog.GameModeResult result = GameModeDialog.prompt(null);
            if (result == null) {
                System.exit(0);
            }

            ChessWindow window = new ChessWindow(result.getMode(), result.getHumanColor());
            window.setVisible(true);
            window.startGame();
        });
    }
}

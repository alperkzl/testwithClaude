package chess.gui;

import chess.model.Color;
import chess.model.Game;
import chess.model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Displays game status (turn, check/checkmate) and control buttons.
 */
public class GameInfoPanel extends JPanel {

    private final JLabel statusLabel;
    private final JLabel turnIndicator;
    private final JButton newGameButton;
    private final JButton undoButton;
    private final JButton flipButton;

    private GameControlListener controlListener;

    public GameInfoPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Top: turn indicator and status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        turnIndicator = new JLabel("\u25CF");
        turnIndicator.setFont(new Font("SansSerif", Font.PLAIN, 28));
        statusLabel = new JLabel("White's turn");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusPanel.add(turnIndicator);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.CENTER);

        // Right: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        undoButton = new JButton("Undo");
        newGameButton = new JButton("New Game");
        flipButton = new JButton("Flip Board");

        styleButton(undoButton);
        styleButton(newGameButton);
        styleButton(flipButton);

        undoButton.addActionListener(e -> {
            if (controlListener != null) controlListener.onUndo();
        });
        newGameButton.addActionListener(e -> {
            if (controlListener != null) controlListener.onNewGame();
        });
        flipButton.addActionListener(e -> {
            if (controlListener != null) controlListener.onFlipBoard();
        });

        buttonPanel.add(undoButton);
        buttonPanel.add(flipButton);
        buttonPanel.add(newGameButton);
        add(buttonPanel, BorderLayout.EAST);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setFocusPainted(false);
    }

    public void setControlListener(GameControlListener listener) {
        this.controlListener = listener;
    }

    /**
     * Updates the display based on current game state.
     */
    public void update(Game game) {
        Color turn = game.getCurrentTurn();
        GameState state = game.getGameState();

        turnIndicator.setForeground(turn == Color.WHITE ? java.awt.Color.WHITE : java.awt.Color.BLACK);

        switch (state) {
            case IN_PROGRESS:
                statusLabel.setText(turnName(turn) + "'s turn");
                break;
            case CHECK:
                statusLabel.setText(turnName(turn) + " is in CHECK!");
                break;
            case CHECKMATE:
                statusLabel.setText("CHECKMATE! " + turnName(turn.opposite()) + " wins!");
                break;
            case STALEMATE:
                statusLabel.setText("STALEMATE — Draw!");
                break;
            case DRAW_FIFTY_MOVE:
                statusLabel.setText("DRAW — 50-move rule");
                break;
            case DRAW_INSUFFICIENT_MATERIAL:
                statusLabel.setText("DRAW — Insufficient material");
                break;
        }

        undoButton.setEnabled(!game.getMoveHistory().isEmpty());
    }

    private String turnName(Color color) {
        return color == Color.WHITE ? "White" : "Black";
    }

    public interface GameControlListener {
        void onNewGame();
        void onUndo();
        void onFlipBoard();
    }
}

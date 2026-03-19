package chess.gui;

import chess.model.Game;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window that assembles all GUI components.
 */
public class ChessWindow extends JFrame
        implements BoardPanel.GameEventListener, GameInfoPanel.GameControlListener {

    private Game game;
    private BoardPanel boardPanel;
    private MoveHistoryPanel moveHistoryPanel;
    private GameInfoPanel gameInfoPanel;

    public ChessWindow() {
        super("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initGame();
        initComponents();
        updateDisplay();

        pack();
        setLocationRelativeTo(null);
    }

    private void initGame() {
        game = new Game();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Board (center)
        boardPanel = new BoardPanel(game, this);
        add(boardPanel, BorderLayout.CENTER);

        // Move history (right)
        moveHistoryPanel = new MoveHistoryPanel();
        add(moveHistoryPanel, BorderLayout.EAST);

        // Game info bar (top)
        gameInfoPanel = new GameInfoPanel();
        gameInfoPanel.setControlListener(this);
        add(gameInfoPanel, BorderLayout.NORTH);
    }

    private void updateDisplay() {
        gameInfoPanel.update(game);
        moveHistoryPanel.updateMoves(game.getMoveHistory());
        boardPanel.repaint();
    }

    // -- BoardPanel.GameEventListener --

    @Override
    public void onMoveMade() {
        updateDisplay();

        if (game.isGameOver()) {
            // Short delay so the board repaints before showing the dialog
            SwingUtilities.invokeLater(() -> showGameOverDialog());
        }
    }

    // -- GameInfoPanel.GameControlListener --

    @Override
    public void onNewGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Start a new game?", "New Game",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            game = new Game();
            boardPanel = new BoardPanel(game, this);
            getContentPane().removeAll();
            initComponents();
            updateDisplay();
            pack();
            repaint();
        }
    }

    @Override
    public void onUndo() {
        game.undoMove();
        boardPanel.resetSelection();
        updateDisplay();
    }

    @Override
    public void onFlipBoard() {
        boardPanel.setFlipped(!boardPanel.isFlipped());
    }

    private void showGameOverDialog() {
        String message;
        switch (game.getGameState()) {
            case CHECKMATE:
                message = game.getCurrentTurn().opposite().name() + " wins by checkmate!";
                break;
            case STALEMATE:
                message = "Game drawn by stalemate.";
                break;
            case DRAW_FIFTY_MOVE:
                message = "Game drawn by the 50-move rule.";
                break;
            case DRAW_INSUFFICIENT_MATERIAL:
                message = "Game drawn — insufficient material.";
                break;
            default:
                return;
        }

        int choice = JOptionPane.showOptionDialog(
                this, message, "Game Over",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"New Game", "OK"}, "OK");

        if (choice == 0) {
            onNewGame();
        }
    }
}

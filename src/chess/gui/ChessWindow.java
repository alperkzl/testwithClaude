package chess.gui;

import chess.engine.CpuPlayer;
import chess.engine.MinimaxPlayer;
import chess.model.Color;
import chess.model.Game;
import chess.model.GameMode;
import chess.model.Move;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window that assembles all GUI components
 * and orchestrates game flow for all three game modes.
 */
public class ChessWindow extends JFrame
        implements BoardPanel.GameEventListener, GameInfoPanel.GameControlListener {

    private Game game;
    private GameMode gameMode;
    private Color humanColor; // only relevant for HUMAN_VS_CPU

    private CpuPlayer cpuWhite;
    private CpuPlayer cpuBlack;

    private BoardPanel boardPanel;
    private MoveHistoryPanel moveHistoryPanel;
    private GameInfoPanel gameInfoPanel;

    private boolean cpuThinking = false;

    public ChessWindow(GameMode mode, Color humanColor) {
        super("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        this.gameMode = mode;
        this.humanColor = humanColor;

        initGame();
        initComponents();
        updateDisplay();

        pack();
        setLocationRelativeTo(null);
    }

    private void initGame() {
        game = new Game();
        cpuWhite = null;
        cpuBlack = null;

        switch (gameMode) {
            case HUMAN_VS_CPU:
                if (humanColor == Color.WHITE) {
                    cpuBlack = new MinimaxPlayer(4);
                } else {
                    cpuWhite = new MinimaxPlayer(4);
                }
                break;
            case CPU_VS_CPU:
                cpuWhite = new MinimaxPlayer(3);
                cpuBlack = new MinimaxPlayer(3);
                break;
            case HUMAN_VS_HUMAN:
                // No CPU players
                break;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Board (center)
        boardPanel = new BoardPanel(game, this);
        add(boardPanel, BorderLayout.CENTER);

        // Flip board if human plays black
        if (gameMode == GameMode.HUMAN_VS_CPU && humanColor == Color.BLACK) {
            boardPanel.setFlipped(true);
        }

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

    /**
     * Returns the CPU player for the current turn, or null if it's a human turn.
     */
    private CpuPlayer currentCpuPlayer() {
        if (game.getCurrentTurn() == Color.WHITE) return cpuWhite;
        return cpuBlack;
    }

    /**
     * Returns true if the current turn belongs to a human.
     */
    private boolean isHumanTurn() {
        return currentCpuPlayer() == null;
    }

    /**
     * Schedule a CPU move if it's the CPU's turn.
     * Uses SwingWorker to keep the UI responsive.
     */
    private void scheduleCpuMoveIfNeeded() {
        if (game.isGameOver() || isHumanTurn() || cpuThinking) return;

        CpuPlayer cpu = currentCpuPlayer();
        cpuThinking = true;
        boardPanel.setInputEnabled(false);
        gameInfoPanel.setCpuThinking(true);

        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return cpu.chooseMove(game);
            }

            @Override
            protected void done() {
                cpuThinking = false;
                try {
                    get(); // retrieve result (move already applied by CpuPlayer)
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boardPanel.setInputEnabled(isHumanTurn());
                updateDisplay();

                if (game.isGameOver()) {
                    SwingUtilities.invokeLater(() -> showGameOverDialog());
                } else {
                    // In CPU vs CPU, schedule the next move with a small delay for visibility
                    if (gameMode == GameMode.CPU_VS_CPU) {
                        Timer timer = new Timer(400, evt -> scheduleCpuMoveIfNeeded());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        scheduleCpuMoveIfNeeded();
                    }
                }
            }
        };
        worker.execute();
    }

    /**
     * Called after the window is shown. Kicks off CPU play if CPU moves first.
     */
    public void startGame() {
        scheduleCpuMoveIfNeeded();
    }

    // -- BoardPanel.GameEventListener --

    @Override
    public void onMoveMade() {
        updateDisplay();

        if (game.isGameOver()) {
            SwingUtilities.invokeLater(() -> showGameOverDialog());
        } else {
            scheduleCpuMoveIfNeeded();
        }
    }

    // -- GameInfoPanel.GameControlListener --

    @Override
    public void onNewGame() {
        if (cpuThinking) return; // Don't allow during CPU thinking

        int confirm = JOptionPane.showConfirmDialog(
                this, "Start a new game?", "New Game",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            startNewGameWithDialog();
        }
    }

    private void startNewGameWithDialog() {
        GameModeDialog.GameModeResult result = GameModeDialog.prompt(this);
        if (result == null) return;

        this.gameMode = result.getMode();
        this.humanColor = result.getHumanColor();

        initGame();
        getContentPane().removeAll();
        initComponents();
        updateDisplay();
        pack();
        repaint();
        scheduleCpuMoveIfNeeded();
    }

    @Override
    public void onUndo() {
        if (cpuThinking) return;

        // In Human vs CPU, undo two moves (CPU + human) so it's still the human's turn
        if (gameMode == GameMode.HUMAN_VS_CPU && game.getMoveHistory().size() >= 2) {
            game.undoMove();
            game.undoMove();
        } else {
            game.undoMove();
        }
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
            startNewGameWithDialog();
        }
    }
}

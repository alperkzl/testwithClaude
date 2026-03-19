package chess.gui;

import chess.model.Color;
import chess.model.GameMode;

import javax.swing.*;
import java.awt.*;

/**
 * Startup dialog for selecting game mode and CPU color.
 */
public class GameModeDialog extends JDialog {

    private GameMode selectedMode = null;
    private Color humanColor = Color.WHITE;
    private boolean confirmed = false;

    public GameModeDialog(Frame owner) {
        super(owner, "Chess — New Game", true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Title
        JLabel title = new JLabel("Select Game Mode");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(title, BorderLayout.NORTH);

        // Center: mode buttons + color chooser
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Mode radio buttons
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton hvh = new JRadioButton(GameMode.HUMAN_VS_HUMAN.getDisplayName());
        JRadioButton hvc = new JRadioButton(GameMode.HUMAN_VS_CPU.getDisplayName());
        JRadioButton cvc = new JRadioButton(GameMode.CPU_VS_CPU.getDisplayName());
        hvc.setSelected(true);

        Font radioFont = new Font("SansSerif", Font.PLAIN, 15);
        hvh.setFont(radioFont);
        hvc.setFont(radioFont);
        cvc.setFont(radioFont);

        modeGroup.add(hvh);
        modeGroup.add(hvc);
        modeGroup.add(cvc);

        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
        modePanel.add(hvh);
        modePanel.add(Box.createVerticalStrut(4));
        modePanel.add(hvc);
        modePanel.add(Box.createVerticalStrut(4));
        modePanel.add(cvc);
        center.add(modePanel);

        center.add(Box.createVerticalStrut(12));

        // Color chooser (only relevant for Human vs CPU)
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
        colorPanel.setBorder(BorderFactory.createTitledBorder("Play as"));

        ButtonGroup colorGroup = new ButtonGroup();
        JRadioButton playWhite = new JRadioButton("White");
        JRadioButton playBlack = new JRadioButton("Black");
        playWhite.setSelected(true);
        playWhite.setFont(radioFont);
        playBlack.setFont(radioFont);
        colorGroup.add(playWhite);
        colorGroup.add(playBlack);

        colorPanel.add(playWhite);
        colorPanel.add(Box.createVerticalStrut(4));
        colorPanel.add(playBlack);
        center.add(colorPanel);

        // Enable/disable color panel based on mode selection
        Runnable updateColorPanel = () -> {
            boolean humanVsCpu = hvc.isSelected();
            playWhite.setEnabled(humanVsCpu);
            playBlack.setEnabled(humanVsCpu);
        };
        hvh.addActionListener(e -> updateColorPanel.run());
        hvc.addActionListener(e -> updateColorPanel.run());
        cvc.addActionListener(e -> updateColorPanel.run());
        updateColorPanel.run();

        root.add(center, BorderLayout.CENTER);

        // Bottom: Start button
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        startButton.setPreferredSize(new Dimension(140, 38));
        startButton.addActionListener(e -> {
            if (hvh.isSelected()) selectedMode = GameMode.HUMAN_VS_HUMAN;
            else if (hvc.isSelected()) selectedMode = GameMode.HUMAN_VS_CPU;
            else selectedMode = GameMode.CPU_VS_CPU;

            humanColor = playWhite.isSelected() ? Color.WHITE : Color.BLACK;
            confirmed = true;
            dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(startButton);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(startButton);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public GameMode getSelectedMode() {
        return selectedMode;
    }

    public Color getHumanColor() {
        return humanColor;
    }

    /**
     * Convenience method: shows the dialog and returns the result.
     * Returns null if the user closed the dialog without confirming.
     */
    public static GameModeResult prompt(Frame owner) {
        GameModeDialog dialog = new GameModeDialog(owner);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            return new GameModeResult(dialog.getSelectedMode(), dialog.getHumanColor());
        }
        return null;
    }

    /**
     * Holds the result of the game mode selection.
     */
    public static class GameModeResult {
        private final GameMode mode;
        private final Color humanColor;

        public GameModeResult(GameMode mode, Color humanColor) {
            this.mode = mode;
            this.humanColor = humanColor;
        }

        public GameMode getMode() {
            return mode;
        }

        public Color getHumanColor() {
            return humanColor;
        }
    }
}

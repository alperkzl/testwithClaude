package chess.model;

/**
 * The three supported game modes.
 */
public enum GameMode {
    HUMAN_VS_HUMAN("Human vs Human"),
    HUMAN_VS_CPU("Human vs CPU"),
    CPU_VS_CPU("CPU vs CPU");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

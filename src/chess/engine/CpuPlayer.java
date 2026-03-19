package chess.engine;

import chess.model.Game;
import chess.model.Move;

/**
 * Interface for CPU players. Given a game state, returns the chosen move.
 */
public interface CpuPlayer {

    /**
     * Analyze the current game state and return the best move.
     * The game's current turn indicates which color this player is moving for.
     *
     * @param game the current game state (must not be game-over)
     * @return the chosen move
     */
    Move chooseMove(Game game);
}

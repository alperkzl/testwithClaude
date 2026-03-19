package chess.engine;

import chess.model.*;
import chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * CPU player using minimax search with alpha-beta pruning.
 */
public class MinimaxPlayer implements CpuPlayer {

    private final int maxDepth;
    private final BoardEvaluator evaluator;

    public MinimaxPlayer(int depth) {
        this.maxDepth = depth;
        this.evaluator = new BoardEvaluator();
    }

    public MinimaxPlayer() {
        this(4);
    }

    @Override
    public Move chooseMove(Game game) {
        Color myColor = game.getCurrentTurn();
        boolean isMaximizing = (myColor == Color.WHITE);

        List<MoveCandidate> candidates = generateAllMoves(game);
        if (candidates.isEmpty()) return null;

        MoveCandidate bestCandidate = null;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (MoveCandidate candidate : candidates) {
            Move move = executeMove(game, candidate);
            if (move == null) continue;

            int score = minimax(game, maxDepth - 1, alpha, beta, !isMaximizing);
            game.undoMove();

            if (isMaximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate;
                }
                alpha = Math.max(alpha, score);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestCandidate = candidate;
                }
                beta = Math.min(beta, score);
            }
        }

        // Actually apply the best move to the game
        if (bestCandidate != null) {
            return executeMove(game, bestCandidate);
        }
        return null;
    }

    private int minimax(Game game, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || game.isGameOver()) {
            return evaluator.evaluate(game);
        }

        List<MoveCandidate> candidates = generateAllMoves(game);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (MoveCandidate candidate : candidates) {
                Move move = executeMove(game, candidate);
                if (move == null) continue;

                int eval = minimax(game, depth - 1, alpha, beta, false);
                game.undoMove();

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Beta cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (MoveCandidate candidate : candidates) {
                Move move = executeMove(game, candidate);
                if (move == null) continue;

                int eval = minimax(game, depth - 1, alpha, beta, true);
                game.undoMove();

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha cutoff
            }
            return minEval;
        }
    }

    /**
     * Generates all (from, to) pairs for the current player.
     */
    private List<MoveCandidate> generateAllMoves(Game game) {
        List<MoveCandidate> candidates = new ArrayList<>();
        Color turn = game.getCurrentTurn();
        List<Piece> pieces = game.getBoard().getPieces(turn);

        for (Piece piece : pieces) {
            Position from = piece.getPosition();
            List<Position> targets = game.getLegalMoves(from);
            for (Position to : targets) {
                candidates.add(new MoveCandidate(from, to));
            }
        }

        return candidates;
    }

    /**
     * Executes a move on the game, handling promotion (always promotes to queen).
     */
    private Move executeMove(Game game, MoveCandidate candidate) {
        if (game.isPromotionMove(candidate.from, candidate.to)) {
            return game.makeMove(candidate.from, candidate.to, PieceType.QUEEN);
        }
        return game.makeMove(candidate.from, candidate.to);
    }

    /**
     * Simple holder for a from/to move candidate.
     */
    private static class MoveCandidate {
        final Position from;
        final Position to;

        MoveCandidate(Position from, Position to) {
            this.from = from;
            this.to = to;
        }
    }
}

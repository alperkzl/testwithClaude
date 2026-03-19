package chess.engine;

import chess.model.*;
import chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CPU player that picks a random legal move. Useful for testing.
 */
public class RandomPlayer implements CpuPlayer {

    private final Random random = new Random();

    @Override
    public Move chooseMove(Game game) {
        Color turn = game.getCurrentTurn();
        List<Piece> pieces = game.getBoard().getPieces(turn);

        // Collect all legal moves
        List<Position[]> allMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            Position from = piece.getPosition();
            List<Position> targets = game.getLegalMoves(from);
            for (Position to : targets) {
                allMoves.add(new Position[]{from, to});
            }
        }

        if (allMoves.isEmpty()) return null;

        Position[] chosen = allMoves.get(random.nextInt(allMoves.size()));
        Position from = chosen[0];
        Position to = chosen[1];

        if (game.isPromotionMove(from, to)) {
            return game.makeMove(from, to, PieceType.QUEEN);
        }
        return game.makeMove(from, to);
    }
}

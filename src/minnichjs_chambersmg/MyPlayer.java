package minnichjs_chambersmg;

import othello.Board;
import othello.IllegalCellException;
import othello.IllegalMoveException;

public class MyPlayer extends othello.AIPlayer{

    // Constants for piece values
    //TODO: Experiment with these values
    private static final double CORNER_VALUE = 25.0;
    private static final double EDGE_VALUE = 5.0;
    private static final double X_SQUARE_PENALTY = -10.0;
    private static final double C_SQUARE_PENALTY = -5.0;

    // Corner positions (GOOD!!!)
    private static final int[][] CORNERS = {
            {0, 0}, {0, 7}, {7, 0}, {7, 7}
    };

    // X-square positions (BAD!!!)
    private static final int[][] X_SQUARES = {
            {1, 1}, {1, 6}, {6, 1}, {6, 6}
    };

    // C-square positions (BAD!!!)
    private static final int[][] C_SQUARES = {
            {0, 1}, {1, 0}, {0, 6}, {1, 7},
            {6, 0}, {7, 1}, {6, 7}, {7, 6}
    };

    // Edge positions (GOOD!!!)
    private static final int[][] EDGES = {
            {0, 2}, {0, 3}, {0, 4}, {0, 5},
            {2, 0}, {3, 0}, {4, 0}, {5, 0},
            {2, 7}, {3, 7}, {4, 7}, {5, 7},
            {7, 2}, {7, 3}, {7, 4}, {7, 5}
    };

    public String getName() {
        return null;
    }

    @Override
    public double evaluate(Board board) {
        // Stop condition - if game is over
        if (board.getWinner() == Board.BLACK)
            return Double.MAX_VALUE;
        if (board.getWinner() == Board.WHITE)
            return -Double.MAX_VALUE;
        if (board.getWinner() == Board.EMPTY && !board.hasLegalMove())
            return 0; // Draw

        double score = 0.0;
        int blackCount = 0;
        int whiteCount = 0;
        int totalPieces = 0;

        // Count pieces and apply positional weights
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                try {
                    int cell = board.getCell(new int[]{x, y});
                    if (cell == Board.BLACK) blackCount++;
                    else if (cell == Board.WHITE) whiteCount++;

                    if (cell != Board.EMPTY) {
                        totalPieces++;

                        // Apply position-based weights
                        double positionValue = getPositionValue(x, y);
                        if (cell == Board.BLACK) score += positionValue;
                        else if (cell == Board.WHITE) score -= positionValue;
                    }
                } catch (IllegalCellException e) {
                    // This should not happen with valid coordinates
                }
            }
        }

        // Calculate piece count differential (more important later in game)
        double gameProgressFactor = totalPieces / 64.0;
        double pieceDiffScore = (blackCount - whiteCount) * gameProgressFactor * 10;
        score += pieceDiffScore;

        // Mobility evaluation (less important later in game)
        int blackMobility = countLegalMoves(board, Board.BLACK);
        int whiteMobility = countLegalMoves(board, Board.WHITE);
        double mobilityScore = (blackMobility - whiteMobility) * (1 - gameProgressFactor) * 5;
        score += mobilityScore;

        // Stability evaluation
        int blackStable = countStablePieces(board, Board.BLACK);
        int whiteStable = countStablePieces(board, Board.WHITE);
        double stabilityScore = (blackStable - whiteStable) * 3;
        score += stabilityScore;

        // Parity evaluation (for late game)
        if (gameProgressFactor > 0.7) {
            int emptySquares = 64 - totalPieces;
            if (emptySquares % 2 == 1 && board.getCurrentPlayer() == Board.BLACK) {
                score += 5;
            } else if (emptySquares % 2 == 0 && board.getCurrentPlayer() == Board.WHITE) {
                score += 5;
            }
        }

        return score;
    }

    public void getNextMove(Board board, int[] bestMove) throws IllegalCellException, IllegalMoveException
    {
        //this should be all the white pieces plus all the black pieces
        int totalPiecesOnTheBoard = board.countCells(1) + board.countCells(2);

        //TODO: we might wanna mess with these values
        int depthLimitForMinimax = 1;
        if (totalPiecesOnTheBoard > 55)
        {
            depthLimitForMinimax = 8;
        }
        else if (totalPiecesOnTheBoard > 40)
        {
            depthLimitForMinimax = 6;
        }
        else if (totalPiecesOnTheBoard > 10)
        {
            depthLimitForMinimax = 4;
        }

        try
        {
            //so this is an array of a single element, but now we can pass our long by reference, so it's value gets changed within the methods
            long[] numNodesExplored = new long[1];
            minimax(board, depthLimitForMinimax, true, bestMove, numNodesExplored);
        }
        catch (InterruptedException e)
        {
            //TODO: ask Wolfe why a thread interruption will happen?
            return;
        }
    }

    public double minimax(Board board, final int depthLimit,
                          final boolean useAlphaBetaPruning,
                          int [] bestMove, long [] numNodesExplored) throws InterruptedException{
        return 0.0;
    }
}

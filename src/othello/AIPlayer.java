/**
 * The AIPlayer interface represents an object that can supply
 * moves to the game of Othello and implements minimax with alpha-beta pruning
 */

package othello;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public abstract class AIPlayer implements Player {
    /**
     * Return a name for this player
     *
     * @return a String
     */
    public abstract String getName();

    /**
     * Given a Board, return a move in the array bestMove.
     */
    public void getNextMove(Board board, int[] bestMove)
            throws IllegalCellException, IllegalMoveException {
        final int DEPTH_LIMIT = 8;
        long[] numNodesExplored = new long[1];
        try {
            minimax(board, DEPTH_LIMIT, true, bestMove, numNodesExplored);
        }
        catch (InterruptedException e) {
        }
    }

    public abstract double evaluate(Board board);

    public abstract double minimax(Board board, final int depthLimit,
                                   final boolean useAlphaBetaPruning,
                                   int[] bestMove, long[] numNodesExplored) throws InterruptedException;




    public boolean checkTestCases(String testCaseFile, int depthLimit,
                                  boolean useAlphaBetaPruning) throws FileNotFoundException, InterruptedException {
        Scanner in = new Scanner(new File(testCaseFile));

        BoardImplementation b = new BoardImplementation();
        int[] bestMove = new int[2];
        long[] numNodesExplored = new long[1];

        while (in.hasNextLine()) {
            String boardString = in.nextLine().strip();
            double targetValue = Double.parseDouble(in.nextLine().strip());
            b.assignFromString(boardString);

            double myValue = Double.POSITIVE_INFINITY;
            try {
                myValue = minimax(b, depthLimit, useAlphaBetaPruning,
                        bestMove, numNodesExplored);
            } catch (InterruptedException e) {
                in.close();
                return false;
            }

            if (myValue != targetValue) {
                in.close();
                return false;
            }
        }

        in.close();
        return true;
    }
}

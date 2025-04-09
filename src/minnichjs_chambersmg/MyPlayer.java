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
        return "minnichjs_chambersmg";
    }

    /**
     * calls a bunch of helper methods to get a score for the board
     * @param board
     * @return score of the given board
     */
    @Override
    public double evaluate(Board board)
    {
        //do we need this stop condition or does the board class already handle this?
        if (board.getWinner() == Board.BLACK)
            return Double.MAX_VALUE;
        if (board.getWinner() == Board.WHITE)
            return -Double.MAX_VALUE;
        if (board.getWinner() == Board.EMPTY && !hasLegalMoves(board))
            return 0;

        double score = 0.0;
        int blackCount = 0;
        int whiteCount = 0;
        int totalPieces = 0;

        // Count pieces and apply positional weights
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                try
                {
                    int cell = board.getCell(new int[]{x, y});
                    if (cell == Board.BLACK)
                        blackCount++;
                    else if (cell == Board.WHITE)
                        whiteCount++;

                    if (cell != Board.EMPTY) {
                        totalPieces++;

                        // Apply position-based weights
                        double positionValue = getPositionValue(x, y);
                        if (cell == Board.BLACK)
                            score += positionValue;
                        else if (cell == Board.WHITE)
                            score -= positionValue;
                    }
                }
                catch (IllegalCellException e)
                {
                    System.err.println("Illegal cell exception: " + e.getMessage());
                    // This should not happen with valid coordinates
                }
            }
        }

        //calc piece count differential (this is gonna get more important later in the game)
        //maxing your tiles early game doesn't do very much because they can easily get flipped
        //maxing them late game is important tho, because you want to have max tiles when game ends
        double gameProgressFactor = totalPieces / 64.0;
        double pieceDiffScore = (blackCount - whiteCount) * gameProgressFactor * 10;
        score += pieceDiffScore;

        //mobility evaluation (less important later in game)
        //this is checking what moves are available to the players
        int blackMobility = countLegalMoves(board, Board.BLACK);
        int whiteMobility = countLegalMoves(board, Board.WHITE);
        double mobilityScore = (blackMobility - whiteMobility) * (1 - gameProgressFactor) * 5;
        score += mobilityScore;

        //stability evaluation
        //this is checking to see which pieces are unable to be changed
        //this is really good in the game because you want guaranteed pieces
        int blackStable = countStablePieces(board, Board.BLACK);
        int whiteStable = countStablePieces(board, Board.WHITE);
        double stabilityScore = (blackStable - whiteStable) * 3;
        score += stabilityScore;

        //parity evaluation (for late game)
        //forcing opponent to make last move in the game is good
        if (gameProgressFactor > 0.7)
        {
            int emptySquares = 64 - totalPieces;
            if (emptySquares % 2 == 1 && board.getPlayer() == Board.BLACK)
            {
                score += 5;
            }
            else if (emptySquares % 2 == 0 && board.getPlayer() == Board.WHITE)
            {
                score += 5;
            }
        }

        return score;
    }

    private double getPositionValue(int x, int y)
    {
        // Check for corners (EPIC!!!)
        for (int[] corner : CORNERS)
        {
            if (corner[0] == x && corner[1] == y)
            {
                return CORNER_VALUE;
            }
        }

        // Check for X-squares (really bad)
        for (int[] xSquare : X_SQUARES)
        {
            if (xSquare[0] == x && xSquare[1] == y)
            {
                return X_SQUARE_PENALTY;
            }
        }

        // Check for C-squares (bad)
        for (int[] cSquare : C_SQUARES)
        {
            if (cSquare[0] == x && cSquare[1] == y)
            {
                return C_SQUARE_PENALTY;
            }
        }

        // Check for edges (pretty good)
        for (int[] edge : EDGES)
        {
            if (edge[0] == x && edge[1] == y)
            {
                return EDGE_VALUE;
            }
        }

        // Regular squares
        return 1.0;
    }

    private int countLegalMoves(Board board, int player)
    {
        int count = 0;
        int originalPlayer = board.getPlayer();

        // If checking moves for the non-current player, create a board with that player's turn
        Board testBoard = board;
        if (originalPlayer != player) {
            testBoard = board.getClone();
            // Try to set the player (this might not directly work with the Board implementation)
            // This is a conceptual example - you might need to adapt this
            try {
                // Skip moves until it's the desired player's turn
                while (testBoard.getPlayer() != player) {
                    // If no legal moves, we can't force the turn to change
                    if (!hasLegalMoves(testBoard)) {
                        return 0;
                    }
                    // Make a pass move or any legal move
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            int[] move = {x, y};
                            if (testBoard.isLegalMove(move)) {
                                testBoard.makeMove(move);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // If we can't successfully switch players, return a default value
                return 5; // Arbitrary middle value
            }
        }

        // Count legal moves
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                try {
                    if (testBoard.isLegalMove(new int[]{x, y})) {
                        count++;
                    }
                } catch (Exception e) {
                    // This should not happen with valid coordinates
                }
            }
        }

        return count;
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
                          int [] bestMove, long [] numNodesExplored) throws InterruptedException
    {
        // Base case: do we have a winner or do we have run out of legal moves, or do we hit the depth limit?
        if(board.getWinner() != Board.EMPTY || depthLimit == 0 || !hasLegalMoves(board)){
            return evaluate(board);
        }


        // figure out which players turn it is
        boolean isMax = board.getPlayer() == Board.BLACK;
        double maxScore = 0;

        // if it's the black player
        if(isMax){
            maxScore = -Double.MAX_VALUE;
        }
        else{
            maxScore = Double.MIN_VALUE;
        }

        int[] currentBestMove = {-1, -1};

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int[] move = {i, j};
//                try {

                    if (board.isLegalMove(move)) {
                        Board nextBoard = board.getClone();
                    }
//                }
//                catch


            }
        }



        return 0.0;
    }

    public boolean hasLegalMoves(Board board){
        int[] move = new int[2];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                move[0] = i;
                move[1] = j;
                if(board.isLegalMove(move)){
                    return true;
                }
            }
        }
        return false;
    }

    public int pieceCountDifferential(Board board, int[] move){
        return 0;
    }

    public int mobility(){
        return 0;
    }

    public int getPositionalValue(){
        return 0;
    }

    public int countStablePieces(Board board, int color){
        return 0;
    }


}

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
        if (originalPlayer != player)
        {
            //gotta make a copy so we dont affect the game
            testBoard = board.getClone();
            try
            {
                //skip moves until it's the desired player's turn
                while (testBoard.getPlayer() != player)
                {
                    //super weird edge case where the player has no legal moves
                    //if no legal moves, we can't force the turn to change
                    if (!hasLegalMoves(testBoard))
                    {
                        return 0;
                    }
                    //make a pass move or any legal move
                    //just be done at the first one we find
                    //TODO: maybe implement greedy decision here? Instead of just first option?
                        //I doubt it's worth the extra runtime
                    for (int x = 0; x < 8; x++)
                    {
                        for (int y = 0; y < 8; y++)
                        {
                            int[] move = {x, y};
                            if (testBoard.isLegalMove(move))
                            {
                                //the current player makes the first move it finds
                                testBoard.makeMove(move);
                                break;
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                //if we can't successfully switch players, return a default value
                return 5; //idk, some arbitrary middle value
            }
        }

        //count legal moves
        //this chunk of code will run for either currPlayer OR opponent
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                try
                {
                    if (testBoard.isLegalMove(new int[]{x, y}))
                    {
                        count++;
                    }
                }
                catch (Exception e)
                {
                    //this should not happen with valid coordinates
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
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        numNodesExplored[0]++;

        // Base case: do we have a winner or do we have run out of legal moves, or do we hit the depth limit?
        if(board.getWinner() != Board.EMPTY || depthLimit == 0 || !hasLegalMoves(board)){
            return evaluate(board);
        }


        // figure out which players turn it is
        boolean isMax = board.getPlayer() == Board.BLACK;
        double bestScore = 0;

        // if it's the black player
        if(isMax){
            bestScore = -Double.MAX_VALUE;
        }
        else{
            bestScore = Double.MAX_VALUE;
        }

        int[] currentBestMove = {-1, -1};

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int[] move = {i, j};
                try {
                    if (board.isLegalMove(move)) {
                        Board nextBoard = board.getClone();
                        nextBoard.makeMove(move);

                        double score = 0.0;
                        if(useAlphaBetaPruning){
                            double initialAlpha = -Double.MAX_VALUE;
                            double initialBeta = Double.MAX_VALUE;
                            score = alphaBeta(nextBoard, depthLimit - 1, initialAlpha, initialBeta, numNodesExplored);
                        }
                        else{
                            int[] stubMove = new int[2];
                            score = minimax(nextBoard, depthLimit - 1, false, stubMove, numNodesExplored);
                        }

                        // if its a max node, replace if it's greater than the best score
                        // min node, replace if it's less than best current
                        if(isMax && score > bestScore || !isMax && score < bestScore) {
                            bestScore = score;
                            currentBestMove[0] = i;
                            currentBestMove[1] = j;
                        }

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
        // if we found a valid move, update best move
        if(currentBestMove[0] != -1 ){
            bestMove[0] = currentBestMove[0];
            bestMove[1] = currentBestMove[1];
        }

        return bestScore;
    }

    private double alphaBeta(Board board, int depth, double alpha, double beta, long[] numNodesExplored) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        numNodesExplored[0]++;

        // Base case: do we have a winner or do we have run out of legal moves, or do we hit the depth limit?
        if(board.getWinner() != Board.EMPTY || depth == 0 || !hasLegalMoves(board)){
            return evaluate(board);
        }


        // figure out which players turn it is
        boolean isMax = board.getPlayer() == Board.BLACK;
        double bestScore = 0;

        // if it's the black player
        if(isMax){
            bestScore = -Double.MAX_VALUE;
        }
        else{
            bestScore = Double.MAX_VALUE;
        }


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int[] move = {i, j};
                try {
                    if (board.isLegalMove(move)) {
                        Board nextBoard = board.getClone();
                        nextBoard.makeMove(move);

                        double score = alphaBeta(nextBoard, depth-1, alpha, beta, numNodesExplored);

                        if(isMax){
                            bestScore = Math.max(bestScore, score);
                            alpha = Math.max(alpha, score);
                        }
                        else{
                            bestScore = Math.min(bestScore, score);
                            beta = Math.min(alpha, score);
                        }

                        // prune branches
                        if(beta <= alpha){
                            break;
                        }
                    }
                } catch (IllegalMoveException e) {
                    throw new RuntimeException(e);
                }
            }
            // prune branches
            if(beta <= alpha){
                break;
            }
        }

        return bestScore;
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

    public int countStablePieces(Board board, int color){
        return 0;
    }


}

package minnichjs_chambersmg;

import othello.Board;
import othello.IllegalCellException;
import othello.IllegalMoveException;

public class MyPlayer extends othello.AIPlayer{
    public String getName() {
        return "minnichjs_chambersmg";
    }

    @Override
    public double evaluate(Board board) {
        return 0;
    }

    public void getNextMove(Board board, int[] bestMove)
            throws IllegalCellException, IllegalMoveException
    {

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
                            score = minimax(nextBoard, depthLimit - 1, false, stubMove, numNodesExplored)
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

    public int pieceCountDifferential(Board board, int[] move){
        return 0;
    }

    public int mobility(){
        return 0;
    }

    public int getPositionalValue(){
        return 0;
    }

    public boolean countStablePieces(Board board, Board.color){
        return false;
    }


}

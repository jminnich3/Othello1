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

    public boolean countStablePieces(Board board, Board.color){
        return false;
    }


}

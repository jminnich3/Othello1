package minnichjs_chambersmg;

import othello.Board;
import othello.IllegalCellException;
import othello.IllegalMoveException;

public class MyPlayer extends othello.AIPlayer{
    public String getName() {
        return null;
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
                          int [] bestMove, long [] numNodesExplored) throws InterruptedException{
        return 0.0;
    }
}

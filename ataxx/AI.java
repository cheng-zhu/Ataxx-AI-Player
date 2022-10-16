/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Cheng Zhu
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best;
        best = null;
        int bestScore;
        if (sense == 1) {
            bestScore = alpha;
        } else {
            bestScore = beta;
        }

        ArrayList<Move> allPossibleMoves = findAllPossibleMoves(board, sense);
        if (allPossibleMoves.size() == 0) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        for (Move oneMove : allPossibleMoves) {
            Board tempBoard = new Board(board);
            tempBoard.makeMove(oneMove);
            int response = minMax(tempBoard, depth - 1, false,
                    -1 * sense, alpha, beta);
            if (sense == 1) {
                if (bestScore <= response) {
                    int randomChoice = _choice[_random.nextInt(_choice.length)];
                    if (randomChoice == 0 || bestScore < response) {
                        bestScore = response;
                        best = oneMove;
                        alpha = max(alpha, bestScore);
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
            if (sense == -1) {
                if (bestScore >= response) {
                    int randomChoice = _choice[_random.nextInt(_choice.length)];
                    if (randomChoice == 0 || bestScore > response) {
                        bestScore = response;
                        best = oneMove;
                        beta = min(beta, bestScore);
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    private ArrayList<Move> findAllPossibleMoves(Board board, int sense) {
        ArrayList<Integer> myColorArr = new ArrayList<>();
        for (int i = 0; i < Move.EXTENDED_SIDE * Move.EXTENDED_SIDE; i++) {
            if (board.get(i) == RED && sense == 1) {
                myColorArr.add(i);
            }
            if (board.get(i) == BLUE && sense == -1) {
                myColorArr.add(i);
            }
        }
        ArrayList<Move> allPossibleMoves = new ArrayList<>();
        for (int piece : myColorArr) {
            char col0 = (char) (piece % 11 - 2 + 'a');
            char row0 = (char) (Math.floorDiv(piece + 1, 11) - 2 + '1');
            for (char col1 = (char) (col0 - 2);
                 col1 <= (char) (col0 + 2); col1++) {
                for (char row1 = (char) (row0 - 2);
                     row1 <= (char) (row0 + 2); row1++) {
                    if (board.legalMove(col0, row0, col1, row1)) {
                        allPossibleMoves.add(Move.move(col0, row0, col1, row1));
                    }
                }
            }
        }
        return allPossibleMoves;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }
        return board.redPieces() - board.bluePieces();
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();

    /** A random choice. */
    private int[] _choice = new int[]{0, 1};
}

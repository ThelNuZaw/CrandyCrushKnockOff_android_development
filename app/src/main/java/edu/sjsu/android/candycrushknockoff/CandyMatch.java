package edu.sjsu.android.candycrushknockoff;

/**
 * Finds and clears lines of three or more identical candies.
 * The board is a rectangle of candy ids. An empty cell is not a candy.
 */
public class CandyMatch {

    /** No candy in this cell. Empty cells never count as a match. */
    public static final int EMPTY = -1;

    private CandyMatch() {
    }

    /**
     * Detects horizontal and vertical matches of three or more
     * identical candies.
     */
    public static boolean[][] findMatches(int[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        boolean[][] matched = new boolean[rows][cols];

        markHorizontal(board, matched, rows, cols);
        markVertical(board, matched, rows, cols);

        return matched;
    }

    /**
     * Removes candies after a valid match.
     * Returns how many candies were cleared. A board with no match
     * is left as it is.
     */
    public static int removeMatches(int[][] board) {
        boolean[][] matched = findMatches(board);
        int removed = 0;

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (matched[row][col]) {
                    board[row][col] = EMPTY;
                    removed++;
                }
            }
        }

        return removed;
    }

    /**
     * Moves candies down to fill the empty spaces after a match.
     * The candies keep their order. Empty cells stay on top.
     * Returns how many candies moved.
     */
    public static int dropCandies(int[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        int moved = 0;

        for (int col = 0; col < cols; col++) {
            int write = rows - 1;

            for (int row = rows - 1; row >= 0; row--) {
                int candy = board[row][col];

                if (candy == EMPTY) {
                    continue;
                }

                if (write != row) {
                    board[write][col] = candy;
                    board[row][col] = EMPTY;
                    moved++;
                }

                write--;
            }
        }

        return moved;
    }

    /**
     * Removes a match, then lets the candies above fall.
     * Repeats when the fall makes another match.
     * Returns how many candies were cleared.
     */
    public static int removeAndDrop(int[][] board) {
        int removed = 0;
        int cleared;

        do {
            cleared = removeMatches(board);
            removed += cleared;

            if (cleared > 0) {
                dropCandies(board);
            }
        } while (cleared > 0);

        return removed;
    }

    private static void markHorizontal(
            int[][] board,
            boolean[][] matched,
            int rows,
            int cols
    ) {
        for (int row = 0; row < rows; row++) {
            int col = 0;

            while (col < cols) {
                int candy = board[row][col];
                int end = col + 1;

                while (end < cols && board[row][end] == candy) {
                    end++;
                }

                if (candy != EMPTY && end - col >= 3) {
                    for (int mark = col; mark < end; mark++) {
                        matched[row][mark] = true;
                    }
                }

                col = end;
            }
        }
    }

    private static void markVertical(
            int[][] board,
            boolean[][] matched,
            int rows,
            int cols
    ) {
        for (int col = 0; col < cols; col++) {
            int row = 0;

            while (row < rows) {
                int candy = board[row][col];
                int end = row + 1;

                while (end < rows && board[end][col] == candy) {
                    end++;
                }

                if (candy != EMPTY && end - row >= 3) {
                    for (int mark = row; mark < end; mark++) {
                        matched[mark][col] = true;
                    }
                }

                row = end;
            }
        }
    }
}

package edu.sjsu.android.candycrushknockoff;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Checks match detection and removal without the Android screen.
 */
public class CandyMatchTest {

    @Test
    public void horizontalLinesOfThreeOrMoreMatch() {
        int[][] three = {
                {0, 1, 1, 1, 2}
        };
        boolean[][] matched = CandyMatch.findMatches(three);

        assertFalse(matched[0][0]);
        assertTrue(matched[0][1]);
        assertTrue(matched[0][2]);
        assertTrue(matched[0][3]);
        assertFalse(matched[0][4]);

        int[][] four = {
                {3, 3, 3, 3, 0}
        };
        assertEquals(4, count(CandyMatch.findMatches(four)));

        int[][] five = {
                {4, 4, 4, 4, 4}
        };
        assertEquals(5, count(CandyMatch.findMatches(five)));

        // Two separate lines in the same row.
        int[][] both = {
                {0, 0, 0, 1, 1, 1}
        };
        assertEquals(6, count(CandyMatch.findMatches(both)));
    }

    @Test
    public void verticalLinesOfThreeOrMoreMatch() {
        int[][] board = {
                {1, 0, 2},
                {1, 4, 2},
                {1, 5, 2},
                {3, 5, 2}
        };

        boolean[][] matched = CandyMatch.findMatches(board);

        assertTrue(matched[0][0]);
        assertTrue(matched[1][0]);
        assertTrue(matched[2][0]);
        assertFalse(matched[3][0]);

        // Two of a kind in the middle column is not a match.
        assertFalse(matched[0][1]);
        assertFalse(matched[1][1]);
        assertFalse(matched[2][1]);
        assertFalse(matched[3][1]);

        // The right column is four of the same candy, including the edge.
        assertTrue(matched[0][2]);
        assertTrue(matched[1][2]);
        assertTrue(matched[2][2]);
        assertTrue(matched[3][2]);
    }

    @Test
    public void pairsDiagonalsAndCornersAreNotMatches() {
        int[][] pairs = {
                {1, 1, 0, 1, 1},
                {2, 0, 2, 0, 2}
        };
        assertEquals(0, count(CandyMatch.findMatches(pairs)));

        int[][] diagonal = {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
        assertEquals(0, count(CandyMatch.findMatches(diagonal)));

        // A 2x2 block is only pairs, so nothing clears.
        int[][] block = {
                {1, 1},
                {1, 1}
        };
        assertEquals(0, CandyMatch.removeMatches(block));
        assertEquals(1, block[0][0]);
        assertEquals(1, block[1][1]);
    }

    @Test
    public void crossingLinesCountEachCandyOnce() {
        int[][] ell = {
                {1, 1, 1, 2},
                {1, 3, 4, 5},
                {1, 6, 0, 2}
        };

        boolean[][] matched = CandyMatch.findMatches(ell);

        assertEquals(5, count(matched));
        assertTrue(matched[0][0]);
        assertTrue(matched[0][1]);
        assertTrue(matched[0][2]);
        assertTrue(matched[1][0]);
        assertTrue(matched[2][0]);
        assertFalse(matched[1][1]);

        int[][] plus = {
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0}
        };

        assertEquals(5, CandyMatch.removeMatches(plus));
        assertEquals(CandyMatch.EMPTY, plus[0][1]);
        assertEquals(CandyMatch.EMPTY, plus[1][0]);
        assertEquals(CandyMatch.EMPTY, plus[1][1]);
        assertEquals(CandyMatch.EMPTY, plus[1][2]);
        assertEquals(CandyMatch.EMPTY, plus[2][1]);
        assertEquals(0, plus[0][0]);
        assertEquals(0, plus[2][0]);
    }

    @Test
    public void removeClearsOnlyTheMatchedCandies() {
        int[][] board = {
                {2, 2, 2, 4},
                {1, 0, 3, 4},
                {5, 0, 1, 4},
                {5, 3, 1, 0}
        };

        int[][] before = copy(board);
        int removed = CandyMatch.removeMatches(board);

        assertEquals(6, removed);
        assertEquals(CandyMatch.EMPTY, board[0][0]);
        assertEquals(CandyMatch.EMPTY, board[0][1]);
        assertEquals(CandyMatch.EMPTY, board[0][2]);
        assertEquals(CandyMatch.EMPTY, board[0][3]);
        assertEquals(CandyMatch.EMPTY, board[1][3]);
        assertEquals(CandyMatch.EMPTY, board[2][3]);
        assertEquals(0, board[3][3]);

        // The candies beside the match stay put.
        assertEquals(before[1][0], board[1][0]);
        assertEquals(before[1][1], board[1][1]);
        assertEquals(before[3][0], board[3][0]);
        assertEquals(before[3][2], board[3][2]);

        // Finding matches does not change the board.
        int[][] still = copy(board);
        CandyMatch.findMatches(board);
        assertEquals(still[1][0], board[1][0]);
        assertEquals(still[0][0], board[0][0]);

        // A second pass does not clear the holes or anything else.
        assertEquals(0, CandyMatch.removeMatches(board));
    }

    @Test
    public void emptyCellsAreNotAMatch() {
        int[][] holes = {
                {
                        CandyMatch.EMPTY,
                        CandyMatch.EMPTY,
                        CandyMatch.EMPTY,
                        1
                },
                {1, 1, 0, 1}
        };

        assertEquals(0, count(CandyMatch.findMatches(holes)));
        assertEquals(0, CandyMatch.removeMatches(holes));
        assertEquals(CandyMatch.EMPTY, holes[0][0]);
        assertEquals(1, holes[0][3]);
    }

    @Test
    public void gameBoardClearsOnlyItsRealLines() {
        int[][] board = new int[8][8];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = (row + col) % 2;
            }
        }

        // One horizontal line of three, and one vertical line of four.
        board[3][2] = 5;
        board[3][3] = 5;
        board[3][4] = 5;

        board[0][7] = 4;
        board[1][7] = 4;
        board[2][7] = 4;
        board[3][7] = 4;

        assertEquals(7, CandyMatch.removeMatches(board));

        assertEquals(CandyMatch.EMPTY, board[3][2]);
        assertEquals(CandyMatch.EMPTY, board[3][3]);
        assertEquals(CandyMatch.EMPTY, board[3][4]);
        assertEquals(CandyMatch.EMPTY, board[0][7]);
        assertEquals(CandyMatch.EMPTY, board[3][7]);

        assertEquals(0, board[3][1]);
        assertEquals((3 + 5) % 2, board[3][5]);
        assertEquals((4 + 7) % 2, board[4][7]);
    }

    @Test
    public void candiesFallDownIntoTheGaps() {
        int[][] board = {
                {1, 4, CandyMatch.EMPTY},
                {CandyMatch.EMPTY, 5, 2},
                {3, CandyMatch.EMPTY, CandyMatch.EMPTY},
                {CandyMatch.EMPTY, 6, 7}
        };

        assertEquals(5, CandyMatch.dropCandies(board));

        assertEquals(CandyMatch.EMPTY, board[0][0]);
        assertEquals(CandyMatch.EMPTY, board[1][0]);
        assertEquals(1, board[2][0]);
        assertEquals(3, board[3][0]);

        assertEquals(CandyMatch.EMPTY, board[0][1]);
        assertEquals(4, board[1][1]);
        assertEquals(5, board[2][1]);
        assertEquals(6, board[3][1]);

        // A sideways gap is not a fall. The right column only slides down.
        assertEquals(CandyMatch.EMPTY, board[0][2]);
        assertEquals(CandyMatch.EMPTY, board[1][2]);
        assertEquals(2, board[2][2]);
        assertEquals(7, board[3][2]);

        // Packed columns and empty columns stay put.
        int[][] packed = {
                {1, CandyMatch.EMPTY},
                {2, CandyMatch.EMPTY}
        };
        assertEquals(0, CandyMatch.dropCandies(packed));
        assertEquals(1, packed[0][0]);
        assertEquals(2, packed[1][0]);
        assertEquals(CandyMatch.EMPTY, packed[0][1]);
    }

    @Test
    public void fallingCandiesCanMakeAnotherMatch() {
        int[][] board = {
                {1, 0, 4},
                {1, 3, 5},
                {2, 2, 2},
                {1, 4, 6}
        };

        // The middle row clears, then the two 1s above fall onto the 1 below.
        assertEquals(6, CandyMatch.removeAndDrop(board));

        assertEquals(CandyMatch.EMPTY, board[0][0]);
        assertEquals(CandyMatch.EMPTY, board[1][0]);
        assertEquals(CandyMatch.EMPTY, board[2][0]);
        assertEquals(CandyMatch.EMPTY, board[3][0]);

        assertEquals(CandyMatch.EMPTY, board[0][1]);
        assertEquals(0, board[1][1]);
        assertEquals(3, board[2][1]);
        assertEquals(4, board[3][1]);

        assertEquals(CandyMatch.EMPTY, board[0][2]);
        assertEquals(4, board[1][2]);
        assertEquals(5, board[2][2]);
        assertEquals(6, board[3][2]);

        // Nothing is left floating above a hole.
        assertEquals(0, CandyMatch.dropCandies(board));
        assertEquals(0, count(CandyMatch.findMatches(board)));
    }

    private static int count(boolean[][] matched) {
        int total = 0;

        for (boolean[] row : matched) {
            for (boolean cell : row) {
                if (cell) {
                    total++;
                }
            }
        }

        return total;
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[board.length][];

        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }

        return copy;
    }
}

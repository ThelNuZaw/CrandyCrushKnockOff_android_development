package edu.sjsu.android.candycrushknockoff;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Checks the move counter without the Android screen.
 */
public class MoveCounterTest {

    @Test
    public void aNewGameStartsWithTwentyMoves() {
        MoveCounter moves = new MoveCounter();

        assertEquals(20, moves.getRemaining());
        assertFalse(moves.isOut());
    }

    @Test
    public void eachMoveLowersTheCountByOne() {
        MoveCounter moves = new MoveCounter();

        assertEquals(19, moves.spend());
        assertEquals(18, moves.spend());
        assertEquals(18, moves.getRemaining());
        assertFalse(moves.isOut());
    }

    @Test
    public void theCountStopsAtZero() {
        MoveCounter moves = new MoveCounter(2);

        assertEquals(1, moves.spend());
        assertEquals(0, moves.spend());
        assertEquals(0, moves.spend());
        assertEquals(0, moves.getRemaining());
        assertTrue(moves.isOut());
    }

    @Test
    public void twentyMovesAreExactlyEnough() {
        MoveCounter moves = new MoveCounter();

        for (int move = 0; move < 20; move++) {
            assertFalse(moves.isOut());
            moves.spend();
        }

        assertEquals(0, moves.getRemaining());
        assertTrue(moves.isOut());
        assertEquals(0, moves.spend());
    }

    @Test
    public void aNegativeCountIsTreatedAsZero() {
        MoveCounter moves = new MoveCounter(-4);

        assertEquals(0, moves.getRemaining());
        assertTrue(moves.isOut());

        moves.setRemaining(7);
        assertEquals(7, moves.getRemaining());

        moves.setRemaining(-1);
        assertEquals(0, moves.getRemaining());
    }
}

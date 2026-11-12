package edu.sjsu.android.candycrushknockoff;

/**
 * Tracks how many moves are left in the level.
 * The count starts at 20 and never drops below zero.
 */
public class MoveCounter {

    public static final int STARTING_MOVES = 20;

    private int remaining;

    public MoveCounter() {
        this(STARTING_MOVES);
    }

    public MoveCounter(int remaining) {
        setRemaining(remaining);
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        if (remaining < 0) {
            this.remaining = 0;
        } else {
            this.remaining = remaining;
        }
    }

    /**
     * Uses one move.
     * Returns how many moves are left.
     */
    public int spend() {
        if (remaining > 0) {
            remaining--;
        }

        return remaining;
    }

    public boolean isOut() {
        return remaining == 0;
    }
}

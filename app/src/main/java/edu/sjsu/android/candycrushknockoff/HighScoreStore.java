package edu.sjsu.android.candycrushknockoff;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The only thing the first build persists: the best score.
 *
 * MERGE NOTE: the main menu reads from here too ("Best: 1240", hidden when
 * {@link #hasBest} is false). If a teammate already has their own prefs file or
 * key for the high score, keep one of the two and point both screens at it.
 */
public final class HighScoreStore {

    private static final String PREFS_NAME = "sweet_smash";
    private static final String KEY_BEST = "best_score";

    private HighScoreStore() {
    }

    public static boolean hasBest(Context context) {
        return prefs(context).contains(KEY_BEST);
    }

    public static int getBest(Context context) {
        return prefs(context).getInt(KEY_BEST, 0);
    }

    /**
     * Saves {@code score} if it beats the saved best.
     *
     * @return true if this score is a new best
     */
    public static boolean saveIfBest(Context context, int score) {
        if (score <= getBest(context)) {
            return false;
        }
        // commit() rather than apply(): the spec says the new best is written
        // before the game-over panel is shown.
        prefs(context).edit().putInt(KEY_BEST, score).commit();
        return true;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

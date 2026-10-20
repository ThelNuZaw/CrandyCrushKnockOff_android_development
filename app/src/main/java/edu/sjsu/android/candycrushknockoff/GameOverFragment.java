package edu.sjsu.android.candycrushknockoff;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Game-over board: a centered panel over the board, shown when moves hit 0 or
 * the timer ends. The board stays visible behind the dim, and the modal dialog
 * swallows every touch so the board ignores taps.
 *
 * MERGE NOTE (game board owner): when the game ends, call
 * <pre>
 *     GameOverFragment.show(getSupportFragmentManager(), this, finalScore);
 * </pre>
 * from the activity (or {@code getChildFragmentManager()} from a board
 * fragment), and implement {@link Listener} on that same activity / fragment.
 */
public class GameOverFragment extends DialogFragment {

    /** Implemented by whichever activity or parent fragment hosts the board. */
    public interface Listener {
        /** Start a fresh game at score 0 directly, without going through the main menu. */
        void onPlayAgain();

        /** Go back to the main menu without starting a game. */
        void onMainMenu();
    }

    public static final String TAG = "GameOverFragment";

    private static final String ARG_SCORE = "score";
    private static final String ARG_BEST = "best";
    private static final String ARG_NEW_BEST = "new_best";

    /**
     * Saves the high score (if beaten) and then shows the panel.
     * Safe to call twice, e.g. if moves and the timer run out together.
     */
    public static void show(@NonNull FragmentManager fragmentManager,
                            @NonNull Context context, int finalScore) {
        if (fragmentManager.findFragmentByTag(TAG) != null) {
            return;
        }
        // Write the new best before the panel is shown. The result goes into
        // the arguments so "New best!" survives rotation (after saving, the
        // stored best equals this score and would no longer compare as beaten).
        boolean newBest = HighScoreStore.saveIfBest(context, finalScore);
        int best = HighScoreStore.getBest(context);

        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, finalScore);
        args.putInt(ARG_BEST, best);
        args.putBoolean(ARG_NEW_BEST, newBest);

        GameOverFragment fragment = new GameOverFragment();
        fragment.setArguments(args);
        fragment.show(fragmentManager, TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Only the two buttons close the panel: no back press, no tap outside.
        // MERGE NOTE: if the group wants system back to act as "Main menu",
        // add an OnKeyListener on the dialog in onStart() that calls onMainMenu.
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_over_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();

        TextView score = view.findViewById(R.id.game_over_score);
        TextView best = view.findViewById(R.id.game_over_best);
        View newBest = view.findViewById(R.id.game_over_new_best);

        score.setText(getString(R.string.game_over_score, args.getInt(ARG_SCORE)));
        best.setText(getString(R.string.game_over_best, args.getInt(ARG_BEST)));
        newBest.setVisibility(args.getBoolean(ARG_NEW_BEST) ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.game_over_play_again).setOnClickListener(v -> {
            Listener listener = findListener();
            dismiss();
            if (listener != null) {
                listener.onPlayAgain();
            }
        });
        view.findViewById(R.id.game_over_main_menu).setOnClickListener(v -> {
            Listener listener = findListener();
            dismiss();
            if (listener != null) {
                listener.onMainMenu();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        // Let the panel's rounded bg_board card be the only background.
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Centered panel, ~88% of the screen width, capped for tablets.
        float density = getResources().getDisplayMetrics().density;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int width = Math.min((int) (screenWidth * 0.88f), (int) (360 * density));
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Dim the board (and blur it on Android 12+) so it stays visible behind the panel.
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            window.getAttributes().setBlurBehindRadius((int) (8 * density));
            window.setAttributes(window.getAttributes());
        }
    }

    @Nullable
    private Listener findListener() {
        Fragment parent = getParentFragment();
        if (parent instanceof Listener) {
            return (Listener) parent;
        }
        if (getActivity() instanceof Listener) {
            return (Listener) getActivity();
        }
        return null;
    }
}

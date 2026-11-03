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

public class GameOverFragment extends DialogFragment {

    public interface Listener {
        void onPlayAgain();

        void onMainMenu();
    }

    public static final String TAG = "GameOverFragment";

    private static final String ARG_WON = "won";
    private static final String ARG_SCORE = "score";
    private static final String ARG_BEST = "best";
    private static final String ARG_NEW_BEST = "new_best";

    /**
     * Retains compatibility with existing calls that omit the result.
     * Those calls default to a loss.
     */
    public static void show(
            @NonNull FragmentManager fragmentManager,
            @NonNull Context context,
            int finalScore
    ) {
        show(fragmentManager, context, finalScore, false);
    }

    /**
     * Saves the best score and displays the result.
     */
    public static void show(
            @NonNull FragmentManager fragmentManager,
            @NonNull Context context,
            int finalScore,
            boolean won
    ) {
        // Avoid changing fragments after state saving or opening duplicates.
        if (fragmentManager.isStateSaved()
                || fragmentManager.findFragmentByTag(TAG) != null) {
            return;
        }

        boolean newBest = HighScoreStore.saveIfBest(
                context,
                finalScore
        );

        int best = HighScoreStore.getBest(context);

        Bundle args = new Bundle();
        args.putBoolean(ARG_WON, won);
        args.putInt(ARG_SCORE, finalScore);
        args.putInt(ARG_BEST, best);
        args.putBoolean(ARG_NEW_BEST, newBest);

        GameOverFragment fragment = new GameOverFragment();
        fragment.setArguments(args);

        // Add immediately so another call can detect the existing dialog.
        fragment.showNow(fragmentManager, TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only Play Again and Main Menu close the dialog.
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.game_over_dialog,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();

        TextView title = view.findViewById(R.id.game_over_title);
        TextView score = view.findViewById(R.id.game_over_score);
        TextView best = view.findViewById(R.id.game_over_best);
        View newBest = view.findViewById(R.id.game_over_new_best);

        boolean won = args.getBoolean(ARG_WON);

        title.setText(
                won
                        ? R.string.game_won_title
                        : R.string.game_lost_title
        );

        score.setText(
                getString(
                        R.string.game_over_score,
                        args.getInt(ARG_SCORE)
                )
        );

        best.setText(
                getString(
                        R.string.game_over_best,
                        args.getInt(ARG_BEST)
                )
        );

        newBest.setVisibility(
                args.getBoolean(ARG_NEW_BEST)
                        ? View.VISIBLE
                        : View.GONE
        );

        view.findViewById(R.id.game_over_play_again)
                .setOnClickListener(v -> {
                    Listener listener = findListener();

                    dismiss();

                    if (listener != null) {
                        listener.onPlayAgain();
                    }
                });

        view.findViewById(R.id.game_over_main_menu)
                .setOnClickListener(v -> {
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

        window.setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );

        float density = getResources()
                .getDisplayMetrics()
                .density;

        int screenWidth = getResources()
                .getDisplayMetrics()
                .widthPixels;

        int width = Math.min(
                (int) (screenWidth * 0.88f),
                (int) (360 * density)
        );

        window.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );

        window.setDimAmount(0.5f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            );

            WindowManager.LayoutParams params =
                    window.getAttributes();

            params.setBlurBehindRadius((int) (8 * density));
            window.setAttributes(params);
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
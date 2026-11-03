package edu.sjsu.android.candycrushknockoff;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class GameActivity extends AppCompatActivity
        implements GameOverFragment.Listener {

    private static final int ROWS = 8;
    private static final int COLS = 8;

    // Temporary level rules.
    private static final int STARTING_MOVES = 20;
    private static final int TARGET_SCORE = 1000;

    private static final int[] CANDIES = {
            R.drawable.candy_gold,
            R.drawable.candy_blue,
            R.drawable.candy_orange,
            R.drawable.candy_green,
            R.drawable.candy_yellow,
            R.drawable.candy_rose
    };

    private int score = 0;
    private int movesRemaining = STARTING_MOVES;
    private boolean gameEnded = false;
    private boolean won = false;

    private GridLayout boardGrid;
    private boolean filled;
    private int[] boardState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.gameContent),
                (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    int side = dp(16);

                    v.setPadding(
                            bars.left + side,
                            bars.top + dp(10),
                            bars.right + side,
                            bars.bottom + dp(12)
                    );

                    return insets;
                }
        );

        findViewById(R.id.backButton)
                .setOnClickListener(v -> onMainMenu());

        boardGrid = findViewById(R.id.boardGrid);

        // Restore progress after rotation or activity recreation.
        if (savedInstanceState != null) {
            score = savedInstanceState.getInt("score", 0);
            movesRemaining = savedInstanceState.getInt(
                    "moves",
                    STARTING_MOVES
            );
            gameEnded = savedInstanceState.getBoolean("ended", false);
            won = savedInstanceState.getBoolean("won", false);
            boardState = savedInstanceState.getIntArray("board");
        }

        updateLabels();

        findViewById(R.id.boardHolder)
                .post(this::sizeAndFillBoard);
    }

    /**
     * Makes the board square before creating the candy views.
     */
    private void sizeAndFillBoard() {
        FrameLayout holder = findViewById(R.id.boardHolder);

        int size = Math.min(
                holder.getWidth(),
                holder.getHeight()
        );

        if (size == 0) {
            holder.post(this::sizeAndFillBoard);
            return;
        }

        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) boardGrid.getLayoutParams();

        params.width = size;
        params.height = size;
        params.gravity = Gravity.CENTER;

        boardGrid.setLayoutParams(params);
        boardGrid.post(this::fillBoard);
    }

    /**
     * Displays the initial board or restores its saved candies.
     */
    private void fillBoard() {
        if (filled || boardGrid.getWidth() == 0) {
            return;
        }

        filled = true;

        int innerWidth = boardGrid.getWidth()
                - boardGrid.getPaddingLeft()
                - boardGrid.getPaddingRight();

        int cellSize = innerWidth / COLS;

        int[][] placed = new int[ROWS][COLS];
        Random random = new Random();

        String[] names = getResources()
                .getStringArray(R.array.candy_names);

        boardGrid.removeAllViews();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int candy;

                if (boardState != null
                        && boardState.length == ROWS * COLS) {

                    candy = boardState[row * COLS + col];

                } else {
                    // Avoid starting with three matching candies in a line.
                    do {
                        candy = random.nextInt(CANDIES.length);
                    } while (makesThree(placed, row, col, candy));
                }

                placed[row][col] = candy;

                ImageView piece = new ImageView(this);
                showCandy(piece, candy, names);
                piece.setScaleType(ImageView.ScaleType.FIT_CENTER);

                int padding = dp(3);
                piece.setPadding(
                        padding,
                        padding,
                        padding,
                        padding
                );

                piece.setEnabled(!gameEnded);

                GridLayout.LayoutParams params =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(row),
                                GridLayout.spec(col)
                        );

                params.width = cellSize;
                params.height = cellSize;
                params.setGravity(Gravity.CENTER);

                boardGrid.addView(piece, params);
            }
        }

        boardState = new int[ROWS * COLS];

        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(
                    placed[row],
                    0,
                    boardState,
                    row * COLS,
                    COLS
            );
        }
    }

    /**
     * Detects horizontal and vertical matches of three or more
     * identical candies on the current board.
     */
    public boolean[][] findMatches() {
        if (!hasBoard()) {
            return new boolean[ROWS][COLS];
        }

        return CandyMatch.findMatches(readBoard());
    }

    /**
     * Removes candies after a valid match, lets the candies above
     * fall into the gaps, and updates the screen.
     * Returns how many candies were cleared.
     *
     * Call this after a swap that creates a match. A board with no
     * match is left as it is. onMoveCompleted is still called once,
     * after the whole move is finished.
     */
    public int removeMatchedCandies() {
        if (!hasBoard()) {
            return 0;
        }

        int[][] board = readBoard();
        int removed = CandyMatch.removeAndDrop(board);
        writeBoard(board);

        if (removed > 0) {
            refreshCandies();
        }

        return removed;
    }

    /**
     * Moves candies down to fill empty spaces and updates the screen.
     * Returns how many candies moved.
     */
    public int dropCandies() {
        if (!hasBoard()) {
            return 0;
        }

        int[][] board = readBoard();
        int moved = CandyMatch.dropCandies(board);
        writeBoard(board);

        if (moved > 0) {
            refreshCandies();
        }

        return moved;
    }

    /**
     * Your teammate calls this ONCE on the UI thread after a valid swap
     * and all resulting matches and chain reactions finish.
     *
     * pointsEarned is the total score earned from that entire move.
     * Invalid swaps must not call this method.
     */
    public void onMoveCompleted(int pointsEarned) {
        // Ignore callbacks after the game has ended.
        if (gameEnded) {
            return;
        }

        if (pointsEarned < 0) {
            throw new IllegalArgumentException(
                    "Points must not be negative"
            );
        }

        // Use long during addition to avoid integer overflow.
        score = (int) Math.min(
                Integer.MAX_VALUE,
                (long) score + pointsEarned
        );

        movesRemaining--;

        updateLabels();

        // Check winning first so reaching the target on the final move wins.
        if (score >= TARGET_SCORE || movesRemaining == 0) {
            won = score >= TARGET_SCORE;
            gameEnded = true;

            boardGrid.setEnabled(false);

            for (int i = 0; i < boardGrid.getChildCount(); i++) {
                boardGrid.getChildAt(i).setEnabled(false);
            }

            showResultIfReady();
        }
    }

    /**
     * Your teammate should check this before accepting board input.
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    private void updateLabels() {
        TextView scoreLabel = findViewById(R.id.scoreLabel);
        TextView movesLabel = findViewById(R.id.movesLabel);

        scoreLabel.setText(
                getString(
                        R.string.score_progress,
                        score,
                        TARGET_SCORE
                )
        );

        movesLabel.setText(
                getString(
                        R.string.moves_remaining,
                        movesRemaining
                )
        );
    }

    private void showResultIfReady() {
        if (gameEnded
                && !getSupportFragmentManager().isStateSaved()) {

            GameOverFragment.show(
                    getSupportFragmentManager(),
                    this,
                    score,
                    won
            );
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        // Show a deferred result after the activity resumes.
        showResultIfReady();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("score", score);
        outState.putInt("moves", movesRemaining);
        outState.putBoolean("ended", gameEnded);
        outState.putBoolean("won", won);
        outState.putIntArray("board", boardState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPlayAgain() {
        // Start a fresh game with score 0 and 20 moves.
        startActivity(new Intent(this, GameActivity.class));
        finish();
    }

    @Override
    public void onMainMenu() {
        Intent menu = new Intent(this, MainActivity.class);

        menu.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(menu);
        finish();
    }

    private boolean hasBoard() {
        return boardState != null && boardState.length == ROWS * COLS;
    }

    private int[][] readBoard() {
        int[][] board = new int[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(
                    boardState,
                    row * COLS,
                    board[row],
                    0,
                    COLS
            );
        }

        return board;
    }

    private void writeBoard(int[][] board) {
        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(
                    board[row],
                    0,
                    boardState,
                    row * COLS,
                    COLS
            );
        }
    }

    private void refreshCandies() {
        if (!filled || !hasBoard()) {
            return;
        }

        if (boardGrid.getChildCount() != boardState.length) {
            return;
        }

        String[] names = getResources()
                .getStringArray(R.array.candy_names);

        for (int i = 0; i < boardState.length; i++) {
            showCandy(
                    (ImageView) boardGrid.getChildAt(i),
                    boardState[i],
                    names
            );
        }
    }

    /**
     * Shows one candy, or a blank cell after that candy is removed.
     */
    private void showCandy(
            ImageView piece,
            int candy,
            String[] names
    ) {
        if (candy == CandyMatch.EMPTY) {
            piece.setImageDrawable(null);
            piece.setContentDescription(
                    getString(R.string.empty_cell)
            );
            return;
        }

        piece.setImageResource(CANDIES[candy]);
        piece.setContentDescription(names[candy]);
    }

    private boolean makesThree(
            int[][] placed,
            int row,
            int col,
            int candy
    ) {
        boolean horizontal = col >= 2
                && placed[row][col - 1] == candy
                && placed[row][col - 2] == candy;

        boolean vertical = row >= 2
                && placed[row - 1][col] == candy
                && placed[row - 2][col] == candy;

        return horizontal || vertical;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}
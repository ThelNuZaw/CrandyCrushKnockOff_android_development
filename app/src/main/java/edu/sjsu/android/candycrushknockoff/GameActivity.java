package edu.sjsu.android.candycrushknockoff;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final int ROWS = 8;
    private static final int COLS = 8;

    private static final int[] CANDIES = {
            R.drawable.candy_gold,
            R.drawable.candy_blue,
            R.drawable.candy_orange,
            R.drawable.candy_green,
            R.drawable.candy_yellow,
            R.drawable.candy_rose
    };

    private GridLayout boardGrid;
    private boolean filled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameContent), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int side = dp(16);
            v.setPadding(bars.left + side, bars.top + dp(10), bars.right + side, bars.bottom + dp(12));
            return insets;
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        boardGrid = findViewById(R.id.boardGrid);
        findViewById(R.id.boardHolder).post(this::sizeAndFillBoard);
    }

    /** Make the board a square, then drop a candy into every cell. */
    private void sizeAndFillBoard() {
        FrameLayout holder = findViewById(R.id.boardHolder);
        int size = Math.min(holder.getWidth(), holder.getHeight());
        if (size == 0) {
            holder.post(this::sizeAndFillBoard);
            return;
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) boardGrid.getLayoutParams();
        lp.width = size;
        lp.height = size;
        lp.gravity = Gravity.CENTER;
        boardGrid.setLayoutParams(lp);
        boardGrid.post(this::fillBoard);
    }

    private void fillBoard() {
        if (filled || boardGrid.getWidth() == 0) {
            return;
        }
        filled = true;

        int inner = boardGrid.getWidth() - boardGrid.getPaddingLeft() - boardGrid.getPaddingRight();
        int cell = inner / COLS;
        int[][] placed = new int[ROWS][COLS];
        Random random = new Random();
        String[] names = getResources().getStringArray(R.array.candy_names);

        boardGrid.removeAllViews();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int candy;
                // Start the level without three matching candies already in a line.
                do {
                    candy = random.nextInt(CANDIES.length);
                } while (makesThree(placed, row, col, candy));
                placed[row][col] = candy;

                ImageView piece = new ImageView(this);
                piece.setImageResource(CANDIES[candy]);
                piece.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int pad = dp(3);
                piece.setPadding(pad, pad, pad, pad);
                piece.setContentDescription(names[candy]);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(row),
                        GridLayout.spec(col));
                lp.width = cell;
                lp.height = cell;
                lp.setGravity(Gravity.CENTER);
                boardGrid.addView(piece, lp);
            }
        }
    }

    private boolean makesThree(int[][] placed, int row, int col, int candy) {
        boolean horizontal = col >= 2
                && placed[row][col - 1] == candy
                && placed[row][col - 2] == candy;
        boolean vertical = row >= 2
                && placed[row - 1][col] == candy
                && placed[row - 2][col] == candy;
        return horizontal || vertical;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

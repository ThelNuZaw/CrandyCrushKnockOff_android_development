package edu.sjsu.android.candycrushknockoff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * White game title with a thick blue outline, so it stays readable on the sky.
 */
public class OutlineTextView extends AppCompatTextView {

    private static final int STROKE_COLOR = 0xFF0055A2;

    private boolean drawingStroke;

    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!drawingStroke) {
            drawingStroke = true;
            Paint paint = getPaint();
            Paint.Style oldStyle = paint.getStyle();
            float oldWidth = paint.getStrokeWidth();
            Paint.Join oldJoin = paint.getStrokeJoin();
            int fill = getCurrentTextColor();

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(getTextSize() * 0.08f);
            setTextColor(STROKE_COLOR);
            super.onDraw(canvas);

            paint.setStyle(oldStyle);
            paint.setStrokeWidth(oldWidth);
            paint.setStrokeJoin(oldJoin);
            setTextColor(fill);
            drawingStroke = false;
        }
        super.onDraw(canvas);
    }
}

package com.tencent.qcloud.ugckit.component.circlebmp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 一个圆形的View
 */
public class TCCircleView extends View {
    private int   mColor = Color.RED;
    private Paint mPaint;
    private RectF mRectF;

    public TCCircleView(Context context) {
        super(context);
        init();
    }

    public TCCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mRectF = new RectF();
    }


    public void setColor(int color) {
        mColor = color;
        mPaint.setColor(mColor);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRectF.left = 0;
        mRectF.top = 0;
        mRectF.right = w;
        mRectF.bottom = h;
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(mRectF, mPaint);
    }
}

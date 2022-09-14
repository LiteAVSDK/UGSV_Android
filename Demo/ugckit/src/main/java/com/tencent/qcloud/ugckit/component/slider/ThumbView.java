package com.tencent.qcloud.ugckit.component.slider;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;

public class ThumbView extends View {

    private static final int EXTEND_TOUCH_SLOP = 15;

    private final int      mExtendTouchSlop;
    private       Drawable mThumbDrawable;
    private       boolean  mPressed;
    private       int      mThumbWidth;
    private       int      mTickIndex;

    public ThumbView(@NonNull Context context, int thumbWidth, Drawable drawable) {
        super(context);
        mThumbWidth = thumbWidth;
        mThumbDrawable = drawable;
        mExtendTouchSlop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                EXTEND_TOUCH_SLOP, context.getResources().getDisplayMetrics());
        setBackgroundDrawable(mThumbDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(mThumbWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));

        mThumbDrawable.setBounds(0, 0, mThumbWidth, getMeasuredHeight());
    }

    public void setThumbWidth(int thumbWidth) {
        mThumbWidth = thumbWidth;
    }

    public void setThumbDrawable(Drawable thumbDrawable) {
        mThumbDrawable = thumbDrawable;
        setBackgroundDrawable(mThumbDrawable);
    }

    public boolean inInTarget(int x, int y) {
        Rect rect = new Rect();
        getHitRect(rect);
        rect.left -= mExtendTouchSlop;
        rect.right += mExtendTouchSlop;
        rect.top -= mExtendTouchSlop;
        rect.bottom += mExtendTouchSlop;
        return rect.contains(x, y);
    }

    public int getRangeIndex() {
        return mTickIndex;
    }

    public void setTickIndex(int tickIndex) {
        mTickIndex = tickIndex;
    }

    @Override
    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void setPressed(boolean pressed) {
        mPressed = pressed;
    }
}

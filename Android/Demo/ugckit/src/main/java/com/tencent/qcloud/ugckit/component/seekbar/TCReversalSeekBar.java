package com.tencent.qcloud.ugckit.component.seekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.tencent.qcloud.ugckit.R;

public class TCReversalSeekBar extends View {
    private static final String  TAG                = "ReversalSeekBar";
    private              int     mWidth;
    private              int     mHeight;
    private              int     mSeekBarLeft;
    private              int     mSeekBarRight;
    private              int     mBgTop;
    private              int     mBgBottom;
    private              int     mRoundSize;
    private              int     mViewEnd;
    private              Paint   mNormalPaint;
    private              Paint   mPointerPaint;
    private              Paint   mProgressPaint;
    private              float   mPointerLeft;
    private              float   mPointerRight;
    private              float   mPointerTop;
    private              float   mPointerBottom;
    private              boolean mIsOnDrag;
    private              float   mCurrentLeftOffset = 0;
    private              float   mLastX;

    @Nullable
    private Drawable mPointerDrawable;
    private int      mHalfDrawableWidth;

    private float mCurrentProgress;

    public TCReversalSeekBar(Context context) {
        super(context);
        init(null);
    }

    public TCReversalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TCReversalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 1~0
     */
    public void setProgress(final float progress) {
        if (progress < 0 || progress > 1.0)
            throw new IllegalArgumentException("progress must between 0 and 1");
        mCurrentProgress = progress;
    }

    public float getProgress() {
        return mCurrentProgress;
    }

    private void init(@Nullable AttributeSet attrs) {
        int progressColor = Color.parseColor("#FF4081");
        int backgroundColor = Color.parseColor("#BBBBBB");
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UGCKitTCReversalSeekBar);
            mPointerDrawable = a.getDrawable(R.styleable.UGCKitTCReversalSeekBar_rs_pointerBackground);
            mHalfDrawableWidth = mPointerDrawable.getIntrinsicWidth() / 2;
            progressColor = a.getColor(R.styleable.UGCKitTCReversalSeekBar_rs_progressColor,
                    Color.parseColor("#FF4081"));
            backgroundColor = a.getColor(R.styleable.UGCKitTCReversalSeekBar_rs_backgroundColor,
                    Color.parseColor("#BBBBBB"));
            mCurrentProgress = a.getFloat(R.styleable.UGCKitTCReversalSeekBar_rs_progress, 0f);
            a.recycle();
        }
        mNormalPaint = new Paint();
        mNormalPaint.setColor(backgroundColor);

        mPointerPaint = new Paint();
        mPointerPaint.setColor(Color.RED);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mSeekBarLeft = mHalfDrawableWidth;
        mSeekBarRight = mWidth - mHalfDrawableWidth;

        mBgTop = 18;
        mBgBottom = mHeight - 18;
        mRoundSize = mHeight / 2;

        mViewEnd = mWidth;

        float dis = (mSeekBarRight - mSeekBarLeft) * mCurrentProgress;
        mPointerLeft = mViewEnd - dis - mHalfDrawableWidth;
        mLastX = mPointerLeft;
        calculatePointerRect();

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //draw  bg
        RectF rectF = new RectF();
        rectF.left = mSeekBarLeft;
        rectF.right = mSeekBarRight;
        rectF.top = mBgTop;
        rectF.bottom = mBgBottom;
        canvas.drawRoundRect(rectF, mRoundSize, mRoundSize, mNormalPaint);

        //draw progress
        if (mPointerRight < mViewEnd) {
            RectF pRecf = new RectF();
            pRecf.left = mPointerRight - mHalfDrawableWidth;
            pRecf.top = mBgTop;
            pRecf.right = mViewEnd;
            pRecf.bottom = mBgBottom;
            canvas.drawRoundRect(pRecf,
                    mRoundSize, mRoundSize, mProgressPaint);
        }

        //draw pointer
        Rect rect = new Rect();
        rect.left = (int) mPointerLeft;
        rect.top = (int) mPointerTop;
        rect.right = (int) mPointerRight;
        rect.bottom = (int) mPointerBottom;

        mPointerDrawable.setBounds(rect);
        mPointerDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled()) return false;

        boolean isHandle = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isHandle = handleDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isHandle = handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isHandle = handleUpEvent(event);
                break;

        }
        return isHandle;
    }

    private boolean handleUpEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mIsOnDrag = false;
            if (mListener != null) {
                mListener.onSeekUp();
            }
            return true;
        }
        return false;
    }

    private boolean handleMoveEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mCurrentLeftOffset = x - mLastX;
            //计算出标尺的Rect
            calculatePointerRect();
            if (mPointerRight - mHalfDrawableWidth <= mSeekBarLeft) {
                mPointerLeft = 0;
                mPointerRight = mPointerLeft + mPointerDrawable.getIntrinsicWidth();
            }
            if (mPointerLeft + mHalfDrawableWidth >= mSeekBarRight) {
                mPointerRight = mWidth;
                mPointerLeft = mWidth - mPointerDrawable.getIntrinsicWidth();
            }
            invalidate();
            callbackProgress();
            mLastX = x;
            return true;
        }
        return false;
    }

    private void callbackProgress() {
        if (mPointerLeft == 0) {
            callbackProgressInternal(1);
        } else if (mPointerRight == mWidth) {
            callbackProgressInternal(0);

        } else {
            float pointerMiddle = mPointerLeft + mHalfDrawableWidth;
            if (pointerMiddle == mViewEnd) {
                callbackProgressInternal(0);
            } else {
                float percent = Math.abs(mViewEnd - pointerMiddle) / mViewEnd * 1.0f;
                callbackProgressInternal(percent);
            }
        }
    }

    private void callbackProgressInternal(float progress) {
        mCurrentProgress = progress;
        if (mListener != null) {
            mListener.onSeekProgress(progress);
        }
    }


    private boolean handleDownEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (x >= mPointerLeft - 100 && x <= mPointerRight + 100) {
            if (mListener != null)
                mListener.onSeekDown();
            mIsOnDrag = true;
            mLastX = x;
            return true;
        }
        return false;
    }

    private void calculatePointerRect() {
        //draw pointer
        float pointerLeft = getPointerLeft(mCurrentLeftOffset);
        float pointerRight = pointerLeft + mPointerDrawable.getIntrinsicWidth();
        mPointerLeft = pointerLeft;
        mPointerRight = pointerRight;
        mPointerTop = 0;
        mPointerBottom = mHeight;
    }

    /**
     * 进行复位
     */
    public void resetSeekBar() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mPointerLeft, mViewEnd - mPointerDrawable.getIntrinsicWidth());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                mPointerLeft = (float) animation.getAnimatedValue();
                mPointerRight = mPointerLeft + mPointerDrawable.getIntrinsicWidth();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    private float getPointerLeft(float offset) {
        return mPointerLeft + offset;
    }

    private OnSeekProgressListener mListener;

    public void setOnSeekProgressListener(OnSeekProgressListener listener) {
        mListener = listener;
    }

    public interface OnSeekProgressListener {
        void onSeekDown();

        void onSeekUp();

        void onSeekProgress(float progress);
    }
}

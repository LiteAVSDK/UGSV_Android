package com.tencent.qcloud.ugckit.component.seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.tencent.qcloud.ugckit.R;

import java.util.ArrayList;
import java.util.Arrays;

public class TCTouchSeekBar extends View {
    private int               downX       = 0;
    private int               downY       = 0;
    private int               upX         = 0;
    private int               upY         = 0;
    private int               moveX       = 0;
    private int               moveY       = 0;
    private int               mViewWidth;
    private int               mViewHeight;
    private Bitmap            mDotDefaultBitmap;
    private Bitmap            mDotCheckedBitmap;
    private OnTouchCallback   mCallback;
    private int               mCurrentPos = 2;
    private ArrayList<String> mSelectionList;
    private int               mUnitWidth;
    private Paint             mPaint;
    private int               mTextSize   = 40;

    public TCTouchSeekBar(Context context) {
        super(context);
    }

    public TCTouchSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TCTouchSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UGCKitTCTouchSeekBar);
            mDotDefaultBitmap = ((BitmapDrawable) a.getDrawable(R.styleable.UGCKitTCTouchSeekBar_tsb_dotDefault)).getBitmap();
            mDotCheckedBitmap = ((BitmapDrawable) a.getDrawable(R.styleable.UGCKitTCTouchSeekBar_tsb_dotChecked)).getBitmap();
            a.recycle();
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 去锯齿
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(mTextSize);

        setSelectionList(null);
    }

    /**
     * 实例化后调用，设置bar的段数和文字
     */
    public void setSelectionList(@Nullable String[] section) {
        if (section != null) {
            mSelectionList = new ArrayList<String>();
            mSelectionList.addAll(Arrays.asList(section));
        } else {
            //随便写的
            String[] str = new String[]{
                    getResources().getString(R.string.ugckit_touch_seekbar_low),
                    getResources().getString(R.string.ugckit_touch_seekbar_mid),
                    getResources().getString(R.string.ugckit_touch_seekbar_high),
            };
            mSelectionList = new ArrayList<String>();
            mSelectionList.addAll(Arrays.asList(str));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
        //计算一个刻度的长度
        mUnitWidth = (mViewWidth - mDotDefaultBitmap.getWidth()) / (mSelectionList.size() - 1);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //先画一个背景 X轴横线
        canvas.drawLine(mDotDefaultBitmap.getWidth() / 2, mViewHeight / 2, mViewWidth - mDotDefaultBitmap.getWidth() / 2, mViewHeight / 2, mPaint);

        //画刻度点和刻度
        for (int i = 0; i < mSelectionList.size(); i++) {
            if (i == mCurrentPos) {
                canvas.drawBitmap(mDotCheckedBitmap,
                        mCurrentPos * mUnitWidth - (mDotCheckedBitmap.getWidth() - mDotDefaultBitmap.getWidth()) / 2,
                        mViewHeight / 2 - mDotCheckedBitmap.getHeight() / 2, mPaint);
            } else {
                canvas.drawBitmap(mDotDefaultBitmap, i * mUnitWidth, mViewHeight / 2 - mDotDefaultBitmap.getHeight() / 2, mPaint);
            }
            canvas.drawText(mSelectionList.get(i), i * mUnitWidth + (mDotDefaultBitmap.getWidth() - mTextSize / 2 * mSelectionList.get(i).length()) / 2, mViewHeight / 2 - mDotDefaultBitmap.getHeight() / 2 - 5, mPaint);
        }
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                responseTouch(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getX();
                moveY = (int) event.getY();
                responseTouch(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                upX = (int) event.getX();
                upY = (int) event.getY();
                responseTouch(upX, upY);
                mCallback.onCallback(mCurrentPos);
                break;
        }
        return true;
    }

    /**
     * 刷新点
     *
     * @param x
     * @param y
     */
    private void responseTouch(int x, int y) {
        if (x <= 0)
            mCurrentPos = 0;
        else if (x % mUnitWidth >= mUnitWidth / 2)
            mCurrentPos = x / mUnitWidth + 1;
        else
            mCurrentPos = x / mUnitWidth;

        invalidate();
    }

    //设置监听
    public void setOnTouchCallback(OnTouchCallback callback) {
        mCallback = callback;
    }

    //设置进度
    public void setProgress(int progress) {
        if (progress < 0)
            mCurrentPos = 0;
        else if (progress > mSelectionList.size() - 1)
            mCurrentPos = mSelectionList.size() - 1;
        else
            mCurrentPos = progress;
        invalidate();
    }

    //获取进度
    public int getProgress() {
        return mCurrentPos;
    }

    public interface OnTouchCallback {
        void onCallback(int position);
    }
}

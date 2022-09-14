package com.tencent.qcloud.ugckit.component.seekbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.tencent.qcloud.ugckit.R;


/**
 * 选择颜色的View
 */
public class TCColorView extends View {
    private Context               mContext;
    @Nullable
    private LinearGradient        linearGradient = null;
    @Nullable
    private Paint                 mHuePaint      = null;
    @Nullable
    private Paint                 mValuePaint    = null;
    @Nullable
    private RectF                 mHueRectF      = null;
    @Nullable
    private RectF                 mValueRectF    = null;
    private int                   mWidth;
    private Paint                 mSwipePaint;
    private Bitmap                mSwipeBitmap;
    private OnSelectColorListener mOnSelectColorListener;
    private int                   mColorHeight;
    private float                 mSwipeRadius;
    private int                   marginTopAndBottom;
    @NonNull
    private float[]               colorHSV       = new float[]{0f, 1f, 0f};
    private float                 mSwipeHueCx    = 0;
    private float                 mSwipeValueCx  = 0;

    public TCColorView(Context context) {
        super(context);

        init(context);
    }

    public TCColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TCColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mHuePaint = new Paint();
        mValuePaint = new Paint();
        mSwipePaint = new Paint();
        mSwipePaint.setAntiAlias(true);
        mSwipeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ugckit_color_swipe);
        mSwipeRadius = lastValueX = mSwipeBitmap.getWidth() / 2;

        mColorHeight = dp2px(10);
        marginTopAndBottom = dp2px(10);
    }


    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, mContext.getResources().getDisplayMetrics());
    }

    public float px2dp(int pxVal) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //绘制色相选择区域
        drawHuePanel(canvas);
        //绘制明度颜色条
        drawValuePanel(canvas);
    }

    @NonNull
    private int[] buildHueColorArray() {
        int[] hue = new int[361];
        for (int i = 0; i < hue.length; i++) {
            hue[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        return hue;
    }

    /**
     * 绘制色相选择区域
     *
     * @param canvas
     */
    private void drawHuePanel(@NonNull Canvas canvas) {
        //绘制颜色条
        if (mHueRectF == null)
            mHueRectF = new RectF(mSwipeRadius, mSwipeRadius - mColorHeight / 2 + marginTopAndBottom, mWidth - mSwipeRadius, mSwipeRadius + mColorHeight / 2 + marginTopAndBottom);
        if (linearGradient == null) {
            linearGradient = new LinearGradient(mHueRectF.left, mHueRectF.top, mHueRectF.right, mHueRectF.top, buildHueColorArray(), null,
                    Shader.TileMode.CLAMP);
            //设置渲染器
            mHuePaint.setShader(linearGradient);
        }
        canvas.drawRoundRect(mHueRectF, 15, 15, mHuePaint);
        //绘制滑块
        if (mSwipeHueCx < mSwipeRadius)
            mSwipeHueCx = mSwipeRadius;
        else if (mSwipeHueCx > mWidth - mSwipeRadius)
            mSwipeHueCx = mWidth - mSwipeRadius;
        canvas.drawBitmap(mSwipeBitmap, mSwipeHueCx - mSwipeRadius, marginTopAndBottom, mSwipePaint);
    }

    /**
     * 明度数组
     *
     * @return
     */
    @NonNull
    private int[] buildValueColorArray() {
        int[] value = new int[11];
        for (int i = 0; i < value.length; i++) {
            value[i] = Color.HSVToColor(new float[]{colorHSV[0], 1f, (float) i / 10});
        }
        return value;
    }

    /**
     * 绘制明度选择区域
     *
     * @param canvas
     */
    private void drawValuePanel(@NonNull Canvas canvas) {
        if (mValueRectF == null)
            mValueRectF = new RectF(mSwipeRadius, mSwipeRadius - mColorHeight / 2 + 3 * mSwipeRadius + marginTopAndBottom,
                    mWidth - mSwipeRadius, mSwipeRadius + mColorHeight / 2 + 3 * mSwipeRadius + marginTopAndBottom);
        final RectF rect = mValueRectF;

        //明度线性渲染器
        LinearGradient mValueShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                buildValueColorArray(), null, Shader.TileMode.CLAMP);

        mValuePaint.setShader(mValueShader);

        canvas.drawRoundRect(mValueRectF, 15, 15, mValuePaint);
        //绘制滑块
        if (mSwipeValueCx < mSwipeRadius)
            mSwipeValueCx = mSwipeRadius;
        else if (mSwipeValueCx > mWidth - mSwipeRadius)
            mSwipeValueCx = mWidth - mSwipeRadius;
        canvas.drawBitmap(mSwipeBitmap, mSwipeValueCx - mSwipeRadius, 3 * mSwipeRadius + marginTopAndBottom, mSwipePaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(measureWidth,
                measureHeight(heightMeasureSpec));
        mWidth = measureWidth;
    }


    private int measureHeight(int heightMeasureSpec) {
        int result = (int) (5 * mSwipeRadius + 2 * marginTopAndBottom);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY && specSize < result) {
            //throw new IllegalArgumentException("Height is too small to display completely , the height needs to be greater than " + px2dp(result) + "dp !");
        }
        return result;
    }

    private int   clickPanel = -1;
    private float lastValueX;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        if (x < mSwipeRadius || x > mWidth - mSwipeRadius)
            return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getY() < (5 * mSwipeRadius + 2 * marginTopAndBottom) / 2) { //色相区域
                    clickPanel = 1;
                    updateHueDate(x);
                } else if (event.getY() < 5 * mSwipeRadius + 2 * marginTopAndBottom) {
                    clickPanel = 2;
                    mSwipeValueCx = lastValueX = x;
                    callbackProgress();
                    invalidate();
                } else return super.onTouchEvent(event);

                break;
            case MotionEvent.ACTION_MOVE:
                if (clickPanel == 1) {
                    updateHueDate(x);
                } else if (clickPanel == 2) {
                    mSwipeValueCx = lastValueX = x;
                    callbackProgress();
                    invalidate();
                } else return super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                callbackFinish();
                clickPanel = -1;
                break;

        }
        return true;

    }

    private void updateHueDate(float x) {
        mSwipeHueCx = x;
        colorHSV[0] = 360 * (x - mSwipeRadius) / (mWidth - mSwipeBitmap.getWidth());
        callbackProgress();
        invalidate();
    }

    private void callbackProgress() {
        colorHSV[2] = (lastValueX - mSwipeRadius) / (mWidth - mSwipeBitmap.getWidth());
        if (mOnSelectColorListener != null) {
            mOnSelectColorListener.onProgressColor(Color.HSVToColor(colorHSV));
        }
    }


    private void callbackFinish() {
        colorHSV[2] = (lastValueX - mSwipeRadius) / (mWidth - mSwipeBitmap.getWidth());
        if (mOnSelectColorListener != null) {
            mOnSelectColorListener.onFinishColor(Color.HSVToColor(colorHSV));
        }
    }

    public void setOnSelectColorListener(@NonNull OnSelectColorListener listener) {
        this.mOnSelectColorListener = listener;
        listener.onProgressColor(Color.HSVToColor(colorHSV)); //初始
        listener.onFinishColor(Color.HSVToColor(colorHSV)); //初始
    }


    public interface OnSelectColorListener {
        void onFinishColor(@ColorInt int color);

        void onProgressColor(@ColorInt int color);
    }

    @Override
    public int getSolidColor() {
        return Color.HSVToColor(colorHSV);
    }
}

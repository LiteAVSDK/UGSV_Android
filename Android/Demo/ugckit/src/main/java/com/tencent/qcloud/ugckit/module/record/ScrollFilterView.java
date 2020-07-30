package com.tencent.qcloud.ugckit.module.record;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;

import com.tencent.liteav.demo.beauty.utils.ResourceUtils;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.liteav.demo.beauty.BeautyParams;
import com.tencent.qcloud.ugckit.R;

/**
 * 滑动滤镜View
 */
public class ScrollFilterView extends RelativeLayout implements View.OnTouchListener, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "ScrollFilterView";

    private int     mCurFilterIndex = 0;     // 当前滤镜Index
    private int     mLeftIndex = 0;          // 左右滤镜的Index
    private int     mRightIndex = 1;
    private int     mLastLeftIndex = -1;     // 之前左右滤镜的Index
    private int     mLastRightIndex = -1;
    private float   mLeftBitmapRatio;        // 左侧滤镜的比例
    private float   mMoveRatio;              // 滑动的比例大小
    private boolean mStartScroll;            // 已经开始滑动了标记
    private boolean mMoveRight;              // 是否往右滑动
    private boolean mIsNeedChange;           // 滤镜的是否需要发生改变
    private boolean mIsDoingAnimator;        // 是否正在执行动画
    private float   mLastScaleFactor;
    private float  mScaleFactor;

    private ValueAnimator          mFilterAnimator;
    @Nullable
    private Bitmap                 mLeftBitmap;
    @Nullable
    private Bitmap                 mRightBitmap;
    private GestureDetector        mGestureDetector;
    private ScaleGestureDetector   mScaleGestureDetector;
    private TextView               mTextFilter;
    private BeautyPanel            mBeautyPanel;
    private FrameLayout            mMaskLayout;

    private OnRecordFilterListener mOnRecordFilterListener;

    public ScrollFilterView(@NonNull Context context) {
        super(context);
        initViews();
    }

    public ScrollFilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ScrollFilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_scroll_filter_view, this);

        mMaskLayout = (FrameLayout) findViewById(R.id.mask);
        mMaskLayout.setOnTouchListener(this);
        mTextFilter = (TextView) findViewById(R.id.tv_filter);

        mGestureDetector = new GestureDetector(getContext(), this);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
    }

    @Override
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        if (v == mMaskLayout) {
            int pointerCount = event.getPointerCount();
            if (pointerCount >= 2) {
                mScaleGestureDetector.onTouchEvent(event);
            } else if (pointerCount == 1) {
                mGestureDetector.onTouchEvent(event);
                // 滤镜滑动后结束
                if (mStartScroll && event.getAction() == MotionEvent.ACTION_UP) {
                    doFilterAnimator();
                }
            }
        }
        return true;
    }

    /**
     * 功能：</p>
     * 1、当向左向右滑动屏幕时，滑动距离达到0.2比例时，切换滤镜</p>
     * 2、执行切换滤镜动画
     */
    private void doFilterAnimator() {
        if (mMoveRatio >= 0.2f) {
            mIsNeedChange = true;
            if (mMoveRight) { // 向右滑动
                mCurFilterIndex--;
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 1);
            } else { //左滑动
                mCurFilterIndex++;
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 0);
            }
        } else {
            if (mCurFilterIndex == mLeftIndex) {//向左侧滑动
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 1);
            } else {
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 0);
            }
        }
        mFilterAnimator.addUpdateListener(mAnimatorUpdateListener);
        mFilterAnimator.start();
    }

    /**
     * 切换滤镜动画监听器
     */
    @NonNull
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            mIsDoingAnimator = true;

            float leftRatio = (float) valueAnimator.getAnimatedValue();
            // 动画结束
            if (leftRatio == 0 || leftRatio == 1) {
                mLeftBitmapRatio = leftRatio;
                if (mIsNeedChange) {
                    mIsNeedChange = false;
                    doTextAnimator(mCurFilterIndex);
                } else {
                    mIsDoingAnimator = false;
                }

                // 保存到params 以便程序切换后恢复滤镜
                UGCKitRecordConfig config = VideoRecordSDK.getInstance().getConfig();
                BeautyParams beautyParams = config.mBeautyParams;
                if (beautyParams != null) {
                    if (mCurFilterIndex == mLeftIndex) {
                        beautyParams.mFilterBmp = mLeftBitmap;
                    } else {
                        beautyParams.mFilterBmp = mRightBitmap;
                    }
                    mBeautyPanel.setCurrentFilterIndex(mCurFilterIndex);
                    beautyParams.mFilterStrength = mBeautyPanel.getFilterProgress(mCurFilterIndex);
                }
            }
            float leftSpecialRatio = mBeautyPanel.getFilterProgress(mLeftIndex) / 10.f;
            float rightSpecialRatio = mBeautyPanel.getFilterProgress(mRightIndex) / 10.f;
            VideoRecordSDK.getInstance().setFilter(mLeftBitmap, leftSpecialRatio, mRightBitmap, rightSpecialRatio, leftRatio);
        }
    };

    /**
     * 执行滤镜滑动动画
     *
     * @param start
     * @param end
     * @return
     */
    private ValueAnimator generateValueAnimator(float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(400);
        return animator;
    }

    /**
     * 设置当前滤镜的名字
     * @param index
     */
    public void doTextAnimator(int index) {
        String filterName = ResourceUtils.getString(mBeautyPanel.getFilterItemInfo(index).getItemName());
        mTextFilter.setText(filterName);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(400);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTextFilter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTextFilter.setVisibility(View.GONE);
                mIsDoingAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTextFilter.startAnimation(alphaAnimation);
    }

    /************************************************************************/
    /*****                 OnGestureListener回调                         *****/
    /************************************************************************/
    @Override
    public boolean onDown(MotionEvent e) {
        mStartScroll = false;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        if (mOnRecordFilterListener != null) {
            mOnRecordFilterListener.onSingleClick(e.getX(), e.getY());
        }
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent downEvent, @NonNull MotionEvent moveEvent, float distanceX, float distanceY) {
        if (mIsDoingAnimator) {
            return true;
        }
        boolean moveRight = moveEvent.getX() > downEvent.getX();
        if (moveRight && mCurFilterIndex == 0) {
            return true;
        } else if (!moveRight && mCurFilterIndex == mBeautyPanel.getFilterSize() - 1) {
            return true;
        } else {
            mStartScroll = true;
            if (moveRight) {//往右滑动
                mLeftIndex = mCurFilterIndex - 1;
                mRightIndex = mCurFilterIndex;
            } else {// 往左滑动
                mLeftIndex = mCurFilterIndex;
                mRightIndex = mCurFilterIndex + 1;
            }

            if (mLastLeftIndex != mLeftIndex) { //如果不一样，才加载bitmap出来；避免滑动过程中重复加载
                mLeftBitmap = mBeautyPanel.getFilterResource(mLeftIndex);
                mLastLeftIndex = mLeftIndex;
            }

            if (mLastRightIndex != mRightIndex) {//如果不一样，才加载bitmap出来；避免滑动过程中重复加载
                mRightBitmap = mBeautyPanel.getFilterResource(mRightIndex);
                mLastRightIndex = mRightIndex;
            }

            int width = mMaskLayout.getWidth();
            float dis = moveEvent.getX() - downEvent.getX();
            float leftBitmapRatio = Math.abs(dis) / (width * 1.0f);

            float leftSpecialRatio = mBeautyPanel.getFilterProgress(mLeftIndex) / 10.0f;
            float rightSpecialRatio = mBeautyPanel.getFilterProgress(mRightIndex) / 10.0f;
            mMoveRatio = leftBitmapRatio;
            if (moveRight) {
                leftBitmapRatio = leftBitmapRatio;
            } else {
                leftBitmapRatio = 1 - leftBitmapRatio;
            }
            this.mMoveRight = moveRight;
            mLeftBitmapRatio = leftBitmapRatio;

            VideoRecordSDK.getInstance().setFilter(mLeftBitmap, leftSpecialRatio, mRightBitmap, rightSpecialRatio, leftBitmapRatio);
            return true;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    /************************************************************************/
    /*****               OnScaleGestureListener回调                      *****/
    /************************************************************************/
    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        int maxZoom = VideoRecordSDK.getInstance().getRecorder().getMaxZoom();
        if (maxZoom == 0) {
            TXCLog.i(TAG, "camera not support zoom");
            return false;
        }

        float factorOffset = detector.getScaleFactor() - mLastScaleFactor;

        mScaleFactor += factorOffset;
        mLastScaleFactor = detector.getScaleFactor();
        if (mScaleFactor < 0) {
            mScaleFactor = 0;
        }
        if (mScaleFactor > 1) {
            mScaleFactor = 1;
        }

        int zoomValue = Math.round(mScaleFactor * maxZoom);
        VideoRecordSDK.getInstance().getRecorder().setZoom(zoomValue);
        return false;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        mLastScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public void setOnRecordFilterListener(OnRecordFilterListener listener) {
        mOnRecordFilterListener = listener;
    }

    public void setBeautyPannel(BeautyPanel beautyPannel) {
        mBeautyPanel = beautyPannel;
    }

    public interface OnRecordFilterListener {
        void onSingleClick(float x, float y);
    }

}

package com.tencent.qcloud.ugckit.component.timeline;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tencent.qcloud.ugckit.R;


import java.util.Locale;

public class RangeSliderViewContainer extends LinearLayout {
    private final String TAG = "RangeSliderView";

    private Context                  mContext;
    private View                     mRootView;
    private View                     mStartView;        // 左边拖动控件
    private View                     mEndView;          // 右边拖动控件
    private View                     mMiddleView;       // 中间裁剪区域
    private long                     mStartTimeMs;        // 起始时间us
    private long                     mDurationMs;         // 最终的时长us
    private long                     mEndTimeMs;          // 结束时间us
    private long                     mMaxDuration;      // 允许设置的最大时长
    private int                      mDistance;          // 中间裁剪区域距离
    private ViewTouchProcess         mStartViewTouchProcess;
    private ViewTouchProcess         mEndViewTouchProcess;
    private VideoProgressController  mVideoProgressController;
    private OnDurationChangeListener mOnDurationChangeListener;

    public interface OnDurationChangeListener {
        void onDurationChange(long startTimeMs, long endTimeMs);
    }

    public void setDurationChangeListener(OnDurationChangeListener onDurationChangeListener) {
        mOnDurationChangeListener = onDurationChangeListener;
    }

    public RangeSliderViewContainer(Context context) {
        super(context);
        initView(context);
    }

    public RangeSliderViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RangeSliderViewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.ugckit_layout_range_slider, this);
        mStartView = mRootView.findViewById(R.id.iv_start_view);
        mEndView = mRootView.findViewById(R.id.iv_end_view);
        mMiddleView = mRootView.findViewById(R.id.middle_view);

        mStartViewTouchProcess = new ViewTouchProcess(mStartView);
        mEndViewTouchProcess = new ViewTouchProcess(mEndView);
    }

    public void init(@NonNull VideoProgressController videoProgressController, long startTimeMs, long durationMs, long maxDurationMs) {
        mVideoProgressController = videoProgressController;
        mStartTimeMs = startTimeMs;
        mDurationMs = durationMs;
        mMaxDuration = maxDurationMs;
        mEndTimeMs = mStartTimeMs + mDurationMs;

        mDistance = videoProgressController.duration2Distance(mDurationMs);

        ViewGroup.LayoutParams layoutParams = mMiddleView.getLayoutParams();
        layoutParams.width = mDistance;
        mMiddleView.setLayoutParams(layoutParams);
        setMiddleRangeColor(mContext.getResources().getColor(R.color.ugckit_color_accent_transparent30));

        setTouchProcessListener();
    }

    /**
     * 设置中间范围颜色
     *
     * @param color
     */
    public void setMiddleRangeColor(int color) {
        mMiddleView.setBackgroundColor(color);
    }

    private void setTouchProcessListener() {
        mStartViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);

                Log.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mStartView distance = %f, dtime = %d", distance, dtime));

                if (dtime > 0 && mDurationMs - dtime < 0) {
                    dtime = mDurationMs;
                } else if (dtime < 0 && (mStartTimeMs + dtime < 0)) {
                    dtime = -mStartTimeMs;
                }
                if (dtime == 0) {
                    return;
                }

                mDurationMs -= dtime;
                mStartTimeMs = mStartTimeMs + dtime;
                MarginLayoutParams layoutParams = (MarginLayoutParams) mStartView.getLayoutParams();
                int dx = layoutParams.leftMargin;

                Log.i(TAG, String.format(Locale.getDefault(), "onPostionChanged, mStartView layoutParams.leftMargin = %d", layoutParams.leftMargin));

                changeStartViewLayoutParams();
                dx = layoutParams.leftMargin - dx;

                layoutParams = (MarginLayoutParams) mMiddleView.getLayoutParams();
                layoutParams.width -= dx;
                layoutParams.width = Math.max(layoutParams.width, 0);
            }

            @Override
            public void onChangeComplete() {
                mVideoProgressController.setIsRangeSliderChanged(true);
                mVideoProgressController.setCurrentTimeMs(mStartTimeMs);
                if (mOnDurationChangeListener != null) {
                    mOnDurationChangeListener.onDurationChange(mStartTimeMs, mEndTimeMs);
                }
            }
        });


        mEndViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);

                if (dtime < 0 && (mEndTimeMs + dtime - mStartTimeMs) < 0) {
                    dtime = mStartTimeMs - mEndTimeMs;
                } else if (dtime > 0 && mEndTimeMs + dtime > mMaxDuration) {
                    dtime = mMaxDuration - mEndTimeMs;
                }
                if (dtime == 0) {
                    return;
                }
                mDurationMs += dtime;

                ViewGroup.LayoutParams layoutParams = mMiddleView.getLayoutParams();
                layoutParams.width = mVideoProgressController.duration2Distance(mDurationMs);

                mEndTimeMs = mEndTimeMs + dtime;
                mMiddleView.setLayoutParams(layoutParams);
            }

            @Override
            public void onChangeComplete() {
                mVideoProgressController.setIsRangeSliderChanged(true);
                mVideoProgressController.setCurrentTimeMs(mEndTimeMs);
                if (mOnDurationChangeListener != null) {
                    mOnDurationChangeListener.onDurationChange(mStartTimeMs, mEndTimeMs);
                }
            }
        });
    }

    public void changeStartViewLayoutParams() {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mStartView.getLayoutParams();
        layoutParams.leftMargin = mVideoProgressController.calculateStartViewPosition(this);

        mStartView.setLayoutParams(layoutParams);
    }

    public void setEditComplete() {
        mStartView.setVisibility(View.INVISIBLE);
        mEndView.setVisibility(View.INVISIBLE);
    }

    public void showEdit() {
        mStartView.setVisibility(View.VISIBLE);
        mEndView.setVisibility(View.VISIBLE);
    }

    @NonNull
    public ViewGroup getContainer() {
        return (ViewGroup) mRootView;
    }

    public View getStartView() {
        return mStartView;
    }

    public View getEndView() {
        return mEndView;
    }

    public long getStartTimeUs() {
        return mStartTimeMs;
    }

    public long getDuration() {
        return mDurationMs;
    }
}

package com.tencent.qcloud.ugckit.component.timeline;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.time.TCTimeFragment;

public class SliderViewContainer extends LinearLayout {
    private static final String                     TAG        = "RepeatSliderView";
    private              Context                    mContext;
    private              View                       mRootView;
    private              ImageView                  mSliderView;
    private              long                       mStartTimeMs;
    private              VideoProgressController    mVideoProgressController;
    private              ViewTouchProcess           mViewTouchProcess;
    private              OnStartTimeChangedListener mOnStartTimeChangedListener;
    private              int                        mSliderIcon = R.color.ugckit_slider_bg;
    private              long                       mSliderDuration = TCTimeFragment.DEAULT_REPEAT_DURATION_MS;

    public void setSliderIcon(int icon) {
        mSliderIcon = icon;
    }

    public interface OnStartTimeChangedListener {
        void onStartTimeMsChanged(long timeMs);
    }

    public SliderViewContainer(Context context) {
        super(context);
        init(context);
    }

    public SliderViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SliderViewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setOnStartTimeChangedListener(OnStartTimeChangedListener onStartTimeChangedListener) {
        mOnStartTimeChangedListener = onStartTimeChangedListener;
    }

    private void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.ugckit_layout_repeat_slider, this);
        mSliderView = (ImageView) mRootView.findViewById(R.id.iv_slider);
        mSliderView.setImageResource(mSliderIcon);

        mViewTouchProcess = new ViewTouchProcess(mSliderView);
        setTouchProcessListener();
    }

    private void setTouchProcessListener() {
        mViewTouchProcess.setOnPositionChangedListener(new ViewTouchProcess.OnPositionChangedListener() {
            @Override
            public void onPostionChanged(float distance) {
                long dtime = mVideoProgressController.distance2Duration(distance);
                long duration = VideoEditerSDK.getInstance().getVideoDuration() - mStartTimeMs;
                if (dtime > 0 && (duration - dtime < 0)) {
                    dtime = duration - mStartTimeMs;
                } else if (dtime < 0 && (mStartTimeMs + dtime < 0)) {
                    dtime = -mStartTimeMs;
                }
                if (dtime == 0) {
                    return;
                }
                mStartTimeMs = mStartTimeMs + dtime;
                mStartTimeMs = Math
                        .min(mStartTimeMs, (VideoEditerSDK.getInstance().getVideoDuration() - mSliderDuration));
                changeLayoutParams();
            }

            @Override
            public void onChangeComplete() {
                if (mOnStartTimeChangedListener != null) {
                    mOnStartTimeChangedListener.onStartTimeMsChanged(mStartTimeMs);
                }
            }

        });
    }

    public void changeLayoutParams() {
        if (mVideoProgressController != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mSliderView.getLayoutParams();
            layoutParams.leftMargin = mVideoProgressController.calculateSliderViewPosition(SliderViewContainer.this);
            layoutParams.width = mVideoProgressController.calculateSliderWidth(mSliderDuration);
            mSliderView.setLayoutParams(layoutParams);
        }
    }

    public View getSliderView() {
        return mSliderView;
    }

    public void setVideoProgressControlloer(VideoProgressController videoProgressControlloer) {
        mVideoProgressController = videoProgressControlloer;
    }

    public void setStartTimeMs(long startTimeMs, long duration) {
        mStartTimeMs = startTimeMs;
        mSliderDuration = duration;
        changeLayoutParams();
    }

    public long getStartTimeMs() {
        return mStartTimeMs;
    }
}

package com.tencent.qcloud.ugckit.module.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.component.timeline.SliderViewContainer;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressView;
import com.tencent.qcloud.ugckit.module.effect.time.TCTimeFragment;
import com.tencent.qcloud.ugckit.module.effect.time.TimeEffect;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;

import java.util.List;

/**
 * 编辑控件：图片时间轴
 */
public class TimeLineView extends RelativeLayout implements ITimeLineView, VideoProgressController.VideoProgressSeekListener, PlayerManagerKit.OnPreviewListener {
    private static final String TAG = "TimeLineView";

    private FragmentActivity        mActivity;
    private ImageView               mImageSlider;
    private VideoProgressView       mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private SliderViewContainer     mSpeedSlider;
    private SliderViewContainer     mRepeatSlider;
    private int                     startProgressIcon = R.drawable.ugckit_ic_repeate_range;
    private OnTimeChangeListener    mListener;

    public TimeLineView(Context context) {
        super(context);
        initViews();
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (FragmentActivity) getContext();
        inflate(getContext(), R.layout.ugckit_video_timeline, this);
        mImageSlider = (ImageView) findViewById(R.id.iv_player_slider);
        mVideoProgressView = (VideoProgressView) findViewById(R.id.video_progress_view);

        PlayerManagerKit.getInstance().addOnPreviewLitener(this);
    }

    /**
     * 初始化进度布局
     */
    @Override
    public void initVideoProgressLayout() {
        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;

        List<Bitmap> thumbnailList = VideoEditerSDK.getInstance().getAllThumbnails();
        mVideoProgressView.setViewWidth(screenWidth);
        mVideoProgressView.setThumbnailData(thumbnailList);
        VideoEditerSDK.getInstance().setThumbnailListener(new VideoEditerSDK.ThumbnailsListener() {
            @Override
            public void onThumbnailGot(long time, final Bitmap bitmap) {
                mVideoProgressView.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoProgressView.addThumbnailDate(bitmap);
                    }
                });

            }
        });

        mVideoProgressController = new VideoProgressController();
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        mVideoProgressController.setVideoProgressSeekListener(this);
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);
    }

    @Override
    public void onVideoProgressSeek(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    @Override
    public void onVideoProgressSeekFinish(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    public VideoProgressController getVideoProgressController() {
        return mVideoProgressController;
    }

    public VideoProgressView getVideoProgressView() {
        return mVideoProgressView;
    }

    @Override
    public void onPreviewProgress(int timeMs) {
        int currentState = PlayerManagerKit.getInstance().getCurrentState();
        if (currentState == PlayState.STATE_PLAY || currentState == PlayState.STATE_RESUME) {
            mVideoProgressController.setCurrentTimeMs(timeMs);
        }
    }

    @Override
    public void onPreviewFinish() {

    }

    @Override
    public void updateUIByFragment(int type) {
        if (type == UGCKitConstants.TYPE_EDITER_BGM) {
            mVideoProgressView.setVisibility(View.GONE);
        } else {
            mVideoProgressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setCurrentProgessIconResource(int resid) {
        mImageSlider.setImageResource(resid);
    }

    public void onAddSlider(int type, long time) {
        switch (type) {
            case TimeEffect.REPEAT_EFFECT:
                addRepeatSliderView(time);
                break;
            case TimeEffect.SPEED_EFFECT:
                addSpeedSliderView(time);
                break;
        }
    }

    private void addSpeedSliderView(long startEffectTime) {
        if (mSpeedSlider == null) {
            mSpeedSlider = new SliderViewContainer(getContext());
            mSpeedSlider.setSliderIcon(startProgressIcon);
            mSpeedSlider.setStartTimeMs(startEffectTime, VideoEditerSDK.getInstance().getEffectDrawWidth());
            mSpeedSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mListener != null) {
                        mListener.onTimeChange(TimeEffect.SPEED_EFFECT, timeMs);
                    }
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                }
            });
            mVideoProgressController.addSliderView(mSpeedSlider);
        }
    }

    private void addRepeatSliderView(long startEffectTime) {
        if (mRepeatSlider == null) {
            mRepeatSlider = new SliderViewContainer(getContext());
            mRepeatSlider.setSliderIcon(startProgressIcon);
            mRepeatSlider.setStartTimeMs(startEffectTime, VideoEditerSDK.getInstance().getEffectDrawWidth());
            mRepeatSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mListener != null) {
                        mListener.onTimeChange(TimeEffect.REPEAT_EFFECT, timeMs);
                    }
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                }
            });
            mVideoProgressController.addSliderView(mRepeatSlider);
        }
    }

    public void onRemoveSlider(int type) {
        switch (type) {
            case TimeEffect.REPEAT_EFFECT:
                removeRepeatSliderView();
                break;
            case TimeEffect.SPEED_EFFECT:
                removeSpeedSliderView();
                break;
        }
    }

    private void removeSpeedSliderView() {
        if (mSpeedSlider != null) {
            mVideoProgressController.removeSliderView(mSpeedSlider);
        }
        mSpeedSlider = null;
    }

    private void removeRepeatSliderView() {
        if (mRepeatSlider != null) {
            mVideoProgressController.removeSliderView(mRepeatSlider);
        }
        mRepeatSlider = null;
    }

    public long getCurrentTime() {
        return mVideoProgressController.getCurrentTimeMs();
    }

    public void setCurrentTime(long time) {
        mVideoProgressController.setCurrentTimeMs(time);
    }

    public void setOnTimeChangeListener(OnTimeChangeListener listener) {
        mListener = listener;
    }

    public interface OnTimeLineListener {
        /**
         * 添加SliderView
         *
         * @param type            类型 {@link TimeEffect}
         * @param startEffectTime 开始添加特效时间点
         */
        void onAddSlider(int type, long startEffectTime);

        /**
         * 移除SliderView
         *
         * @param type 类型 {@link TimeEffect}
         */
        void onRemoveSlider(int type);

        long getCurrentTime();

        void setCurrentTime(long time);

        void onRefresh();

    }

    public interface OnTimeChangeListener {
        /**
         * 调整SliderView时间
         *
         * @param type 类型 {@link TimeEffect}
         * @param time 调整特效开始时间点
         */
        void onTimeChange(int type, long time);
    }
}

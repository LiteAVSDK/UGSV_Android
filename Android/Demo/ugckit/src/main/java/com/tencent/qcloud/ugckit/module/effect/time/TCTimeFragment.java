package com.tencent.qcloud.ugckit.module.effect.time;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.TimeLineView;
import com.tencent.qcloud.ugckit.module.effect.TimelineViewUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.timeline.SliderViewContainer;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 时间特效的Fragment
 */
public class TCTimeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "TCTimeFragment";
    public static final long DEAULT_DURATION_MS = 1000; //默认重复时间段1s
    private int mCurrentEffect = NONE_EFFECT;

    public static final int NONE_EFFECT = -1;
    private static final int SPEED_EFFECT = 1;
    private static final int REPEAT_EFFECT = 2;
    private static final int REVERSE_EFFECT = 3;

    private ImageView mTvCancel, mTvSpeed, mTvRepeat, mTvReverse;
    private CircleImageView mTvCancelSelect, mTvSpeedSelect, mTvRepeatSelect, mTvReverseSelect;

    private TXVideoEditer mTXVideoEditer;
    private VideoProgressController mVideoProgressController;
    private SliderViewContainer mRepeatSlider;

    private SliderViewContainer mSpeedSlider;
    private long mCurrentEffectStartMs;

    //定制化Gif
    private int noTimeMotionGif = R.drawable.motion_time_normal;
    private int slowMotionGif = R.drawable.motion_time_slow;
    private int repeatGif = R.drawable.motion_time_repeat;
    private int reverseGif = R.drawable.motion_time_reverse;
    private int coverIcon = R.drawable.ic_effect5;
    private int startProgressIcon = R.drawable.ic_repeate_range;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        VideoEditerSDK wrapper = VideoEditerSDK.getInstance();
        mTXVideoEditer = wrapper.getEditer();

        initViews(view);
        initEffect();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimeLineView timeLineView = TimelineViewUtil.getInstance().getTimeLineView();
        if (timeLineView != null) {
            mVideoProgressController = timeLineView.getVideoProgressController();
        }
    }

    private void initEffect() {
        mCurrentEffect = TCTimeViewInfoManager.getInstance().getCurrentEffect();
        mCurrentEffectStartMs = TCTimeViewInfoManager.getInstance().getCurrentStartMs();

        switch (mCurrentEffect) {
            case NONE_EFFECT:
                showNoneLayout();
                break;
            case SPEED_EFFECT:
                mSpeedSlider = new SliderViewContainer(getContext());
                mSpeedSlider.setSliderIcon(startProgressIcon);
                mSpeedSlider.setStartTimeMs(mCurrentEffectStartMs);
                mSpeedSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                    @Override
                    public void onStartTimeMsChanged(long timeMs) {
                        if (mCurrentEffect != SPEED_EFFECT)
                            cancelSetEffect();
                        mCurrentEffect = SPEED_EFFECT;
                        setSpeed(timeMs);

                        PlayerManagerKit.getInstance().previewAtTime(timeMs);
                        // 进度条移动到当前位置
                        mVideoProgressController.setCurrentTimeMs(timeMs);
                        mCurrentEffectStartMs = timeMs;
                    }
                });
                mVideoProgressController.addSliderView(mSpeedSlider);
                mTvSpeedSelect.setVisibility(View.VISIBLE);
                break;
            case REPEAT_EFFECT:
                mRepeatSlider = new SliderViewContainer(getContext());
                mSpeedSlider.setSliderIcon(startProgressIcon);
                mRepeatSlider.setStartTimeMs(mCurrentEffectStartMs);
                mRepeatSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                    @Override
                    public void onStartTimeMsChanged(long timeMs) {
                        if (mCurrentEffect != REPEAT_EFFECT) {
                            cancelSetEffect();
                        }
                        mCurrentEffect = REPEAT_EFFECT;

                        setRepeatList(timeMs);

                        PlayerManagerKit.getInstance().previewAtTime(timeMs);
                        // 进度条移动到当前位置
                        mVideoProgressController.setCurrentTimeMs(timeMs);
                        mCurrentEffectStartMs = timeMs;
                    }
                });
                mVideoProgressController.addSliderView(mRepeatSlider);
                mTvRepeatSelect.setVisibility(View.VISIBLE);
                break;
            case REVERSE_EFFECT:
                showReverseLayout();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCTimeViewInfoManager.getInstance().setCurrentEffect(mCurrentEffect, mCurrentEffectStartMs);
    }

    private void initViews(@NonNull View view) {
        noTimeMotionGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectNormalIcon, R.drawable.motion_time_normal);
        slowMotionGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectSlowMotionIcon, R.drawable.motion_time_slow);
        repeatGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectRepeatIcon, R.drawable.motion_time_repeat);
        reverseGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectReverseIcon, R.drawable.motion_time_reverse);

        mTvCancelSelect = (CircleImageView) view.findViewById(R.id.time_tv_cancel_select);
        mTvCancel = (ImageView) view.findViewById(R.id.time_tv_cancel);
        mTvCancel.setOnClickListener(this);
        mTvSpeedSelect = (CircleImageView) view.findViewById(R.id.time_tv_speed_select);
        mTvSpeed = (ImageView) view.findViewById(R.id.time_tv_speed);
        mTvSpeed.setOnClickListener(this);
        mTvSpeed.setSelected(true);
        mTvRepeatSelect = (CircleImageView) view.findViewById(R.id.time_tv_repeat_select);
        mTvRepeat = (ImageView) view.findViewById(R.id.time_tv_repeat);
        mTvRepeat.setOnClickListener(this);
        mTvReverseSelect = (CircleImageView) view.findViewById(R.id.time_tv_reverse_select);
        mTvReverse = (ImageView) view.findViewById(R.id.time_tv_reverse);
        mTvReverse.setOnClickListener(this);

        Glide.with(this).load(noTimeMotionGif).into(mTvCancel);
        Glide.with(this).load(slowMotionGif).into(mTvSpeed);
        Glide.with(this).load(repeatGif).into(mTvRepeat);
        Glide.with(this).load(reverseGif).into(mTvReverse);

        mTvCancelSelect.setBackgroundResource(coverIcon);
        mTvSpeedSelect.setBackgroundResource(coverIcon);
        mTvRepeatSelect.setBackgroundResource(coverIcon);
        mTvReverseSelect.setBackgroundResource(coverIcon);
    }

    private void setRepeatSliderView() {
        if (mRepeatSlider == null) {
            // 第一次展示重复界面的时候，将重复的按钮移动到当前的进度
            long currentPts = mVideoProgressController.getCurrentTimeMs();

            setRepeatList(currentPts);

            PlayerManagerKit.getInstance().previewAtTime(currentPts);

            mRepeatSlider = new SliderViewContainer(getContext());
            mRepeatSlider.setStartTimeMs(currentPts);
            mCurrentEffectStartMs = currentPts;
            mRepeatSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mCurrentEffect != REPEAT_EFFECT) {
                        cancelSetEffect();
                    }
                    mCurrentEffect = REPEAT_EFFECT;

                    setRepeatList(timeMs);

                    PlayerManagerKit.getInstance().previewAtTime(timeMs);

                    // 进度条移动到当前位置
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                    mCurrentEffectStartMs = timeMs;
                }
            });
            mVideoProgressController.addSliderView(mRepeatSlider);
            mRepeatSlider.setVisibility(View.GONE);
        } else {
            long currentPts = mVideoProgressController.getCurrentTimeMs();

            setRepeatList(currentPts);
            PlayerManagerKit.getInstance().previewAtTime(currentPts);
            mRepeatSlider.setStartTimeMs(currentPts);
            mCurrentEffectStartMs = currentPts;
        }
    }

    private void setRepeatList(long currentPts) {
        List<TXVideoEditConstants.TXRepeat> repeatList = new ArrayList<>();
        TXVideoEditConstants.TXRepeat repeat = new TXVideoEditConstants.TXRepeat();
        repeat.startTime = currentPts;
        repeat.endTime = currentPts + DEAULT_DURATION_MS;
        repeat.repeatTimes = 3;
        repeatList.add(repeat);
        mTXVideoEditer.setRepeatPlay(repeatList);
    }


    private void initSpeedLayout() {
        if (mSpeedSlider == null) {
            long currentPts = mVideoProgressController.getCurrentTimeMs();
            setSpeed(currentPts);
            mCurrentEffect = SPEED_EFFECT;
            mVideoProgressController.setCurrentTimeMs(currentPts);

            mSpeedSlider = new SliderViewContainer(getContext());
            mSpeedSlider.setStartTimeMs(currentPts);
            mCurrentEffectStartMs = currentPts;
            mSpeedSlider.setOnStartTimeChangedListener(new SliderViewContainer.OnStartTimeChangedListener() {
                @Override
                public void onStartTimeMsChanged(long timeMs) {
                    if (mCurrentEffect != SPEED_EFFECT)
                        cancelSetEffect();
                    mCurrentEffect = SPEED_EFFECT;
                    setSpeed(timeMs);

                    PlayerManagerKit.getInstance().previewAtTime(timeMs);
                    // 进度条移动到当前位置
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                    mCurrentEffectStartMs = timeMs;
                }
            });
            mVideoProgressController.addSliderView(mSpeedSlider);
        } else {
            long currentPts = mVideoProgressController.getCurrentTimeMs();
            setSpeed(currentPts);
            mCurrentEffect = SPEED_EFFECT;
            PlayerManagerKit.getInstance().previewAtTime(currentPts);
            mSpeedSlider.setStartTimeMs(currentPts);
            mVideoProgressController.setCurrentTimeMs(currentPts);
            mCurrentEffectStartMs = currentPts;
        }
    }

    /**
     * SDK拥有支持多段变速的功能。 在DEMO仅展示一段慢速播放
     *
     * @param startTime
     */
    private void setSpeed(long startTime) {
        List<TXVideoEditConstants.TXSpeed> list = new ArrayList<>();
        TXVideoEditConstants.TXSpeed speed1 = new TXVideoEditConstants.TXSpeed();
        speed1.startTime = startTime;                                                    // 开始时间
        speed1.endTime = startTime + 500;
        speed1.speedLevel = TXVideoEditConstants.SPEED_LEVEL_SLOW;                       // 慢速
        list.add(speed1);

        TXVideoEditConstants.TXSpeed speed2 = new TXVideoEditConstants.TXSpeed();
        speed2.startTime = startTime + 500;                                              // 开始时间
        speed2.endTime = startTime + 1000;
        speed2.speedLevel = TXVideoEditConstants.SPEED_LEVEL_SLOWEST;                    // 极慢速
        list.add(speed2);

        TXVideoEditConstants.TXSpeed speed3 = new TXVideoEditConstants.TXSpeed();
        speed3.startTime = startTime + 1000;                                             // 开始时间
        speed3.endTime = startTime + 1500;
        speed3.speedLevel = TXVideoEditConstants.SPEED_LEVEL_SLOW;                       // 极速
        list.add(speed3);

        // 设入SDK
        mTXVideoEditer.setSpeedList(list);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mRepeatSlider != null && mTvRepeat != null && mTvRepeat.isSelected()) {
            mRepeatSlider.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.time_tv_cancel) {
            cancelSetEffect();
            showNoneLayout();

            PlayerManagerKit.getInstance().restartPlay();
        } else if (i == R.id.time_tv_speed) {
            cancelSetEffect();
            showSpeedLayout();

        } else if (i == R.id.time_tv_reverse) {
            if (mCurrentEffect == REVERSE_EFFECT) return;// 当前处于倒放状态 无视
            cancelSetEffect();
            showReverseLayout();
            mTXVideoEditer.setReverse(true);
            mCurrentEffect = REVERSE_EFFECT;
            VideoEditerSDK.getInstance().setReverse(true);
            PlayerManagerKit.getInstance().restartPlay();
        } else if (i == R.id.time_tv_repeat) {
            cancelSetEffect();
            showRepeatLayout();
        }
    }


    /**
     * 取消设置了的时间特效
     */
    private void cancelSetEffect() {
        switch (mCurrentEffect) {
            case SPEED_EFFECT:
                cancelSpeedEffect();
                break;
            case REPEAT_EFFECT:
                cancelRepeatEffect();
                break;
            case REVERSE_EFFECT:
                cancelReverseEffect();
                break;
        }
    }

    private void cancelSpeedEffect() {
        mCurrentEffect = NONE_EFFECT;
        mTXVideoEditer.setSpeedList(null);
    }

    private void cancelRepeatEffect() {
        mCurrentEffect = NONE_EFFECT;
        mTXVideoEditer.setRepeatPlay(null);
    }

    private void cancelReverseEffect() {
        mCurrentEffect = NONE_EFFECT;
        mTXVideoEditer.setReverse(false);
        VideoEditerSDK.getInstance().setReverse(false);
    }

    private void showNoneLayout() {
        mTvCancel.setSelected(true);
        mTvSpeed.setSelected(false);
        mTvRepeat.setSelected(false);
        mTvReverse.setSelected(false);
        if (mRepeatSlider != null)
            mRepeatSlider.setVisibility(View.GONE);
        if (mSpeedSlider != null) {
            mSpeedSlider.setVisibility(View.GONE);
        }
        mTvCancelSelect.setVisibility(View.VISIBLE);
        mTvSpeedSelect.setVisibility(View.INVISIBLE);
        mTvRepeatSelect.setVisibility(View.INVISIBLE);
        mTvReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showSpeedLayout() {
        initSpeedLayout();
        mTvSpeed.setSelected(true);
        mTvRepeat.setSelected(false);
        mTvCancel.setSelected(false);
        mTvReverse.setSelected(false);
        if (mRepeatSlider != null)
            mRepeatSlider.setVisibility(View.GONE);
        if (mSpeedSlider.getVisibility() == View.GONE) {
            mSpeedSlider.setVisibility(View.VISIBLE);
        }
        mTvCancelSelect.setVisibility(View.INVISIBLE);
        mTvSpeedSelect.setVisibility(View.VISIBLE);
        mTvRepeatSelect.setVisibility(View.INVISIBLE);
        mTvReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showRepeatLayout() {
        setRepeatSliderView();
        mTvCancel.setSelected(false);
        mTvSpeed.setSelected(false);
        mTvRepeat.setSelected(true);
        mTvReverse.setSelected(false);
        if (mSpeedSlider != null && mSpeedSlider.getVisibility() == View.VISIBLE) {
            mSpeedSlider.setVisibility(View.GONE);
        }
        if (mRepeatSlider.getVisibility() == View.GONE) {
            mRepeatSlider.setVisibility(View.VISIBLE);
        }
        mCurrentEffect = REPEAT_EFFECT;

        mTvCancelSelect.setVisibility(View.INVISIBLE);
        mTvSpeedSelect.setVisibility(View.INVISIBLE);
        mTvRepeatSelect.setVisibility(View.VISIBLE);
        mTvReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showReverseLayout() {
        mTvSpeed.setSelected(false);
        mTvCancel.setSelected(false);
        mTvRepeat.setSelected(false);
        mTvReverse.setSelected(true);
        if (mRepeatSlider != null)
            mRepeatSlider.setVisibility(View.GONE);
        if (mSpeedSlider != null) {
            mSpeedSlider.setVisibility(View.GONE);
        }
        mTvCancelSelect.setVisibility(View.INVISIBLE);
        mTvSpeedSelect.setVisibility(View.INVISIBLE);
        mTvRepeatSelect.setVisibility(View.INVISIBLE);
        mTvReverseSelect.setVisibility(View.VISIBLE);
    }

}

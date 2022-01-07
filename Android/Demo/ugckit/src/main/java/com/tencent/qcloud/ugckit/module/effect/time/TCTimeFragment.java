package com.tencent.qcloud.ugckit.module.effect.time;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import com.tencent.qcloud.ugckit.basic.JumpActivityMgr;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.TimeLineView;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditConstants.TXGenerateResult;
import com.tencent.ugc.TXVideoEditer;

import com.tencent.ugc.TXVideoEditer.TXVideoProcessListener;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 时间特效的Fragment
 */
public class TCTimeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG                = "TCTimeFragment";
    public static final  long DEAULT_REPEAT_DURATION_MS = 1000; //默认重复时间段1s
    public static final  long DEAULT_SPEED_DURATION_MS = 1500; //默认速度时间段1.5s

    private ImageView                       mImageCancel;
    private ImageView                       mImageSpeed;
    private ImageView                       mImageRepeat;
    private ImageView                       mImageReverse;
    private CircleImageView                 mCircleImageCancelSelect;
    private CircleImageView                 mCircleSpeedSelect;
    private CircleImageView                 mCircleImageRepeatSelect;
    private CircleImageView                 mCircleImageReverseSelect;
    private TXVideoEditer                   mTXVideoEditer;
    private long                            mCurrentEffectStartMs;
    private int                             mCurrentEffect  = TimeEffect.NONE_EFFECT;
    private int                             noTimeMotionGif = R.drawable.ugckit_motion_time_normal;
    private int                             slowMotionGif   = R.drawable.ugckit_motion_time_slow;
    private int                             repeatGif       = R.drawable.ugckit_motion_time_repeat;
    private int                             reverseGif      = R.drawable.ugckit_motion_time_reverse;
    private int                             coverIcon       = R.drawable.ugckit_ic_effect5;
    private TimeLineView.OnTimeLineListener mListener;

    // 倒放跟反复特效需要全I帧视频，这里为等到全I帧视频的状态
    private boolean mIsProcessing = false; //是否正在转全I帧
    private boolean mNeedReload = false; //是否需要重新加载全I帧视频
    private View mRootView;
    private View mProgressLayer;
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ugckit_fragment_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        VideoEditerSDK wrapper = VideoEditerSDK.getInstance();
        mTXVideoEditer = wrapper.getEditer();

        initViews(view);
        initEffect();
    }

    private void initEffect() {
        mCurrentEffect = TCTimeViewInfoManager.getInstance().getCurrentEffect();
        mCurrentEffectStartMs = TCTimeViewInfoManager.getInstance().getCurrentStartMs();

        switch (mCurrentEffect) {
            case TimeEffect.NONE_EFFECT:
                showNoneLayout();
                break;
            case TimeEffect.SPEED_EFFECT:
                if (mListener != null) {
                    mListener.onAddSlider(TimeEffect.SPEED_EFFECT, mCurrentEffectStartMs);
                }
                mCircleSpeedSelect.setVisibility(View.VISIBLE);
                break;
            case TimeEffect.REPEAT_EFFECT:
                if (mListener != null) {
                    mListener.onAddSlider(TimeEffect.REPEAT_EFFECT, mCurrentEffectStartMs);
                }
                mCircleImageRepeatSelect.setVisibility(View.VISIBLE);
                break;
            case TimeEffect.REVERSE_EFFECT:
                showReverseLayout();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCTimeViewInfoManager.getInstance().setCurrentEffect(mCurrentEffect, mCurrentEffectStartMs);
        VideoEditerSDK.getInstance().getEditer().setVideoProcessListener(null);
    }

    private void initViews(@NonNull View view) {
        mRootView = view;
        noTimeMotionGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectNormalIcon, R.drawable.ugckit_motion_time_normal);
        slowMotionGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectSlowMotionIcon, R.drawable.ugckit_motion_time_slow);
        repeatGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectRepeatIcon, R.drawable.ugckit_motion_time_repeat);
        reverseGif = UIAttributeUtil.getResResources(getContext(), R.attr.editerTimeEffectReverseIcon, R.drawable.ugckit_motion_time_reverse);

        mCircleImageCancelSelect = (CircleImageView) view.findViewById(R.id.time_tv_cancel_select);
        mImageCancel = (ImageView) view.findViewById(R.id.time_tv_cancel);
        mImageCancel.setOnClickListener(this);
        mCircleSpeedSelect = (CircleImageView) view.findViewById(R.id.time_tv_speed_select);
        mImageSpeed = (ImageView) view.findViewById(R.id.time_tv_speed);
        mImageSpeed.setOnClickListener(this);
        mImageSpeed.setSelected(true);
        mCircleImageRepeatSelect = (CircleImageView) view.findViewById(R.id.time_tv_repeat_select);
        mImageRepeat = (ImageView) view.findViewById(R.id.time_tv_repeat);
        mImageRepeat.setOnClickListener(this);
        mCircleImageReverseSelect = (CircleImageView) view.findViewById(R.id.time_tv_reverse_select);
        mImageReverse = (ImageView) view.findViewById(R.id.time_tv_reverse);
        mImageReverse.setOnClickListener(this);

        Glide.with(this).load(noTimeMotionGif).into(mImageCancel);
        Glide.with(this).load(slowMotionGif).into(mImageSpeed);
        Glide.with(this).load(repeatGif).into(mImageRepeat);
        Glide.with(this).load(reverseGif).into(mImageReverse);

        mCircleImageCancelSelect.setBackgroundResource(coverIcon);
        mCircleSpeedSelect.setBackgroundResource(coverIcon);
        mCircleImageRepeatSelect.setBackgroundResource(coverIcon);
        mCircleImageReverseSelect.setBackgroundResource(coverIcon);

        boolean quickImport = JumpActivityMgr.getInstance().isQuickImport();
        RelativeLayout layoutRepeat = (RelativeLayout) view.findViewById(R.id.layout_repeat);
        RelativeLayout layoutReverse = (RelativeLayout) view.findViewById(R.id.layout_reverse);
        if (quickImport) {
            layoutRepeat.setVisibility(View.GONE);
            layoutReverse.setVisibility(View.GONE);
        } else {
            layoutRepeat.setVisibility(View.VISIBLE);
            layoutReverse.setVisibility(View.VISIBLE);
        }
        checkPorgressLoadding();
    }

    private void initRepeatLayout() {
        long currentTime = 0;
        if (mListener != null) {
            currentTime = mListener.getCurrentTime();
            mListener.onAddSlider(TimeEffect.REPEAT_EFFECT, currentTime);
        }
        setRepeatList(currentTime);
        PlayerManagerKit.getInstance().previewAtTime(currentTime);
        mCurrentEffectStartMs = currentTime;

        if (mListener != null) {
            mListener.setCurrentTime(currentTime);
        }
    }

    private void setRepeatList(long currentPts) {
        List<TXVideoEditConstants.TXRepeat> repeatList = new ArrayList<>();
        TXVideoEditConstants.TXRepeat repeat = new TXVideoEditConstants.TXRepeat();
        repeat.startTime = currentPts;
        repeat.endTime = currentPts + DEAULT_REPEAT_DURATION_MS;
        repeat.repeatTimes = 3;
        repeatList.add(repeat);
        mTXVideoEditer.setRepeatPlay(repeatList);
    }

    private void initSpeedLayout() {
        long currentTime = 0;
        if (mListener != null) {
            currentTime = mListener.getCurrentTime();
            mListener.onAddSlider(TimeEffect.SPEED_EFFECT, currentTime);
        }
        setSpeed(currentTime);
        PlayerManagerKit.getInstance().previewAtTime(currentTime);
        mCurrentEffectStartMs = currentTime;

        if (mListener != null) {
            mListener.setCurrentTime(currentTime);
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
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.time_tv_cancel) {
            if (mCurrentEffect == TimeEffect.NONE_EFFECT) {
                return;
            }
            cancelSetEffect();
            mCurrentEffect = TimeEffect.NONE_EFFECT;
            cancleLoadding();
            showNoneLayout();
            PlayerManagerKit.getInstance().restartPlay();
        } else if (i == R.id.time_tv_speed) {
            if (mCurrentEffect == TimeEffect.SPEED_EFFECT) {
                return;
            }
            cancelSetEffect();
            mCurrentEffect = TimeEffect.SPEED_EFFECT;
            cancleLoadding();
            showSpeedLayout();
            PlayerManagerKit.getInstance().restartPlay();
        } else if (i == R.id.time_tv_reverse) {
            // 当前处于倒放状态 无视
            if (mCurrentEffect == TimeEffect.REVERSE_EFFECT) {
                return;
            }
            cancelSetEffect();
            mCurrentEffect = TimeEffect.REVERSE_EFFECT;
            if (mIsProcessing) {
                showReverseLayout();
                showLoadding();
                return;
            }
            showReverseLayout();
            setReverse();
        } else if (i == R.id.time_tv_repeat) {
            if (mCurrentEffect == TimeEffect.REPEAT_EFFECT) {
                return;
            }
            cancelSetEffect();
            mCurrentEffect = TimeEffect.REPEAT_EFFECT;
            if (mIsProcessing) {
                showRepeatLayout();
                showLoadding();
                return;
            }
            showRepeatLayout();
            setRepeat();
        }
    }

    private void setRepeat() {
        if (mNeedReload && mListener != null) {
            mListener.onRefresh();
            mNeedReload = false;
        }
        PlayerManagerKit.getInstance().restartPlay();
    }

    private void setReverse() {
        mTXVideoEditer.setReverse(true);
        VideoEditerSDK.getInstance().setReverse(true);
        if (mNeedReload && mListener != null) {
            mListener.onRefresh();
            mNeedReload = false;
        }
        PlayerManagerKit.getInstance().restartPlay();
    }

    private boolean checkPorgressLoadding() {
        if (mRootView == null) {
            return false;
        }
        if (VideoEditerSDK.getInstance().getVideoSourcePath() != null
                && VideoEditerSDK.getInstance().getVideoProcessPath() != null
                && !VideoEditerSDK.getInstance().getVideoSourcePath()
                .equals(VideoEditerSDK.getInstance().getVideoProcessPath())) {
            mNeedReload = true;
            mIsProcessing = true;
            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            editer.setVideoProcessListener(processListener);
            return true;
        }
        return false;
    }


    private void showLoadding(){
        PlayerManagerKit.getInstance().stopPlay();
        mProgressLayer = mRootView.findViewById(R.id.progressbar_layer);
        mProgressLayer.setVisibility(View.VISIBLE);
    }

    private boolean cancleLoadding() {
        if (mProgressLayer == null || mProgressLayer.getVisibility() == View.GONE) {
            return false;
        }
        mProgressLayer.setVisibility(View.GONE);
        return true;
    }

    // 转全I帧监听
    TXVideoProcessListener processListener = new TXVideoProcessListener() {

        @Override
        public void onProcessProgress(float progress) {
            if (mProgressLayer == null || mProgressLayer.getVisibility() == View.GONE) {
                return;
            }
            if (mProgressBar == null) {
                mProgressBar = mProgressLayer.findViewById(R.id.progressbar);
            }
            mProgressBar.setProgress((int) (progress * 100));
        }

        @Override
        public void onProcessComplete(TXGenerateResult result) {
            Log.i(TAG, "onProcessComplete: ");
            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            if (editer == null) {
                return;
            }
            editer.setVideoProcessListener(null);
            //在倒放功能下就reload
            if (cancleLoadding()) {
                if (mCurrentEffect == TimeEffect.REVERSE_EFFECT) {
                    setReverse();
                } else if (mCurrentEffect == TimeEffect.REPEAT_EFFECT) {
                    setRepeat();
                }
            }
            mIsProcessing = false;
        }
    };


    /**
     * 取消设置了的时间特效
     */
    private void cancelSetEffect() {
        switch (mCurrentEffect) {
            case TimeEffect.SPEED_EFFECT:
                cancelSpeedEffect();
                break;
            case TimeEffect.REPEAT_EFFECT:
                cancelRepeatEffect();
                break;
            case TimeEffect.REVERSE_EFFECT:
                cancelReverseEffect();
                break;
        }
    }

    private void cancelSpeedEffect() {
        mTXVideoEditer.setSpeedList(null);
    }

    private void cancelRepeatEffect() {
        mTXVideoEditer.setRepeatPlay(null);
    }

    private void cancelReverseEffect() {
        mTXVideoEditer.setReverse(false);
        VideoEditerSDK.getInstance().setReverse(false);
    }

    private void showNoneLayout() {
        mImageCancel.setSelected(true);
        mImageSpeed.setSelected(false);
        mImageRepeat.setSelected(false);
        mImageReverse.setSelected(false);

        if (mListener != null) {
            mListener.onRemoveSlider(TimeEffect.SPEED_EFFECT);
            mListener.onRemoveSlider(TimeEffect.REPEAT_EFFECT);
        }
        mCircleImageCancelSelect.setVisibility(View.VISIBLE);
        mCircleSpeedSelect.setVisibility(View.INVISIBLE);
        mCircleImageRepeatSelect.setVisibility(View.INVISIBLE);
        mCircleImageReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showSpeedLayout() {
        initSpeedLayout();
        mImageSpeed.setSelected(true);
        mImageRepeat.setSelected(false);
        mImageCancel.setSelected(false);
        mImageReverse.setSelected(false);
        if (mListener != null) {
            mListener.onRemoveSlider(TimeEffect.REPEAT_EFFECT);
        }
        mCircleImageCancelSelect.setVisibility(View.INVISIBLE);
        mCircleSpeedSelect.setVisibility(View.VISIBLE);
        mCircleImageRepeatSelect.setVisibility(View.INVISIBLE);
        mCircleImageReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showRepeatLayout() {
        initRepeatLayout();
        mImageCancel.setSelected(false);
        mImageSpeed.setSelected(false);
        mImageRepeat.setSelected(true);
        mImageReverse.setSelected(false);
        if (mListener != null) {
            mListener.onRemoveSlider(TimeEffect.SPEED_EFFECT);
        }
        mCircleImageCancelSelect.setVisibility(View.INVISIBLE);
        mCircleSpeedSelect.setVisibility(View.INVISIBLE);
        mCircleImageRepeatSelect.setVisibility(View.VISIBLE);
        mCircleImageReverseSelect.setVisibility(View.INVISIBLE);
    }

    private void showReverseLayout() {
        mImageSpeed.setSelected(false);
        mImageCancel.setSelected(false);
        mImageRepeat.setSelected(false);
        mImageReverse.setSelected(true);
        if (mListener != null) {
            mListener.onRemoveSlider(TimeEffect.REPEAT_EFFECT);
            mListener.onRemoveSlider(TimeEffect.SPEED_EFFECT);
        }
        mCircleImageCancelSelect.setVisibility(View.INVISIBLE);
        mCircleSpeedSelect.setVisibility(View.INVISIBLE);
        mCircleImageRepeatSelect.setVisibility(View.INVISIBLE);
        mCircleImageReverseSelect.setVisibility(View.VISIBLE);
    }

    public void setOnTimeLineListener(TimeLineView.OnTimeLineListener listener) {
        mListener = listener;
    }

    public void onTimeChange(int type, long timeMs) {
        switch (type) {
            case TimeEffect.REPEAT_EFFECT:
                if (mCurrentEffect != TimeEffect.REPEAT_EFFECT) {
                    cancelSetEffect();
                }
                mCurrentEffect = TimeEffect.REPEAT_EFFECT;

                setRepeatList(timeMs);

                PlayerManagerKit.getInstance().previewAtTime(timeMs);
                if (mListener != null) {
                    mListener.setCurrentTime(timeMs);
                }
                mCurrentEffectStartMs = timeMs;
                break;
            case TimeEffect.SPEED_EFFECT:
                if (mCurrentEffect != TimeEffect.SPEED_EFFECT) {
                    cancelSetEffect();
                }
                mCurrentEffect = TimeEffect.SPEED_EFFECT;
                setSpeed(timeMs);

                PlayerManagerKit.getInstance().previewAtTime(timeMs);
                if (mListener != null) {
                    mListener.setCurrentTime(timeMs);
                }
                mCurrentEffectStartMs = timeMs;
                break;
        }

    }
}

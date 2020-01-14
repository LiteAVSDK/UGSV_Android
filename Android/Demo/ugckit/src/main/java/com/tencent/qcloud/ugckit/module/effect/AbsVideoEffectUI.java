package com.tencent.qcloud.ugckit.module.effect;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.module.cut.VideoPlayLayout;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;
import com.tencent.qcloud.ugckit.component.floatlayer.FloatLayerViewGroup;
import com.tencent.qcloud.ugckit.module.effect.bgm.TCMusicSettingFragment;
import com.tencent.qcloud.ugckit.module.effect.bubble.TCBubbleSubtitleFragment;
import com.tencent.qcloud.ugckit.module.effect.bubble.BubbleSubtitlePannel;
import com.tencent.qcloud.ugckit.module.effect.filter.TCStaticFilterFragment;
import com.tencent.qcloud.ugckit.module.effect.motion.TCMotionFragment;
import com.tencent.qcloud.ugckit.module.effect.paster.TCPasterFragment;
import com.tencent.qcloud.ugckit.module.effect.paster.view.PasterPannel;
import com.tencent.qcloud.ugckit.module.effect.time.TCTimeFragment;

public abstract class AbsVideoEffectUI extends RelativeLayout implements IVideoEffectKit {
    private TitleBarLayout mTitleBar;
    private VideoPlayLayout mVideoPlayLayout;

    private TimeLineView mTimeLineView;
    private PlayControlLayout mPlayControlLayout;

    private FloatLayerViewGroup mBubbleContainer;
    private FloatLayerViewGroup mPasterContainer;

    private PasterPannel mPasterSelectView;
    private BubbleSubtitlePannel mBubbleSettingView;

    private Fragment mCurrentFragment;
    private TCTimeFragment mTimeFragment;
    private TCStaticFilterFragment mStaticFilterFragment;
    private TCMotionFragment mMotionFragment;
    private TCPasterFragment mPasterFragment;
    private TCBubbleSubtitleFragment mBubbleFragment;
    private TCMusicSettingFragment mMusicFragment;
    private TimeLineView.OnTimeLineListener mOnTimeLineListener = new TimeLineView.OnTimeLineListener() {
        @Override
        public void onAddSlider(int type, long startEffectTime) {
            if (mTimeLineView != null) {
                mTimeLineView.onAddSlider(type, startEffectTime);
            }
        }

        @Override
        public void onRemoveSlider(int type) {
            if (mTimeLineView != null) {
                mTimeLineView.onRemoveSlider(type);
            }
        }

        @Override
        public long getCurrentTime() {
            if (mTimeLineView != null) {
                return mTimeLineView.getCurrentTime();
            }
            return 0;
        }

        @Override
        public void setCurrentTime(long time) {
            if (mTimeLineView != null) {
                mTimeLineView.setCurrentTime(time);
            }
        }
    };

    public AbsVideoEffectUI(Context context) {
        super(context);
        initViews();
    }

    public AbsVideoEffectUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsVideoEffectUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.video_eff_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mVideoPlayLayout = (VideoPlayLayout) findViewById(R.id.video_play_layout);

        mPlayControlLayout = (PlayControlLayout) findViewById(R.id.play_control_layout);
        mTimeLineView = (TimeLineView) findViewById(R.id.timeline_view);
        mTimeLineView.setOnTimeChangeListener(new TimeLineView.OnTimeChangeListener() {
            @Override
            public void onTimeChange(int type, long time) {
                if (mTimeFragment != null) {
                    mTimeFragment.onTimeChange(type, time);
                }
            }
        });

        mBubbleContainer = (FloatLayerViewGroup) findViewById(R.id.bubble_container);
        mPasterContainer = (FloatLayerViewGroup) findViewById(R.id.paster_container);
        mPasterSelectView = (PasterPannel) findViewById(R.id.paster_select_view);
        mBubbleSettingView = (BubbleSubtitlePannel) findViewById(R.id.bubble_setting_view);

        mTimeFragment = new TCTimeFragment();
        mTimeFragment.setOnTimeLineListener(mOnTimeLineListener);

        mStaticFilterFragment = new TCStaticFilterFragment();
        mMotionFragment = new TCMotionFragment();
        mPasterFragment = new TCPasterFragment();
        mBubbleFragment = new TCBubbleSubtitleFragment();
        mMusicFragment = new TCMusicSettingFragment();
    }

    public TimeLineView getTimelineView() {
        return mTimeLineView;
    }

    public PlayControlLayout getPlayControlLayout() {
        return mPlayControlLayout;
    }

    public VideoPlayLayout getVideoPlayLayout() {
        return mVideoPlayLayout;
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    public TCTimeFragment getTimeFragment() {
        return mTimeFragment;
    }

    public TCStaticFilterFragment getStaticFilterFragment() {
        return mStaticFilterFragment;
    }

    public TCMotionFragment getMotionFragment() {
        return mMotionFragment;
    }

    public TCPasterFragment getPasterFragment() {
        return mPasterFragment;
    }

    public TCBubbleSubtitleFragment getBubbleFragment() {
        return mBubbleFragment;
    }

    public TCMusicSettingFragment getMusicFragment() {
        return mMusicFragment;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public void setCurrentFragment(Fragment fragment) {
        mCurrentFragment = fragment;
    }

}

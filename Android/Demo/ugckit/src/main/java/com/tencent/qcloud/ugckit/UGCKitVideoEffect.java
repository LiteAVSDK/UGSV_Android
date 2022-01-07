package com.tencent.qcloud.ugckit;

import android.app.KeyguardManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;


import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.AbsVideoEffectUI;
import com.tencent.qcloud.ugckit.module.effect.ConfigureLoader;
import com.tencent.qcloud.ugckit.module.effect.TimelineViewUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.TelephonyUtil;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.qcloud.ugckit.module.effect.utils.DraftEditer;

public class UGCKitVideoEffect extends AbsVideoEffectUI implements VideoProgressController.VideoProgressSeekListener {

    private FragmentActivity      mActivity;
    private int                   mConfirmIcon;
    private OnVideoEffectListener mOnVideoEffectListener;

    public UGCKitVideoEffect(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoEffect(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoEffect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    private void initDefault() {
        mActivity = (FragmentActivity) getContext();
        // 当上个界面为裁剪界面，此时重置裁剪的开始时间和结束时间
        VideoEditerSDK.getInstance().resetDuration();
        // 加载草稿配置
        ConfigureLoader.getInstance().loadConfigToDraft();

        TelephonyUtil.getInstance().initPhoneListener();

        initTitlebar();

        preivewVideo();
    }

    private void preivewVideo() {
        // 初始化图片时间轴
        getTimelineView().initVideoProgressLayout();
        // 初始化播放器
        getVideoPlayLayout().initPlayerLayout();
        // 开始播放
        PlayerManagerKit.getInstance().startPlay();
    }

    private void initTitlebar() {
        mConfirmIcon = UIAttributeUtil.getResResources(mActivity, R.attr.editerConfirmIcon, R.drawable.ugckit_ic_edit_effect_confirm_selector);
        getTitleBar().getRightButton().setBackgroundResource(mConfirmIcon);
        getTitleBar().getRightButton().setText("");
        getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击"返回",清除当前设置的视频特效
                DraftEditer.getInstance().clear();
                // 还原已经设置给SDK的特效
                VideoEditerSDK.getInstance().restore();

                PlayerManagerKit.getInstance().stopPlay();

                if (mOnVideoEffectListener != null) {
                    mOnVideoEffectListener.onEffectCancel();
                }
            }
        });
        getTitleBar().setOnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清理时间轴的滑动事件，防止与上一级页面的播放状态冲突
                getTimelineView().getVideoProgressView().getRecyclerView().clearOnScrollListeners();

                // 点击"完成"，应用当前设置的视频特效
                ConfigureLoader.getInstance().saveConfigFromDraft();

                PlayerManagerKit.getInstance().stopPlay();

                if (mOnVideoEffectListener != null) {
                    mOnVideoEffectListener.onEffectApply();
                }
            }
        });
    }

    @Override
    public void start() {
        KeyguardManager manager = (KeyguardManager) UGCKit.getAppContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (!manager.inKeyguardRestrictedInputMode()) {
            PlayerManagerKit.getInstance().restartPlay();
        }
    }

    @Override
    public void stop() {
        PlayerManagerKit.getInstance().stopPlay();
    }

    @Override
    public void release() {
        PlayerManagerKit.getInstance().removeAllPreviewListener();
        PlayerManagerKit.getInstance().removeAllPlayStateListener();
        TelephonyUtil.getInstance().uninitPhoneListener();
        TimelineViewUtil.getInstance().release();
    }

    @Override
    public void setEffectType(int type) {
        TimelineViewUtil.getInstance().setTimelineView(getTimelineView());
        getPlayControlLayout().updateUIByFragment(type);
        getTimelineView().updateUIByFragment(type);
        showFragmentByType(type);
    }

    @Override
    public void setOnVideoEffectListener(OnVideoEffectListener listener) {
        mOnVideoEffectListener = listener;
    }

    @Override
    public void backPressed() {
        DraftEditer.getInstance().clear();
        PlayerManagerKit.getInstance().stopPlay();

        if (mOnVideoEffectListener != null) {
            mOnVideoEffectListener.onEffectCancel();
        }
    }

    @Override
    public void onVideoProgressSeek(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    @Override
    public void onVideoProgressSeekFinish(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    public void showFragmentByType(int type) {
        switch (type) {
            case UGCKitConstants.TYPE_EDITER_BGM:
                showFragment(getMusicFragment(), "bgm_setting_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_MOTION:
                showFragment(getMotionFragment(), "motion_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_SPEED:
                showFragment(getTimeFragment(), "time_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_FILTER:
                showFragment(getStaticFilterFragment(), "static_filter_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_PASTER:
                showFragment(getPasterFragment(), "paster_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_SUBTITLE:
                showFragment(getBubbleFragment(), "bubble_fragment");
                break;
            case UGCKitConstants.TYPE_EDITER_TRANSITION:
                showFragment(getTransitionFragment(), "transition_fragment");
                break;
        }
    }

    private void showFragment(@NonNull Fragment fragment, String tag) {
        if (fragment == getCurrentFragment()) return;
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (getCurrentFragment() != null) {
            transaction.hide(getCurrentFragment());
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_layout, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        setCurrentFragment(fragment);
        transaction.commit();
    }
}

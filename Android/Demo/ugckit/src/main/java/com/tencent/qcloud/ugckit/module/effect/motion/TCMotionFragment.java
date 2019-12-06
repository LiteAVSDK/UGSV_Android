package com.tencent.qcloud.ugckit.module.effect.motion;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.tencent.liteav.basic.log.TXCLog;

import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.TimeLineView;
import com.tencent.qcloud.ugckit.module.effect.TimelineViewUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.timeline.ColorfulProgress;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态滤镜特效的设置Fragment
 */
public class TCMotionFragment extends AbsMotionFragment implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "TCMotionFragment";

    private boolean mIsOnTouch; // 是否已经有按下的
    private TXVideoEditer mTXVideoEditer;

    private ColorfulProgress mColorfulProgress;
    private VideoProgressController mVideoProgressController;
    private ImageView mIvUndo;
    private boolean mStartMark;
    private Map<Integer, TCMotionItem> mMotionMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_motion, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoEditerSDK wrapper = VideoEditerSDK.getInstance();
        mTXVideoEditer = wrapper.getEditer();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimeLineView timeLineView = TimelineViewUtil.getInstance().getTimeLineView();
        if (timeLineView != null) {
            mVideoProgressController = timeLineView.getVideoProgressController();
        }
        initViews(getView());
        setDefaultValue(getActivity(),getView());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mColorfulProgress != null) {
            mColorfulProgress.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        List<ColorfulProgress.MarkInfo> markInfoList = mColorfulProgress.getMarkInfoList();
        TCMotionViewInfoManager.getInstance().setMarkInfoList(markInfoList);
    }

    private void initViews(@NonNull View view) {
        mIvUndo = (ImageView) view.findViewById(R.id.iv_undo);
        mIvUndo.setOnClickListener(this);

        mColorfulProgress = new ColorfulProgress(getContext());
        mColorfulProgress.setWidthHeight(mVideoProgressController.getThumbnailPicListDisplayWidth(), getResources().getDimensionPixelOffset(R.dimen.video_progress_height));
        mColorfulProgress.setMarkInfoList(TCMotionViewInfoManager.getInstance().getMarkInfoList());
        mVideoProgressController.addColorfulProgress(mColorfulProgress);
    }

    private void setDefaultValue(Context context, View view) {
        soulOutGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionSoulOutIcon, R.drawable.motion_soul_out);
        splitScreenGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionSplitScreenIcon, R.drawable.motion_split_screen);
        rockLightGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionRockLightIcon, R.drawable.motion_rock_light);
        darkDreamGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionDarkDreamIcon, R.drawable.motion_dark_dream);
        winShadowGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionWinShadowIcon, R.drawable.motion_win_shaddow);
        ghostShadowGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionGhostIcon, R.drawable.motion_ghost);
        phantomShadowGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionPhantomShadowIcon, R.drawable.motion_phantom_shaddow);
        ghostShadowGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionGhostShadowIcon, R.drawable.motion_ghost_shaddow);
        lightningGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionLightningIcon, R.drawable.motion_lightning);
        mirrorGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionMirrorIcon, R.drawable.motion_mirror);
        illusionGif = UIAttributeUtil.getResResources(context, R.attr.editerMotionIllusionIcon, R.drawable.motion_illusion);

        soulOutColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionSoulOutCoverColor, R.color.soul_out_color_press);
        splitScreenColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionSplitScreenCoverColor, R.color.screen_split_press);
        rockLightColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionRockLightCoverColor, R.color.rock_light_press);
        darkDreamColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionDarkDreamCoverColor, R.color.dark_dream_press);
        winShadowColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionWinShadowCoverColor, R.color.win_shaddow_color_press);
        ghostShadowColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionGhostShadowCoverColor, R.color.ghost_shaddow_color_press);
        phantomShadowColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionPhantomShadowCoverColor, R.color.phantom_shaddow_color_press);
        ghostColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionGhostCoverColor, R.color.ghost_color_press);
        lightningColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionLightningCoverColor, R.color.lightning_color_press);
        mirrorColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionMirrorCoverColor, R.color.mirror_color_press);
        illusionColor = UIAttributeUtil.getColorRes(context, R.attr.editerMotionIllusionCoverColor, R.color.illusion_color_press);

        mMotionMap = new HashMap<>();
        mMotionMap.put(R.id.btn_soul_out, new TCMotionItem(R.id.btn_soul_out, R.id.rl_spirit_out_select_container, soulOutGif, TXVideoEditConstants.TXEffectType_SOUL_OUT));
        mMotionMap.put(R.id.btn_split, new TCMotionItem(R.id.btn_split, R.id.rl_split_select_container, splitScreenGif, TXVideoEditConstants.TXEffectType_SPLIT_SCREEN));
        mMotionMap.put(R.id.btn_rock_light, new TCMotionItem(R.id.btn_rock_light, R.id.rl_light_wave_select_container, rockLightGif, TXVideoEditConstants.TXEffectType_ROCK_LIGHT));
        mMotionMap.put(R.id.btn_dark_dream, new TCMotionItem(R.id.btn_dark_dream, R.id.rl_dark_select_container, darkDreamGif, TXVideoEditConstants.TXEffectType_DARK_DRAEM));
        mMotionMap.put(R.id.btn_win_shadow, new TCMotionItem(R.id.btn_win_shadow, R.id.rl_win_shadow_select_container, winShadowGif, TXVideoEditConstants.TXEffectType_WIN_SHADDOW));
        mMotionMap.put(R.id.btn_ghost_shadow, new TCMotionItem(R.id.btn_ghost_shadow, R.id.rl_ghost_shadow_select_container, ghostShadowGif, TXVideoEditConstants.TXEffectType_GHOST_SHADDOW));
        mMotionMap.put(R.id.btn_phantom_shadow, new TCMotionItem(R.id.btn_phantom_shadow, R.id.rl_phantom_shadow_select_container, phantomShadowGif, TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW));
        mMotionMap.put(R.id.btn_ghost, new TCMotionItem(R.id.btn_ghost, R.id.rl_ghost_select_container, ghostShadowGif, TXVideoEditConstants.TXEffectType_GHOST));
        mMotionMap.put(R.id.btn_lightning, new TCMotionItem(R.id.btn_lightning, R.id.rl_lightning_select_container, lightningGif, TXVideoEditConstants.TXEffectType_LIGHTNING));
        mMotionMap.put(R.id.btn_mirror, new TCMotionItem(R.id.btn_mirror, R.id.rl_mirror_select_container, mirrorGif, TXVideoEditConstants.TXEffectType_MIRROR));
        mMotionMap.put(R.id.btn_illusion, new TCMotionItem(R.id.btn_illusion, R.id.rl_illusion_select_container, illusionGif, TXVideoEditConstants.TXEffectType_ILLUSION));

        for (Map.Entry<Integer, TCMotionItem> entry : mMotionMap.entrySet()) {
            TCMotionItem item = entry.getValue();
            ImageButton ibtn = (ImageButton) view.findViewById(item.ivID);
            ibtn.setOnTouchListener(this);
            Glide.with(this).load(item.animID).into(ibtn);
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.iv_undo) {
            undoMotion();
        }
    }

    /**
     * 撤销 undo
     */
    private void undoMotion() {
        ColorfulProgress.MarkInfo markInfo = mColorfulProgress.deleteLastMark();
        if (markInfo != null) {
            mVideoProgressController.setCurrentTimeMs(markInfo.startTimeMs);

            PlayerManagerKit.getInstance().previewAtTime(markInfo.startTimeMs);
        }

        mTXVideoEditer.deleteLastEffect();
        if (mColorfulProgress.getMarkListSize() > 0) {
            mIvUndo.setVisibility(View.VISIBLE);
        } else {
            mIvUndo.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (mIsOnTouch && action == MotionEvent.ACTION_DOWN) {
            return false;
        }

        TCMotionItem item = mMotionMap.get(view.getId());
        if (item != null) {
            RelativeLayout rlSelect = (RelativeLayout) getActivity().findViewById(item.rlSelectID);
            if (action == MotionEvent.ACTION_DOWN) {
                rlSelect.setVisibility(View.VISIBLE);
                pressMotion(item.effectID);
                mIsOnTouch = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                rlSelect.setVisibility(View.INVISIBLE);
                upMotion(item.effectID);
                mIsOnTouch = false;
            }
            return false;
        }

        return false;
    }

    private void pressMotion(int type) {
        // 未开始播放 则开始播放
        long currentTime = mVideoProgressController.getCurrentTimeMs();

        if (PlayerManagerKit.getInstance().isPreviewFinish) {
            TXCLog.i(TAG, "pressMotion, preview finished, ignore");
            mStartMark = false;
            return;
        }
        mStartMark = true;

        PlayerManagerKit.getInstance().playVideo(true);

        mTXVideoEditer.startEffect(type, currentTime);

        switch (type) {
            case TXVideoEditConstants.TXEffectType_SOUL_OUT:
                // 进度条开始变颜色
                mColorfulProgress.startMark(soulOutColor);
                break;
            case TXVideoEditConstants.TXEffectType_SPLIT_SCREEN:
                mColorfulProgress.startMark(splitScreenColor);
                break;
            case TXVideoEditConstants.TXEffectType_ROCK_LIGHT:
                mColorfulProgress.startMark(rockLightColor);
                break;
            case TXVideoEditConstants.TXEffectType_DARK_DRAEM:
                mColorfulProgress.startMark(darkDreamColor);
                break;
            case TXVideoEditConstants.TXEffectType_WIN_SHADDOW:
                mColorfulProgress.startMark(winShadowColor);
                break;
            case TXVideoEditConstants.TXEffectType_GHOST_SHADDOW:
                mColorfulProgress.startMark(ghostShadowColor);
                break;
            case TXVideoEditConstants.TXEffectType_PHANTOM_SHADDOW:
                mColorfulProgress.startMark(phantomShadowColor);
                break;
            case TXVideoEditConstants.TXEffectType_GHOST:
                mColorfulProgress.startMark(ghostColor);
                break;
            case TXVideoEditConstants.TXEffectType_LIGHTNING:
                mColorfulProgress.startMark(lightningColor);
                break;
            case TXVideoEditConstants.TXEffectType_MIRROR:
                mColorfulProgress.startMark(mirrorColor);
                break;
            case TXVideoEditConstants.TXEffectType_ILLUSION:
                mColorfulProgress.startMark(illusionColor);
                break;
        }
    }

    private void upMotion(int type) {
        if (!mStartMark) {
            return;
        }
        // 暂停播放
        PlayerManagerKit.getInstance().pausePlay();
        // 进度条结束标记
        mColorfulProgress.endMark();

        // 特效结束时间
        long currentTime = mVideoProgressController.getCurrentTimeMs();
        mTXVideoEditer.stopEffect(type, currentTime);
        // 显示撤销的按钮
        if (mColorfulProgress.getMarkListSize() > 0) {
            mIvUndo.setVisibility(View.VISIBLE);
        }
    }
}

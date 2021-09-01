package com.tencent.qcloud.ugckit.module.effect;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.effect.utils.DraftEditer;
import com.tencent.qcloud.ugckit.module.effect.utils.EffectEditer;

public class ConfigureLoader {
    private static final String TAG = "ConfigureLoader";

    @NonNull
    private static ConfigureLoader sInstance = new ConfigureLoader();
    private        DraftEditer     mDraftEditer;
    private        EffectEditer    mEffectEditer;

    private ConfigureLoader() {
    }

    @NonNull
    public static ConfigureLoader getInstance() {
        return sInstance;
    }

    /**
     * 将配置加载到草稿箱
     */
    public void loadConfigToDraft() {
        mDraftEditer = DraftEditer.getInstance();
        mEffectEditer = EffectEditer.getInstance();

        mDraftEditer.setBgmName(mEffectEditer.getBgmName());
        mDraftEditer.setBgmPath(mEffectEditer.getBgmPath());
        mDraftEditer.setBgmPos(mEffectEditer.getBgmPos());
        mDraftEditer.setBgmVolume(mEffectEditer.getBgmVolume());
        mDraftEditer.setVideoVolume(mEffectEditer.getVideoVolume());
        mDraftEditer.setBgmStartTime(mEffectEditer.getBgmStartTime());
        mDraftEditer.setBgmEndTime(mEffectEditer.getBgmEndTime());
        mDraftEditer.setBgmDuration(mEffectEditer.getBgmDuration());
    }

    /**
     * 将草稿箱变更的配置保存
     */
    public void saveConfigFromDraft() {
        mEffectEditer.setBgmName(mDraftEditer.getBgmName());
        mEffectEditer.setBgmPath(mDraftEditer.getBgmPath());
        mEffectEditer.setBgmPos(mDraftEditer.getBgmPos());
        mEffectEditer.setBgmVolume(mDraftEditer.getBgmVolume());
        mEffectEditer.setVideoVolume(mDraftEditer.getVideoVolume());
        mEffectEditer.setBgmStartTime(mDraftEditer.getBgmStartTime());
        mEffectEditer.setBgmEndTime(mDraftEditer.getBgmEndTime());
        mEffectEditer.setBgmDuration(mDraftEditer.getBgmDuration());
    }


}

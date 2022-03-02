package com.tencent.qcloud.ugckit.module.effect.utils;

import androidx.annotation.Nullable;

/**
 * 生效的配置保存【点击完成】
 */
public class EffectEditer {

    private static EffectEditer sInstance;
    @Nullable
    private        String       bgmPath;
    private        int          bgmPos;
    private        float        bgmVolume;
    private        float        videoVolume;
    private        long         bgmStartTime;
    private        long         bgmEndTime;
    private        long         bgmDuration;
    private        String       bgmName;

    public static EffectEditer getInstance() {
        if (sInstance == null) {
            synchronized (EffectEditer.class) {
                if (sInstance == null) {
                    sInstance = new EffectEditer();
                }
            }
        }
        return sInstance;
    }

    private EffectEditer() {
        // 要给个默认值，否则第一次去bgm选择界面，但是不选择任何一个直接返回，会设置到编辑器里音量都为0。
        bgmVolume = 0.5f;
        videoVolume = 0.5f;
    }

    public String getBgmName() {
        return bgmName;
    }

    public void setBgmName(String bgmName) {
        this.bgmName = bgmName;
    }

    @Nullable
    public String getBgmPath() {
        return bgmPath;
    }

    public void setBgmPath(String bgmPath) {
        this.bgmPath = bgmPath;
    }

    public int getBgmPos() {
        return bgmPos;
    }

    public void setBgmPos(int bgmPos) {
        this.bgmPos = bgmPos;
    }

    public float getBgmVolume() {
        return bgmVolume;
    }

    public void setBgmVolume(float bgmVolume) {
        this.bgmVolume = bgmVolume;
    }

    public float getVideoVolume() {
        return videoVolume;
    }

    public void setVideoVolume(float videoVolume) {
        this.videoVolume = videoVolume;
    }

    public long getBgmStartTime() {
        return bgmStartTime;
    }

    public void setBgmStartTime(long bgmStartTime) {
        this.bgmStartTime = bgmStartTime;
    }

    public long getBgmEndTime() {
        return bgmEndTime;
    }

    public void setBgmEndTime(long bgmEndTime) {
        this.bgmEndTime = bgmEndTime;
    }

    public long getBgmDuration() {
        return bgmDuration;
    }

    public void setBgmDuration(long bgmDuration) {
        this.bgmDuration = bgmDuration;
    }

    public void clear() {
        this.bgmPath = null;
        this.bgmPos = -1;
        this.bgmVolume = 0f;
        this.videoVolume = 1f;
        this.bgmStartTime = -1;
        this.bgmEndTime = -1;
    }
}

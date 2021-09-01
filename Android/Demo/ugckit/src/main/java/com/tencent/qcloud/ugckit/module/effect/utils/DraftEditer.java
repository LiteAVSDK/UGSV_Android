package com.tencent.qcloud.ugckit.module.effect.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.qcloud.ugckit.module.record.MusicInfo;

/**
 * 草稿箱保存【点击返回】
 */
public class DraftEditer {
    private static DraftEditer sInstance;
    @Nullable
    private        String      bgmPath;
    private        int         bgmPos;
    private        float       bgmVolume;
    private        float       videoVolume;
    private        long        bgmStartTime;
    private        long        bgmEndTime;
    private        long        bgmDuration;
    private        String      bgmName;

    public static DraftEditer getInstance() {
        if (sInstance == null) {
            synchronized (DraftEditer.class) {
                if (sInstance == null) {
                    sInstance = new DraftEditer();
                }
            }
        }
        return sInstance;
    }

    private DraftEditer() {
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

    @NonNull
    public MusicInfo loadMusicInfo() {
        MusicInfo musicInfo = new MusicInfo();
        musicInfo.path = getBgmPath();
        musicInfo.name = getBgmName();
        musicInfo.position = getBgmPos();
        musicInfo.videoVolume = getVideoVolume();
        musicInfo.bgmVolume = getBgmVolume();
        musicInfo.duration = getBgmDuration();
        musicInfo.startTime = getBgmStartTime();
        musicInfo.endTime = getBgmEndTime();
        return musicInfo;
    }

    public void saveRecordMusicInfo(@NonNull MusicInfo musicInfo) {
        setBgmPath(musicInfo.path);
        setBgmName(musicInfo.name);
        setBgmDuration(musicInfo.duration);
        setBgmPos(musicInfo.position);
        setBgmVolume(musicInfo.bgmVolume);
        setVideoVolume(musicInfo.videoVolume);
        setBgmStartTime(0);
        setBgmEndTime(musicInfo.duration);
    }
}

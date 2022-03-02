package com.tencent.qcloud.ugckit.module.record;

import androidx.annotation.Nullable;

/**
 * 录制-音乐信息
 */
public class MusicInfo {
    /**
     * 音乐名称
     */
    public String name;
    /**
     * 音乐路径
     */
    @Nullable
    public String path;
    /**
     * 正在播放的音乐路径
     */
    @Nullable
    public String playingPath;
    /**
     * 音乐列表中的位置
     */
    public int    position;
    /**
     * 音乐截取的开始时间
     */
    public long   startTime;
    /**
     * 音乐截取的结束时间
     */
    public long   endTime;
    /**
     * 音乐时长
     */
    public long   duration;
    /**
     * 人声的音量大小
     */
    public float  videoVolume;
    /**
     * 音乐的音量大小
     */
    public float  bgmVolume;
}

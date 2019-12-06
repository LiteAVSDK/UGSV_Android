package com.tencent.qcloud.ugckit.module.effect.bgm.view;

import android.support.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.record.MusicInfo;

/**
 * 视频编辑中音乐面板
 */
public interface IEditMusicPannel {
    /**
     * 设置选择的背景音乐基本信息
     *
     * @param musicInfo
     */
    void setMusicInfo(@NonNull MusicInfo musicInfo);

    /**
     * 设置背景音乐改变监听器
     *
     * @param listener
     */
    void setOnMusicChangeListener(MusicChangeListener listener);

    /**
     * 操作当前背景音乐
     */
    interface MusicChangeListener {
        /**
         * 录音音量改变
         *
         * @param volume
         */
        void onMicVolumeChanged(float volume);

        /**
         * 背景音乐音量改变
         *
         * @param volume
         */
        void onMusicVolumChanged(float volume);

        /**
         * 背景音乐起止时间改变
         *
         * @param startTime 开始时间
         * @param endTime   结束时间
         */
        void onMusicTimeChanged(long startTime, long endTime);

        /**
         * 背景音乐替换
         */
        void onMusicReplace();

        /**
         * 背景音乐删除
         */
        void onMusicDelete();
    }
}

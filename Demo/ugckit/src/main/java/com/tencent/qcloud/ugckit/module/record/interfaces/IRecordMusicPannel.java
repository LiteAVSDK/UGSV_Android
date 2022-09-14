package com.tencent.qcloud.ugckit.module.record.interfaces;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.record.MusicInfo;

/**
 * 视频录制中音乐面板
 */
public interface IRecordMusicPannel {
    /**
     * 设置选择的背景音乐基本信息
     *
     * @param musicInfo
     */
    void setMusicInfo(@NonNull MusicInfo musicInfo);

    /**
     * 设置音乐面板监听器
     *
     * @param listener
     */
    void setOnMusicChangeListener(MusicChangeListener listener);

    interface MusicChangeListener {
        /**
         * 音乐音量改变
         *
         * @param volume
         */
        void onMusicVolumChanged(float volume);

        /**
         * 音乐开始和结束时间改变
         *
         * @param startTime
         * @param endTime
         */
        void onMusicTimeChanged(long startTime, long endTime);

        /**
         * 切换背景音乐
         */
        void onMusicReplace();

        /**
         * 删除背景音乐
         */
        void onMusicDelete();

        /**
         * 选择音乐
         */
        void onMusicSelect();
    }

    /**
     * 设置音乐Icon
     */
    void setMusicIconResource(@DrawableRes int resid);

    /**
     * 设置"删除音乐"Icon
     */
    void setMusicDeleteIconResource(@DrawableRes int resid);

    /**
     * 设置"替换音乐"Icon
     */
    void setMusicReplaceIconResource(@DrawableRes int resid);

    /**
     * 设置"音乐音量"seekbar颜色
     */
    void setVolumeSeekbarColor(@ColorRes int color);

    /**
     * 设置"音乐开始和结束范围条"颜色
     */
    void setMusicRangeColor(Drawable color);

    /**
     * 设置"音乐开始和结束范围条"背景图片Icon
     */
    void setMusicRangeBackgroundResource(@DrawableRes int resid);

    /**
     * 设置"确定"按钮背景颜色
     */
    void setConfirmButtonBackgroundColor(@ColorRes int color);

    /**
     * 设置"确定"按钮文字颜色
     */
    void setConfirmButtonTextColor(@ColorRes int color);

    /**
     * 设置"确定"按钮文字字体大小
     */
    void setConfirmButtonTextSize(int size);
}

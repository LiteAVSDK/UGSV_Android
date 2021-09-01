package com.tencent.qcloud.ugckit.module.record.interfaces;

import androidx.annotation.ColorRes;

/**
 * 定制化"多段录制进度条"<br>
 * 注意：颜色请放入资源文件，通过R文件引入，示例 {@code getResources().getColor(R.color.record_progress_bg)}<br>
 * 进度条颜色，包括<br>
 * 1、已经录制的视频进度条颜色<br>
 * 2、删除上一段选中的进度条颜色<br>
 * 3、进度条背景颜色<br>
 * 4、多段录制间隔颜色<br>
 */

public interface IRecordProgressView {

    /**
     * 选中多段进度中最后一段
     */
    void selectLast();

    /**
     * 删除多段进度中最后一段
     */
    void deleteLast();

    /**
     * 设置进度条最大时长，单位：毫秒
     *
     * @param maxDuration
     */
    void setMaxDuration(int maxDuration);

    /**
     * 设置进度条最小时长，单位：毫秒
     *
     * @param minDuration
     */
    void setMinDuration(int minDuration);

    /**
     * 设置进度
     *
     * @param progress
     */
    void setProgress(int progress);

    /**
     * 已经录制的视频进度条颜色
     */
    void setNormalColor(@ColorRes int color);

    /**
     * 删除上一段选中的进度条颜色
     */
    void setDeleteColor(@ColorRes int color);

    /**
     * 多段录制间隔颜色
     */
    void setSpaceColor(@ColorRes int color);
}

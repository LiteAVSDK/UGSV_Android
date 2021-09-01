package com.tencent.qcloud.ugckit.module.effect;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * 定制化播放状态控制UI
 */
public interface IPlayControlLayout {

    /**
     * 设置当前播放时间字体大小
     *
     * @param size
     */
    void setCurrentTimeTextSize(int size);

    /**
     * 设置当前播放时间字体颜色
     *
     * @param color
     */
    void setCurrentTimeTextColor(@ColorRes int color);

    /**
     * 设置播放按钮Icon
     *
     * @param resid
     */
    void setPlayIconResource(@DrawableRes int resid);

    /**
     * 设置暂停按钮Icon
     *
     * @param resid
     */
    void setPauseIconResource(@DrawableRes int resid);
}

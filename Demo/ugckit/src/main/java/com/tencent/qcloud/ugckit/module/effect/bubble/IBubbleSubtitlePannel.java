package com.tencent.qcloud.ugckit.module.effect.bubble;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import java.util.List;

/**
 * 气泡字幕面板
 */
public interface IBubbleSubtitlePannel {

    /**
     * 加载所有的气泡字幕
     */
    void loadAllBubble(List<TCBubbleInfo> list);

    /**
     * 显示气泡字幕面板
     *
     * @param info 编辑已存在的气泡字幕，如果不存在传入null
     */
    void show(TCSubtitleInfo info);

    /**
     * 隐藏气泡字幕面板
     */
    void dismiss();

    /**
     * 设置添加气泡字幕回调接口
     *
     * @param callback
     */
    void setOnBubbleSubtitleCallback(OnBubbleSubtitleCallback callback);

    interface OnBubbleSubtitleCallback {
        /**
         * 添加一个气泡字幕
         *
         * @param info
         */
        void onBubbleSubtitleCallback(TCSubtitleInfo info);
    }

}

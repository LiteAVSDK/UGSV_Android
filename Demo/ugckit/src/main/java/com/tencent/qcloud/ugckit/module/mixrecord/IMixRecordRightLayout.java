package com.tencent.qcloud.ugckit.module.mixrecord;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * 定制化合唱右侧工具栏
 */
public interface IMixRecordRightLayout {

    /**
     * 设置合唱右侧工具栏点击事件
     *
     * @param listener
     */
    void setOnItemClickListener(OnItemClickListener listener);

    interface OnItemClickListener {

        /**
         * 显示基础美颜面板
         */
        void onShowBeautyPanel();

        /**
         * 显示高级美颜面板
         */
        void onShowTEBeautyPanel();

        /**
         * 显示倒计时动画
         */
        void countDownTimer();
    }

    /************************************************************************/
    /*****                    定制化"美颜图标"                             *****/
    /************************************************************************/

    /**
     * 美颜图标ResId：<pre>R.drawable.icon_beauty</pre>
     */
    void setBeautyIconResource(@DrawableRes int resid);

    /**
     * 美颜图标文字大小
     */
    void setBeautyTextSize(int beautyFontSize);

    /**
     * 美颜图标文字颜色
     */
    void setBeautyTextColor(@ColorRes int color);

    /************************************************************************/
    /*****                    定制化"倒计时图标"                            *****/
    /************************************************************************/

    /**
     * 倒计时图标ResId：<pre>R.drawable.icon_beauty</pre>
     */
    void setCountDownIconResource(@DrawableRes int resid);

    /**
     * 倒计时图标文字大小
     */
    void setCountDownTextSize(int size);

    /**
     * 倒计时图标文字颜色
     */
    void setCountDownTextColor(@ColorRes int color);
}

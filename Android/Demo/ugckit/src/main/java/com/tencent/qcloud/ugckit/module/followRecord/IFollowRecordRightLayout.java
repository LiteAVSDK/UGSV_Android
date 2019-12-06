package com.tencent.qcloud.ugckit.module.followRecord;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

/**
 * 定制化合唱右侧工具栏
 */
public interface IFollowRecordRightLayout {

    /**
     * 设置合唱右侧工具栏点击事件
     *
     * @param listener
     */
    void setOnItemClickListener(OnItemClickListener listener);

    interface OnItemClickListener {
        /**
         * 显示美颜面板
         */
        void onShowBeautyPannel();

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

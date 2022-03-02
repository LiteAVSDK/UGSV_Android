package com.tencent.qcloud.ugckit.module.picturetransition;

import androidx.annotation.DrawableRes;

/**
 * 定制化图片转场Icon
 */
public interface IPictureTransitionLayout {

    /**
     * 设置切换图片转场监听器
     *
     * @param listener
     */
    void setTransitionListener(OnTransitionListener listener);

    interface OnTransitionListener {
        /**
         * 设置图片转场类型
         *
         * @param type {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING}
         *             {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_UP_DOWN_SLIPPING}
         *             {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_ENLARGE}
         *             {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_NARROW}
         *             {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_ROTATIONAL_SCALING}
         *             {@link com.tencent.ugc.TXVideoEditConstants#TX_TRANSITION_TYPE_FADEIN_FADEOUT}
         */
        void transition(int type);
    }

    /**
     * 禁用"左右"切换转场
     */
    void disableLeftrightTransition();

    /**
     * 禁用"上下"切换转场
     */
    void disableUpdownTransition();

    /**
     * 禁用"放大"切换转场
     */
    void disableEnlargeTransition();

    /**
     * 禁用"缩小"切换转场
     */
    void disableNarrowTransition();

    /**
     * 禁用"旋转"切换转场
     */
    void disableRotateTransition();

    /**
     * 禁用"淡入淡出"切换转场
     */
    void disableFadeinoutTransition();

    /**
     * 设置"左右"切换转场Icon
     *
     * @param resid
     */
    void setLeftrightIconResource(@DrawableRes int resid);

    /**
     * 设置"上下"切换转场Icon
     *
     * @param resid
     */
    void setUpdownIconResource(@DrawableRes int resid);

    /**
     * 设置"放大"转场Icon
     *
     * @param resid
     */
    void setEnlargeIconResource(@DrawableRes int resid);

    /**
     * 设置"缩小"转场Icon
     *
     * @param resid
     */
    void setNarrowIconResource(@DrawableRes int resid);

    /**
     * 设置"旋转"转场Icon
     *
     * @param resid
     */
    void setRotateIconResource(@DrawableRes int resid);

    /**
     * 设置"淡入淡出"转场Icon
     *
     * @param resid
     */
    void setFadeinoutIconResource(@DrawableRes int resid);
}

package com.tencent.qcloud.ugckit.module.record.interfaces;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * 定制化"多模式录制的录制按钮"
 */

public interface IRecordButton {

    /**
     * 设置录制按钮监听器
     *
     * @param listener
     */
    void setOnRecordButtonListener(OnRecordButtonListener listener);

    interface OnRecordButtonListener {
        /**
         * 多段录制点击开始
         */
        void onRecordStart();

        /**
         * 多段录制点击暂停
         */
        void onRecordPause();

        /**
         * 拍照
         */
        void onTakePhoto();

        /**
         * 删除
         */
        void onDeleteParts(int partsSize, long duration);
    }

    /**
     * 设置当前拍摄模式
     *
     * @param recordMode
     */
    void setCurrentRecordMode(int recordMode);

    /**
     * "拍照按钮"外圈颜色
     */
    void setPhotoOutterColor(@ColorRes int color);

    /**
     * "拍照按钮"内圈颜色
     */
    void setPhotoInnerColor(@ColorRes int color);

    /**
     * "单击拍摄"外圈颜色
     */
    void setClickRecordOutterColor(@ColorRes int color);

    /**
     * "单击拍摄"内圈颜色
     */
    void setClickRecordInnerColor(@ColorRes int color);

    /**
     * "按住拍摄"外圈颜色
     */
    void setTouchRecordOutterColor(@ColorRes int color);

    /**
     * "按住拍摄"内圈颜色
     */
    void setTouchRecordInnerColor(@ColorRes int color);

    /**
     * 暂停按钮Icon
     */
    void setPauseIconResource(@DrawableRes int resid);
}

package com.tencent.qcloud.ugckit.module.record.interfaces;

import android.content.res.ColorStateList;

import com.tencent.qcloud.ugckit.module.record.RecordSpeedLayout;

/**
 * 定制化"录制速度面板"
 */
public interface IRecordSpeedLayout {
    /**
     * 设置录制速度监听器
     *
     * @param listener
     */
    void setOnRecordSpeedListener(RecordSpeedLayout.OnRecordSpeedListener listener);

    interface OnRecordSpeedListener {
        /**
         * 选择一种速度
         *
         * @param currentSpeed 当前速度
         */
        void onSpeedSelect(int currentSpeed);
    }

    /**
     * 文字大小
     */
    void setSpeedTextSize(int size);

    /**
     * 文字颜色(普通状态/选中状态)
     */
    void setSpeedTextColor(ColorStateList color);

}

package com.tencent.qcloud.ugckit.module.record.interfaces;

import com.tencent.qcloud.ugckit.module.record.beauty.BeautyParams;

/**
 * 美颜面板定制化UI
 */
public interface IBeautyPannel {
    /**
     * 设置美颜面板监听器
     *
     * @param listener
     */
    void setBeautyParamsChangeListener(IOnBeautyParamsChangeListener listener);

    interface IOnBeautyParamsChangeListener {
        /**
         * 美颜参数改变
         *
         * @param params
         * @param key
         */
        void onBeautyParamsChange(BeautyParams params, int key);
    }

}

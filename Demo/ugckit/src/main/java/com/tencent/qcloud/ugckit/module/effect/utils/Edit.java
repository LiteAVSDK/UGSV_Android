package com.tencent.qcloud.ugckit.module.effect.utils;

public class Edit {

    /**
     * 视频裁剪片段选取改变监听
     */
    public interface OnCutChangeListener {
        void onCutClick();

        void onCutChangeKeyDown();

        /**
         * @param startTime
         * @param endTime
         * @param type      TCVideoEditerActivity#TYPE_CUT/ TYPE_REPEAT
         */
        void onCutChangeKeyUp(long startTime, long endTime, int type);
    }
}

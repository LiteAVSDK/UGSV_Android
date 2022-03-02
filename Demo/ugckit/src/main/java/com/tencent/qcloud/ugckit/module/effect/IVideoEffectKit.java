package com.tencent.qcloud.ugckit.module.effect;

import com.tencent.qcloud.ugckit.UGCKitConstants;

public interface IVideoEffectKit {

    /**
     * 当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}来重新播放视频
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}来停止播放视频
     */
    void stop();

    /**
     * 当Activity执行生命周期方法{@code onDestroy()}时，UGCKit需要执行{@link #release()}来释放资源<br>
     */
    void release();

    /**
     * 退出视频录制，返回上一界面
     */
    void backPressed();

    /**
     * 设置显示的特效Fragment类型
     *
     * @param effectType {@link UGCKitConstants#TYPE_EDITER_BGM} 添加背景音<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_MOTION} 添加动态滤镜<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_SPEED} 添加时间特效<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_FILTER} 添加静态滤镜<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_PASTER} 添加贴纸<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_SUBTITLE} 添加字幕<br>
     *                   {@link UGCKitConstants#TYPE_EDITER_TRANSITION} 添加转场特效<br>
     */
    void setEffectType(int effectType);

    /**
     * 设置特效应用监听器
     *
     * @param listener
     */
    void setOnVideoEffectListener(OnVideoEffectListener listener);

    interface OnVideoEffectListener {
        /**
         * 视频特效应用【当前跳转到特效页面操作的所有特效添加到视频源上】
         */
        void onEffectApply();

        /**
         * 视频特效取消【当前跳转到特效页面操作的所有特效不会添加到视频源上，以前添加的特效还有效果】
         */
        void onEffectCancel();
    }

}

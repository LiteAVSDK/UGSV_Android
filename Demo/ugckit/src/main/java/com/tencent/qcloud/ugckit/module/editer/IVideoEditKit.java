package com.tencent.qcloud.ugckit.module.editer;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutKit;

/**
 * 腾讯云短视频UGCKit({@code IVideoEditKit}):视频编辑。
 *
 * <p>
 * 如下演示了UGCKit视频录制模块简单用法：<br>
 */
public interface IVideoEditKit {

    /**
     * 当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}来完成如下功能<br>
     * 重新播放视频
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}来完成如下功能<br>
     * 1、停止播放视频
     * 2、停止视频生成
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
     * 初始化播放器控件
     */
    void initPlayer();

    /**
     * 设置输入选项
     */
    void setConfig(UGCKitEditConfig config);

    /**
     * 设置视频编辑的监听器
     */
    void setOnVideoEditListener(OnEditListener listener);

    interface OnEditListener {
        /**
         * 视频编辑操作完成，生成编辑好的视频。
         *
         * @param ugcKitResult
         */
        void onEditCompleted(UGCKitResult ugcKitResult);

        /**
         * 视频编辑操作取消
         */
        void onEditCanceled();
    }
    /************************************************************************/
    /*****                     功能定制化                                 *****/
    /************************************************************************/

    /**
     * 设置视频的源路径</p>
     * 注意：【如果上一个页面为"视频裁剪页面"，并且设置了下一步进行"视频编辑"{@link IVideoCutKit#setVideoEditFlag(boolean)}，
     * 那么就不需要调用此接口{@link IVideoEditKit#setVideoPath(String)} 设置视频路径。】
     *
     * @param videoPath 视频裁剪的源路径
     */
    void setVideoPath(String videoPath);

}

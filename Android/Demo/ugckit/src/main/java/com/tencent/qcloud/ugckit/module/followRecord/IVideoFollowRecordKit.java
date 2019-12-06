package com.tencent.qcloud.ugckit.module.followRecord;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;

/**
 * 腾讯云短视频TUIKit({@code IVideoFollowRecordKit}):视频合唱。
 * 视频合唱分为左右两个预览界面，左侧为视频录制界面，右侧为视频跟拍界面。<br>
 * 当右侧跟拍视频开始播放时，左侧同时开始进行视频录制，生成一个画面左右拼接的合唱视频<br>
 * <p>
 * 您可以通过TUIkit很简单搭建使用视频合唱功能，也可以定制视频合唱的功能和UI
 * <p>
 * 如下演示了TUIKit视频合唱模块简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.chorus.VideoChorus
 *         android:id="@+id/video_chorus"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 * }</pre>
 * <p>
 * 2、在Activity中设置
 * <pre>
 *     &#064;Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_video_record);
 *
 *         mVideoChorus = (VideoChorus) findViewById(R.id.video_chorus);
 *
 *         FollowRecordInfo chorusVideoInfo = new FollowRecordInfo();
 *         chorusVideoInfo.playPath = mFollowShotVideoPath;
 *         chorusVideoInfo.duration = mFollowShotVideoDuration;
 *         chorusVideoInfo.fps = mFollowShotVideoFps;
 *         chorusVideoInfo.audioSampleRate = mFollowShotAudioSampleRateType;
 *
 *         mVideoChorus.setFollowRecordInfo(chorusVideoInfo);
 *         mVideoChorus.setOnFollowRecordListener(new IVideoFollowRecordKit.OnFollowRecordListener() {
 *             &#064Override
 *             public void onFollowRecordCanceled() {
 *             }
 *
 *             &#064Override
 *             public void onFollowRecordCompleted(String outputPath) {
 *             }
 *         });
 *     }
 *
 *     &#064;Override
 *     protected void onStart() {
 *         super.onStart();
 *         mVideoChorus.start();
 *     }
 *
 *     &#064;Override
 *     protected void onStop() {
 *         super.onStop();
 *         mVideoChorus.stop();
 *     }
 *
 *     &#064;Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         mVideoChorus.release();
 *     }
 * </pre>
 * <p>
 * TUIKit视频合唱模块的生命周期方法<br>
 * 1、当Activity执行生命周期方法{@code onStart()}时，TUIKit需要执行{@link #start()}初始化合唱配置，打开合唱预览界面，
 * 其中包括 左边"录制的摄像头预览界面"和右侧"跟拍视频的播放界面"<br>
 * 2、当Activity执行生命周期方法{@code onStop()}时，TUIKit需要执行{@link #stop()}暂停合唱功能，包括关闭视频录制预览界面，暂停视频录制；停止播放跟拍视频<br>
 * 3、当Activity执行生命周期方法{@code onDestroy()}时，TUIKit视频录制需要执行{@link #release()}来释放资源<br>
 * 4、当您开启Activity 的界面旋转后，Activity执行生命周期方法{@code onConfigurationChanged()}时，TUIKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
 * 5、当Activity执行 {@code stopPlay()} 时，TUIKit需要执行{@link #backPressed()} 退出视频合唱，返回上一界面
 * <p>
 * 视频合唱功能定制化<br>
 * 1、禁用倒计时开始录制功能{@link #disableCountDownTimer()}<br>
 * <p>
 * 视频合唱UI定制化<br>
 * 1、定制化"美颜图标" {@link IFollowRecordRightLayout}  <br>
 * 2、定制化"倒计时图标" {@link IFollowRecordRightLayout} <br>
 * 3、定制化"倒计时"动画View {@link ICountDownTimerView}
 * <p>
 * 视频合唱相关文档
 * <a href="https://cloud.tencent.com/document/product/584/18373">视频合唱(Android)</a>
 */
public interface IVideoFollowRecordKit {
    /**
     * 当Activity执行生命周期方法{@code onStart()}时，TUIKit需要执行{@link #start()}来完成如下功能<br>
     * 1、初始化合唱配置<br>
     * 2、打开合唱预览界面，其中包括 左边"录制的摄像头预览界面"和右侧"跟拍视频的播放界面"
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，TUIKit需要执行{@link #stop()}来完成如下功能<br>
     * 暂停合唱功能，其中包括<br/>
     * 左侧<br/>
     * 1、关闭视频录制预览界面<br/>
     * 2、暂停视频录制<br/>
     * 右侧<br/>
     * 1、停止播放跟拍视频
     * <br/>
     */
    void stop();

    /**
     * 当Activity执行生命周期方法{@code onDestroy()}时，TUIKit需要执行{@link #release()}来释放资源<br>
     */
    void release();

    /**
     * 当Activity执行生命周期方法{@code onConfigurationChanged()}时，TUIKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
     */
    void screenOrientationChange();

    /**
     * 退出合唱，返回上一界面
     */
    void backPressed();

    /**
     * 设置合唱视频信息
     *
     * @param followRecordInfo
     * @see FollowRecordInfo
     */
    void setFollowRecordInfo(FollowRecordInfo followRecordInfo);

    /**
     * 设置合唱的监听器
     */
    void setOnFollowRecordListener(OnFollowRecordListener listener);

    interface OnFollowRecordListener {
        /**
         * 合唱生成视频取消
         */
        void onFollowRecordCanceled();

        /**
         * 合唱生成视频完成
         *
         * @param
         */
        void onFollowRecordCompleted(UGCKitResult ugcKitResult);
    }
    /************************************************************************/
    /*****                     功能定制化                                 *****/
    /************************************************************************/

    /**
     * 禁用倒计时开始录制功能<br>
     * 不显示倒计时Icon和倒计时动画View{@link ICountDownTimerView}
     */
    void disableCountDownTimer();

    /**
     * 禁用美颜功能
     */
    void disableBeauty();

    /**
     * 视频合唱完是否进行视频特效处理
     *
     * @param enable {@code true} 进行视频特效编辑<br>
     *               {@code false} 直接输出合唱完视频<br>
     *               默认为true
     */
    void setEditVideoFlag(boolean enable);

}

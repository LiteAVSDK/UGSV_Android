package com.tencent.qcloud.ugckit.module.mixrecord;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;

import java.util.List;

/**
 * 腾讯云短视频UGCKit({@code IVideoMixRecordKit}):视频合唱。
 * 视频合唱分为左右两个预览界面，左侧为视频录制界面，右侧为视频跟拍界面。<br>
 * 当右侧跟拍视频开始播放时，左侧同时开始进行视频录制，生成一个画面左右拼接的合唱视频<br>
 * <p>
 * 您可以通过UGCKit很简单搭建使用视频合唱功能，也可以定制视频合唱的功能和UI
 * <p>
 * 如下演示了UGCKit视频合唱模块简单用法：<br>
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
 *         setContentView(R.layout.activity_video_chorus);
 *
 *         mUGCKitVideoFollowRecord = (UGCKitVideoMixRecord) findViewById(R.id.video_chorus);
 *
 *         List<String> paths = new ArrayList<>();
 *         paths.add(mFollowShotVideoPath);
 *         mUGCKitVideoFollowRecord.setMixRecordInfo(paths,0,720*2,1280,TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
 *         mUGCKitVideoFollowRecord.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
 *             @Override
 *             public void onClick(View v) {
 *                 finish();
 *             }
 *         });
 *         mUGCKitVideoFollowRecord.setOnMixRecordListener(new IVideoMixRecordKit.OnMixRecordListener() {
 *             @Override
 *             public void onMixRecordCanceled() {
 *                 finish();
 *             }
 *
 *             @Override
 *             public void onMixRecordCompleted(UGCKitResult ugcKitResult) {
 *                 startEditActivity();
 *             }
 *
 *             @Override
 *             public void onMixRecordAction(IVideoMixRecordKit.MixRecordActionT actionT, Object object) {
 *                 if(actionT== IVideoMixRecordKit.MixRecordActionT.MIX_RECORD_ACTION_T_SELECT){
 *                     MixRecordActionData data =(MixRecordActionData)object;
 *                     Intent intent = new Intent(TCVideoFollowRecordActivity.this, TCTripleRecordVideoPickerActivity.class);
 *                     startActivityForResult(intent,REQUEST_CODE);
 *                 }
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
 * UGCKit视频合唱模块的生命周期方法<br>
 * 1、当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}初始化合唱配置，打开合唱预览界面，
 * 其中包括 左边"录制的摄像头预览界面"和右侧"跟拍视频的播放界面"<br>
 * 2、当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}暂停合唱功能，包括关闭视频录制预览界面，暂停视频录制；停止播放跟拍视频<br>
 * 3、当Activity执行生命周期方法{@code onDestroy()}时，UGCKit视频录制需要执行{@link #release()}来释放资源<br>
 * 4、当您开启Activity 的界面旋转后，Activity执行生命周期方法{@code onConfigurationChanged()}时，UGCKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
 * 5、当Activity执行 {@code stopPlay()} 时，UGCKit需要执行{@link #backPressed()} 退出视频合唱，返回上一界面
 * <p>
 * 视频合唱功能定制化<br>
 * 1、禁用倒计时开始录制功能{@link #disableCountDownTimer()}<br>
 * <p>
 * 视频合唱UI定制化<br>
 * 1、定制化"美颜图标" {@link IMixRecordRightLayout}  <br>
 * 2、定制化"倒计时图标" {@link IMixRecordRightLayout} <br>
 * 3、定制化"倒计时"动画View {@link ICountDownTimerView}
 * <p>
 * 视频合唱相关文档
 * <a href="https://cloud.tencent.com/document/product/584/18373">视频合唱(Android)</a>
 *
 *Tencent Cloud UGCKit: Duet Lifecycle<br>
 * 1. After the lifecycle callback {@code onStart()} is invoked for the activity, call {@link #start()}
 * of UGCKit to initialize duet configurations and open the duet view.
 * The left half of the duet view is the shooting view and the right half is the duet video view.<br>
 * 2. After the lifecycle callback {@code onStop()} is invoked for the activity, call {@link #stop()}
 * of UGCKit to pause duet. This means closing the shooting view, pausing shooting,
 * and stopping the playback of the duet video.<br>
 * 3. After the lifecycle callback {@code onDestroy()} is invoked for the activity, call {@link #release()}
 * of UGCKit to release the resources.<br>
 * 4. After auto rotation is enabled for the activity, the lifecycle callback {@code onConfigurationChanged()}
 * will be invoked. Call {@link #screenOrientationChange()} of UGCKit to change the orientation.<br>
 * 5. After the lifecycle callback {@code stopPlay()} is invoked, call {@link #backPressed()}
 * of UGCKit to exit the duet view and return to the previous view.
 * <p>
 * Custom Duet Features<br>
 * 1. Call {@link #disableCountDownTimer()} to disable the timer.<br>
 * <p>
 * Custom Duet UI<br>
 * 1. Call {@link IMixRecordRightLayout} to customize the “Beauty” icon.<br>
 * 2. Call {@link IMixRecordRightLayout} to customize the “Timer” icon.<br>
 * 3. Call {@link ICountDownTimerView} to customize the timer animation.
 * <p>
 * Duet documentation
 *  <a href="https://cloud.tencent.com/document/product/584/18373">视频合唱(Android)</a>
 * */



public interface IVideoMixRecordKit {
    /**
     * 当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}来完成如下功能<br>
     * 1、初始化合唱配置<br>
     * 2、打开合唱预览界面，其中包括 左边"录制的摄像头预览界面"和右侧"跟拍视频的播放界面"
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}来完成如下功能<br>
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
     * 当Activity执行生命周期方法{@code onDestroy()}时，UGCKit需要执行{@link #release()}来释放资源<br>
     */
    void release();

    /**
     * 当Activity执行生命周期方法{@code onConfigurationChanged()}时，UGCKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
     */
    void screenOrientationChange();

    /**
     * 退出合唱，返回上一界面
     */
    void backPressed();

    /**
     * 设置视频信息
     *
     * @param videoPaths
     */
    void setMixRecordInfo(MixRecordConfigBuildInfo buildInfo);

    /**
     * 设置合唱的监听器
     */
    void setOnMixRecordListener(OnMixRecordListener listener);

    void updateMixFile(int index, String filePath);

    interface OnMixRecordListener {
        /**
         * 生成视频取消
         */
        void onMixRecordCanceled();

        /**
         * 生成视频完成
         *
         * @param
         */
        void onMixRecordCompleted(UGCKitResult ugcKitResult);

        void onMixRecordAction(MixRecordActionT actionT, Object object);
    }

    enum MixRecordActionT {
        MIX_RECORD_ACTION_T_SELECT
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

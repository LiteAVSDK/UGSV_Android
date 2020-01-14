package com.tencent.qcloud.ugckit.module.record.interfaces;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.effect.bgm.view.SoundEffectsPannel;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.module.record.RecordMusicPannel;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;

/**
 * 腾讯云短视频UGCKit({@code IVideoRecordKit}):视频录制。
 * 本组件包含视频录制的所有SDK功能和UI展示，包括拍照，多段录制，草稿箱，美颜，变声和混响，添加背景音乐，多屏比录制，变速录制等等。<br>
 * 您可以通过UGCKit很简单搭建使用诸多视频录制功能，也可以定制视频录制的功能和UI
 * <p>
 * 如下演示了UGCKit视频录制模块简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.record.VideoRecord
 *         android:id="@+id/video_record_layout"
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
 *         mVideoRecord = (VideoRecord) findViewById(R.id.video_record_layout);
 *         mVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
 *             &#064;Override
 *             public void onRecordCanceled() {
 *             }
 *
 *             &#064;Override
 *             public void onRecordCompleted(String outputPath) {
 *             }
 *         });
 *     }
 *
 *     &#064;Override
 *     protected void onStart() {
 *         super.onStart();
 *         if (hasPermission()) {
 *             mVideoRecord.start();
 *         }
 *     }
 *
 *     &#064;Override
 *     protected void onStop() {
 *         super.onStop();
 *         mVideoRecord.stop();
 *     }
 *
 *     &#064;Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         mVideoRecord.release();
 *     }
 * </pre>
 * <p>
 * UGCKit视频录制模块的生命周期方法<br>
 * 1、当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}初始化录制配置，开始视频录制预览界面<br>
 * 2、当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}暂停视频录制，关闭视频录制预览界面，并更新视频录制界面<br>
 * 3、当Activity执行生命周期方法{@code onDestroy()}时，UGCKit需要执行{@link #release()}来释放资源<br>
 * 4、当您开启Activity 的界面旋转后，Activity执行生命周期方法{@code onConfigurationChanged()}时，
 * UGCKit需要执行{@link #screenOrientationChange()}来更改屏幕预览方向<br>
 * 5、当Activity执行 {@code stopPlay()} 时，UGCKit需要执行{@link #backPressed()} 退出视频录制，返回上一界面
 * <p>
 * UGCKit视频录制背景音乐设置<br>
 * 1、通过 {@link #setOnMusicChooseListener(OnMusicChooseListener)} 监听点击音乐Icon，启动背景音乐Activity进行音乐选择。
 * 2、当Activity选择背景音乐后，通过{@link #setRecordMusicInfo(MusicInfo)} 设置给UGCKit。<br>
 * 其中必须设置的包括背景音乐名称{@link MusicInfo#name}，背景音乐路径{@link MusicInfo#path}，
 * 和背景音乐在列表中的位置{@link MusicInfo#position}<br>
 * <p>
 * 视频录制功能定制化<br>
 * 1、视频录制完是否进行视频特效处理 {@link #setEditVideoFlag(boolean)}<br>
 * 2、禁用录制速度功能{@link #disableRecordSpeed()}<br>
 * 3、禁用拍照功能{@link #disableTakePhoto}<br>
 * 4、禁用背景音乐功能{@link #disableRecordMusic}<br>
 * 5、禁用音效功能{@link #disableRecordSoundEffect()}<br>
 * 6、禁用屏比功能{@link #disableAspect()}
 * 7、禁用美颜功能{@link #disableBeauty()}
 * <p>
 * 视频录制UI定制化<br>
 * 1、定制化"录制速度面板" {@link IRecordSpeedLayout} <br>
 * 2、定制化"多段录制进度条" {@link IRecordProgressView} <br>
 * 3、定制化"多模式录制按钮"{@link IRecordButton} <br>
 * 4、定制化"音乐图标" {@link IRecordRightLayout} <br>
 * 5、定制化"屏比图标" {@link IRecordRightLayout} <br>
 * 6、定制化"美颜图标" {@link IRecordRightLayout} <br>
 * 7、定制化"音效图标" {@link IRecordRightLayout}
 * <p>
 * 如果您不使用UGCKit视频录制组件，自行搭建UI，调用SDK功能。<br>
 * 请参照文档
 * <a href="https://cloud.tencent.com/document/product/584/9369">拍照和录制(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20318">多段录制(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20320">录制草稿箱(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20316">添加背景音乐(Android)</a>
 * <a href="https://cloud.tencent.com/document/product/584/20322">变声和混响(Android)</a>
 */
public interface IVideoRecordKit {

    /**
     * 当Activity执行生命周期方法{@code onStart()}时，UGCKit需要执行{@link #start()}来完成如下功能<br>
     * 1、初始化录制配置<br>
     * 2、开始视频录制预览界面
     */
    void start();

    /**
     * 当Activity执行生命周期方法{@code onStop()}时，UGCKit需要执行{@link #stop()}来完成如下功能<br>
     * 1、暂停视频录制<br>
     * 2、关闭视频录制预览界面
     * 3、更新视频录制界面
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
     * 退出视频录制，返回上一界面
     */
    void backPressed();

    /**
     * 设置音乐
     *
     * @param musicInfo
     * @see MusicInfo
     */
    void setRecordMusicInfo(MusicInfo musicInfo);

    /**
     * 设置输入选项
     */
    void setConfig(UGCKitRecordConfig config);

    /**
     * 设置视频录制的监听器
     */
    void setOnRecordListener(OnRecordListener listener);

    /**
     * 设置视频录制背景音乐选择监听器
     */
    void setOnMusicChooseListener(OnMusicChooseListener listener);

    interface OnMusicChooseListener {
        /**
         * 选择音乐
         *
         * @param position 音乐在音乐列表的第几个位置
         */
        void onChooseMusic(int position);
    }

    interface OnRecordListener {
        /**
         * 视频录制被取消
         */
        void onRecordCanceled();

        /**
         * 视频录制完成回调
         *
         * @param
         */
        void onRecordCompleted(UGCKitResult result);
    }

    /************************************************************************/
    /*****                     功能定制化                                 *****/
    /************************************************************************/

    /**
     * 视频录制完是否进行视频特效处理
     *
     * @param enable {@code true} 进行视频特效编辑<br>
     *               {@code false} 直接输出录制完视频<br>
     *               默认为true
     */
    void setEditVideoFlag(boolean enable);

    /**
     * 禁用录制速度功能<br>
     * 1、使用标准速度进行视频录制<br>
     * 2、关闭录制速度面板
     */
    void disableRecordSpeed();

    /**
     * 禁用拍照功能<br>
     * 不显示拍照界面
     */
    void disableTakePhoto();

    /**
     * 禁用长按录制
     */
    void disableLongPressRecord();

    /**
     * 禁用背景音乐功能<br>
     * 1、不显示背景音乐相关UI，包括音乐Icon，音乐面板{@link RecordMusicPannel}<br>
     * 2、背景音乐相关接口不生效，包括<br>
     * 设置音乐选择监听器 {@link #setOnMusicChooseListener(OnMusicChooseListener)},
     * 设置选择好的背景音乐信息 {@link #setRecordMusicInfo(MusicInfo)}
     *
     * @see RecordMusicPannel
     */
    void disableRecordMusic();

    /**
     * 禁用音效功能<br>
     * 1、不显示音效相关UI，包括音效Icon，音效面板{@link SoundEffectsPannel}<br>
     * 2、音效面板包括调整"录音音量"，"混响"，"变声"
     *
     * @see SoundEffectsPannel
     */
    void disableRecordSoundEffect();

    /**
     * 禁用屏比功能<br>
     * 默认使用全屏进行视频录制
     */
    void disableAspect();

    /**
     * 禁用美颜功能
     */
    void disableBeauty();

}

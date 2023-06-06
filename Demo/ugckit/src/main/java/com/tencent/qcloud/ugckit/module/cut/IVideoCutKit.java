package com.tencent.qcloud.ugckit.module.cut;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;

/**
 * 腾讯云短视频UGCKit({@code IVideoCutKit}):视频裁剪。
 * 本组建包含视频裁剪的SDK功能和UI展示，通过拖动裁剪范围，裁剪一个长视频为一个短视频。<br>
 * 您可以通过UGCKit很简单搭建使用视频裁剪功能，也可以定制视频裁剪UI
 * <p>
 * 如下演示了UGCKit视频裁剪模块简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.cut.VideoCut
 *         android:id="@+id/video_cutter_layout"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 * }</pre>
 * <p>
 * 2、在Activity中设置
 * <pre>
 *     &#064;Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_video_cut);
 *
 *         mVideoCut = (VideoCut) findViewById(R.id.video_cutter_layout);
 *         mVideoCut.setVideoPath(mInVideoPath, mVideoResolution, mCustomBitrate);
 *         mVideoCut.setOnCutListener(new IVideoCutKit.OnCutListener() {
 *             &#064;Override
 *             public void onCutterCompleted() {
 *             }
 *             &#064;Override
 *             public void onCutterCanceled() {
 *             }
 *         });
 *     }
 *
 *     &#064;Override
 *     protected void onResume() {
 *         super.onResume();
 *         mVideoCut.startPlay();
 *     }
 *
 *     &#064;Override
 *     protected void onPause() {
 *         super.onPause();
 *         mVideoCut.stopPlay();
 *     }
 *
 *     &#064;Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         mVideoCut.release();
 *     }
 * </pre>
 * <p>
 * 视频裁剪功能定制化<br>
 * 1、设置是否下一步进行视频编辑，{@link #setVideoEditFlag(boolean)}<br>
 * 若进行视频编辑，则设置为{@code true}，不输出裁剪后的视频路径，{@code IVideoCutKit} 同时进行视频裁剪和视频预处理。下一个页面必跳转到"视频编辑页面"<br>
 * 若不进行视频编辑，则设置为{@code false}，输出裁剪并压缩的视频路径。
 * <p>
 * 视频裁剪UI定制化<br>
 * 1、定制化"裁剪View" {@link IVideoCutLayout} <br>
 * <p>
 * 如果您不使用UGCKit视频裁剪组件，自行搭建UI，调用SDK功能。<br>
 * 请参照文档<a href="https://cloud.tencent.com/document/product/584/9502">视频编辑(Android)</a> 中"压缩裁剪"
 *
 * Custom Video Clipping Features<br>
 * 1. Call {@link #setVideoEditFlag(boolean)} to specify whether to enter the editing view after clipping.<br>
 * Use {@code true} if you want to enter the editing view, in which case the video path
 * will not be returned after clipping. {@code IVideoCutKit} will clip and preprocess
 * the video at the same time and enter the editing view after clipping.<br>
 * Use {@code false} if you do not want to enter the editing view, in which case the
 * video path will be returned after clipping.
 * <p>
 * Custom Video Clipping UI<br>
 * 1. Call {@link IVideoCutLayout} to customize the clipping view.<br>
 * <p>
 * If you build your own UI instead of using the clipping component of UGCKit,
 * refer to the following document to call the SDK’s APIs.<br>
 * <a href="https://www.tencentcloud.com/document/product/1069/38024">
 *     Video Editing (Android)</a> (the “Compression and Clipping” section)
 */


public interface IVideoCutKit {

    /**
     * 设置视频裁剪的源路径
     *
     * @param videoPath 视频裁剪的源路径
     */
    void setVideoPath(String videoPath);

    /**
     * 开始播放视频
     */
    void startPlay();

    /**
     * 停止播放视频
     */
    void stopPlay();

    /**
     * 释放资源
     */
    void release();

    /**
     * 设置视频裁剪的监听器
     */
    void setOnCutListener(OnCutListener listener);

    interface OnCutListener {
        /**
         * 视频裁剪操作完成
         */
        void onCutterCompleted(UGCKitResult ugcKitResult);

        /**
         * 视频裁剪操作取消
         */
        void onCutterCanceled();
    }

    /************************************************************************/
    /*****                     功能定制化                                 *****/
    /************************************************************************/

    /**
     * 是否进行视频编辑
     *
     * @param enable {@code true} 进行视频特效编辑<br>
     *               {@code false} 直接输出裁剪完视频<br>
     *               默认为true
     */
    void setVideoEditFlag(boolean enable);

    /**
     * 仅当调用 {@link #setVideoEditFlag(boolean)} 参数为 {@code false} 时，有效
     *
     * @return 裁剪完的视频路径
     */
    String getVideoOutputPath();
}

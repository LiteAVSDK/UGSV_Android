package com.tencent.qcloud.ugckit.module.upload;

import com.tencent.qcloud.ugckit.UGCKitVideoPublish;

/**
 * 腾讯云短视频UGCKit({@code TCVideoPublishKit}):视频发布。
 * 本组件包含视频封面和视频本身上传到cos并发布到点播服务器。
 * 如下演示了UGCKit视频发布模块简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.upload.VideoPublishLayout
 *         android:id="@+id/video_publish_layout"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 * }</pre>
 * <p>
 * 2、在Activity中设置
 * <pre>
 *      mVideoPublishLayout = (VideoPublishLayout) findViewById(R.id.video_publish_layout);
 *      mVideoPublishLayout.setPublishPath(mVideoPath, mCoverPath);
 *      mVideoPublishLayout.setCacheEnable(mDisableCache);
 *      mVideoPublishLayout.setOnPublishListener(new TCVideoPublishKit.OnPublishListener(){
 *
 *          &#064;Override
 *          public void onPublishCompleted() {
 *          }
 *
 *          &#064;Override
 *          public void onPublishCanceled() {
 *          }
 *      });
 * </pre>
 * <p>
 * <p>
 * UGCKit视频发布模块<br>
 * 1、设置待发布视频的路径和封面 {@link #setPublishPath(String, String)}<br>
 * 2、设置发布视频的监听器{@link #setOnPublishListener(OnPublishListener)} <br>
 * 3、当发布视频完成，{@link OnPublishListener#onPublishCompleted()} 被调用 <br>
 * 4、当视频发布被取消，{@link OnPublishListener#onPublishCanceled()} 被调用 <br>
 * <p>
 * 可选功能：<br>
 * 1、是否开启本地缓存{@link #setCacheEnable(boolean)}<br>
 * 若关闭本地缓存，则发布完成后删除"已发布"的视频和封面；<br>
 * 若开启本地缓存，则设置的视频文件和封面文件不会被删除。<br>
 * 默认开启本地缓存 {@code setCacheEnable(true)}
 * <p>
 * 如果您不使用UGCKit视频发布组建，自行搭建UI，调用SDK功能。<br>
 * 请参考文档  <a href="https://cloud.tencent.com/document/product/584/15535">视频上传(Android)</a>
 * </p>
 *
 * @see UGCKitVideoPublish
 */
public interface TCVideoPublishKit {
    /**
     * 设置发布视频的路径和封面<br>
     * 注意：请检查路径是否正确
     *
     * @param videoPath 视频的路径
     * @param coverPath 封面的路径
     */
    void setPublishPath(String videoPath, String coverPath);

    /**
     * 开启本地缓存，若关闭本地缓存，则发布完成后删除"已发布"的视频和封面
     *
     * @param enable {@code true} 开启本地缓存，设置的视频文件和封面文件不会被删除。<br>
     *               {@code false} 关闭本地缓存，则发布完成后删除"已发布"的视频和封面；<br>
     *               默认为true
     */
    void setCacheEnable(boolean enable);

    /**
     * 设置发布视频的监听器
     *
     * @param onUIClickListener
     */
    void setOnPublishListener(TCVideoPublishKit.OnPublishListener onUIClickListener);

    interface OnPublishListener {
        /**
         * 视频发布完成
         */
        void onPublishCompleted();

        /**
         * 视频发布取消
         */
        void onPublishCanceled();
    }
}

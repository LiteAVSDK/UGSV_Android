package com.tencent.qcloud.ugckit.module.picturetransition;

import com.tencent.qcloud.ugckit.basic.UGCKitResult;

import java.util.List;

/**
 * 腾讯云短视频UGCKit({@code TCPictureTransitionKit}):图片合成。<br>
 * 本组件功能包含多张图片设置转场动画，生成一个视频文件。<br>
 * 您可以通过UGCKit很简单搭建图片合成功能
 * <p>
 * 如下演示了UGCKit图片合成简单用法：<br>
 * 1、在xml中设置
 * <pre>
 * {@code
 * <com.tencent.qcloud.xiaoshipin.uikit.module.picturetransition.PictureJoin
 *     android:id="@+id/picture_transition"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * }</pre>
 * <p>
 * 2、在Activity中设置
 * <pre>
 *      &#064;Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *        super.onCreate(savedInstanceState);
 *        setContentView(R.layout.picture_join_layout);
 *
 *        mPicPathList = getIntent().getStringArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_PIC_LIST);
 *        mPictureTransition = (PictureJoin) findViewById(R.id.picture_transition);
 *        mPictureTransition.setInputPictureList(mPicPathList);
 *        mPictureTransition.setOnPictureJoinListener(new IPictureJoinKit.OnPictureJoinListener() {
 *
 *            &#064;Override
 *            public void onPictureJoinCompleted() {
 *            }
 *
 *            &#064;Override
 *            public void onPictureJoinCanceled() {
 *            }
 *        });
 *    }
 *
 *    &#064;Override
 *    protected void onResume() {
 *        super.onResume();
 *        mPictureTransition.resumePlay();
 *    }
 *
 *    &#064;Override
 *    protected void onPause() {
 *        super.onPause();
 *        mPictureTransition.pausePlay();
 *    }
 * }
 * </pre>
 * <p>
 * 图片合成UI定制化<br>
 * 1、定制化图片转场Icon {@link IPictureTransitionLayout}
 * <p>
 * https://cloud.tencent.com/document/product/584/20328
 */
public interface IPictureJoinKit {

    void resumePlay();

    void pausePlay();

    void stopPlay();

    void release();

    /**
     * 设置图片列表数据
     */
    void setInputPictureList(List<String> pictureList);

    /**
     * 设置图片合成的监听器
     */
    void setOnPictureJoinListener(IPictureJoinKit.OnPictureJoinListener listener);

    interface OnPictureJoinListener {
        /**
         * 图片合成完成
         *
         * @param ugcKitResult 生成视频输出路径
         */
        void onPictureJoinCompleted(UGCKitResult ugcKitResult);

        /**
         * 图片合成操作取消
         */
        void onPictureJoinCanceled();
    }

    /************************************************************************/
    /*****                     UI定制化                                  *****/
    /************************************************************************/
    /**
     * 获取转场设置工具栏
     */
    PictureTransitionLayout getPictureTransitionLayout();

}

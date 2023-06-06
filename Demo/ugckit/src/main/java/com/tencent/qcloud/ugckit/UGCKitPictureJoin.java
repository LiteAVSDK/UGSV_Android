package com.tencent.qcloud.ugckit;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.PictureGenerateKit;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.picturetransition.AbsPictureJoinUI;
import com.tencent.qcloud.ugckit.module.picturetransition.IPictureJoinKit;
import com.tencent.qcloud.ugckit.module.picturetransition.IPictureTransitionLayout;
import com.tencent.qcloud.ugckit.module.picturetransition.PictureTransitionKit;
import com.tencent.qcloud.ugckit.utils.BitmapUtils;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云短视频UGCKit:图片转场动画控件</p>
 * <p>
 * PictureJoinLayout功能： <p>
 * 1、设置图片转场的的动画 <p>
 * 2、多张图片加入转场动画后生成一个视频，每张图片显示三秒钟
 * <p>
 * PictureJoinLayout用法：
 * 1、调用{@link UGCKitPictureJoin#setInputPictureList(List)} (ArrayList)} 传入多张图片作为视频的输入源<p>
 * 2、调用{@link IPictureJoinKit.OnPictureJoinListener} 监听视频合成的状态<p>
 * {@link IPictureJoinKit.OnPictureJoinListener#onPictureJoinCompleted(UGCKitResult)} 表示视频合成完成
 * {@link IPictureJoinKit.OnPictureJoinListener#onPictureJoinCanceled()} ()} 表示当前合成视频动作取消。
 * <p>
 * SDK调用步骤：</p>
 * 1、调用{@link TXVideoEditer#setPictureList(List, int)} 设置图片路径集合</p>
 * 2、调用{@link TXVideoEditer#setPictureTransition(int)} 设置图片转场动画</p>
 * 3、调用{@link TXVideoEditer#setVideoGenerateListener(TXVideoEditer.TXVideoGenerateListener)} 设置生成视频的监听器</p>
 * 4、调用{@link TXVideoEditer#generateVideo(int, String)} 将多张图片合成一个视频</p>
 * <p>
 * SDK注意事项：
 * 1、必须按照SDK调用步骤来使用
 * 2、图片转视频的时长需要设置转场类型后获取，因为不同的转场类型时长会不一样</p>
 * 3、宽高信息sdk内部会处理成9：16比例，上层只有在加片尾水印的时候算归一化坐标用到，所以这里可以设置成720P（720 * 1280）或者540P（540 * 960）来计算。
 * 注意最终视频的分辨率是按照生成时传的参数决定的。</p>
 * 4、SDK版本更新说明：5.0以前版本是按照第一张图片的宽高来决定最终的宽高，导致的问题是如果第一张图片有一边比较短，后面的图片会以最短边等比例缩放，显示出来就小了</p>
 *
 * Tencent Cloud UGCKit: Image Transitions</p>
 * <p>
 * PictureJoinLayout capabilities: <p>
 * 1. Set image transition effects. <p>
 * 2. Generate a video after the transition effects are applied (each image is shown for three seconds).
 * <p>
 * Using PictureJoinLayout:
 * 1. Call {@link UGCKitPictureJoin#setInputPictureList(List)} (ArrayList)} to pass in multiple images.<p>
 * 2. Call {@link IPictureJoinKit.OnPictureJoinListener} to listen for the video generation status.<p>
 * {@link IPictureJoinKit.OnPictureJoinListener#onPictureJoinCompleted(UGCKitResult)}
 * indicates that the video has been generated.
 * {@link IPictureJoinKit.OnPictureJoinListener#onPictureJoinCanceled()} ()}
 * indicates that video generation has been canceled.
 * <p>
 * Directions:</p>
 * 1. Call {@link TXVideoEditer#setPictureList(List, int)} to specify image paths.</p>
 * 2. Call {@link TXVideoEditer#setPictureTransition(int)} to set transition effects.</p>
 * 3. Call {@link TXVideoEditer#setVideoGenerateListener(TXVideoEditer.TXVideoGenerateListener)}
 * to set the video generation listener.</p>
 * 4. Call {@link TXVideoEditer#generateVideo(int, String)} to create a video from the images.</p>
 * <p>
 * Notes:
 * 1. Make sure you call the APIs in the specified order.
 * 2. Because transition effects differ in duration,
 * the video length can be obtained only after transition effects are configured.
 * 3. Internally, the SDK uses the aspect ratio 9:16, which the upper layer uses
 * to normalize the coordinates of closing segment watermarks. You can set the resolution
 * to 720p (720 x 1280) or 540p (540 x 960).
 * The resolution of the output video is determined by the parameters passed in.</p>
 * 4. In versions earlier than v5.0, the video resolution is determined by the first image’s aspect ratio.
 * The problem of this is that if the first image has a particularly short dimension,
 * the other images will be scaled to the same dimension, and the video generated will be small.</p>
 */


public class UGCKitPictureJoin extends AbsPictureJoinUI {
    private static final String TAG = "UGCKitPictureJoin";

    private ProgressFragmentUtil mProgressFragmentUtil;

    public UGCKitPictureJoin(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitPictureJoin(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitPictureJoin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    public void initDefault() {
        mProgressFragmentUtil = new ProgressFragmentUtil((FragmentActivity) getContext());
        // 初始化SDK:TXVideoEditer
        VideoEditerSDK.getInstance().initSDK();

        // 点击"工具栏"切换转场动画
        getPictureTransitionLayout().setTransitionListener(new IPictureTransitionLayout.OnTransitionListener() {
            @Override
            public void transition(int type) {
                Log.i(TAG, "transition type:" + type);
                initTransition(type);
            }
        });

        // 点击"下一步"
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableRightButton(false);
                mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
                    @Override
                    public void onStop() {
                        // 点击进度条"X"取消生成
                        mProgressFragmentUtil.dismissLoadingProgress();
                        setEnableRightButton(true);
                        PictureGenerateKit.getInstance().stopGenerate();
                        PlayerManagerKit.getInstance().startPlay();
                    }
                });
                PlayerManagerKit.getInstance().stopPlay();
                PictureGenerateKit.getInstance().startGenerate();
            }
        });
    }

    /**
     * 设置图片转场动画
     *
     * @param type
     */
    private void initTransition(int type) {
        long duration = PictureTransitionKit.getInstance().pictureTransition(type);
        Log.d(TAG, "initTransition duration:" + duration);

        PlayerManagerKit.getInstance().stopPlay();
        // 设置视频基本信息
        TXVideoEditConstants.TXVideoInfo videoInfo = new TXVideoEditConstants.TXVideoInfo();
        VideoEditerSDK.getInstance().setCutterStartTime(0, duration);
        VideoEditerSDK.getInstance().setVideoDuration(duration);
        VideoEditerSDK.getInstance().constructVideoInfo(videoInfo, duration);

        PlayerManagerKit.getInstance().startPlay();
    }

    @Override
    public void setInputPictureList(@Nullable List<String> pictureList) {
        if (pictureList == null || pictureList.size() == 0) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_picture_choose_activity_please_select_multiple_images));
            return;
        }
        ArrayList<Bitmap> bitmapList = BitmapUtils.decodeFileToBitmap(pictureList);

        int retCode = PictureTransitionKit.getInstance().setPictureList(bitmapList);
        if (retCode == TXVideoEditConstants.PICTURE_TRANSITION_FAILED) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_tc_picture_join_activity_toast_picture_is_abnormal_and_finish_editing));

            PictureGenerateKit.getInstance().stopGenerate();
            return;
        }
        // 初始化播放器界面[必须在setPictureList/setVideoPath设置数据源之后]
        getVideoPlayLayout().initPlayerLayout();

        // 设置默认的图片转场动画[左右切换]
        initTransition(PictureTransitionKit.DEFAULT_TRANSITION);
    }

    @Override
    public void setOnPictureJoinListener(@Nullable final IPictureJoinKit.OnPictureJoinListener listener) {
        if (listener == null) {
            PictureGenerateKit.getInstance().setOnUpdateUIListener(null);
            return;
        }
        // 设置生成的监听器，用来更新"UI控件" 和 Activity
        PictureGenerateKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
            @Override
            public void onUIProgress(float progress) {
                mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
            }

            @Override
            public void onUIComplete(int retCode, String descMsg) {
                // 更新UI控件
                mProgressFragmentUtil.dismissLoadingProgress();

                // 更新Activity
                if (listener != null) {
                    UGCKitResult ugcKitResult = new UGCKitResult();
                    ugcKitResult.errorCode = retCode;
                    ugcKitResult.descMsg = descMsg;
                    ugcKitResult.outputPath = PictureGenerateKit.getInstance().getVideoOutputPath();
                    ugcKitResult.coverPath = PictureGenerateKit.getInstance().getCoverPath();
                    listener.onPictureJoinCompleted(ugcKitResult);
                }
                setEnableRightButton(true);
            }

            @Override
            public void onUICancel() {
                // 更新Activity
                if (listener != null) {
                    listener.onPictureJoinCanceled();
                }
            }
        });
    }

    private void setEnableRightButton(boolean isOpen) {
        if(null != getTitleBar()) {
            getTitleBar().getRightButton().setEnabled(isOpen);
        }
    }

    @Override
    public void resumePlay() {
        PlayerManagerKit.getInstance().resumePlay();
    }

    @Override
    public void pausePlay() {
        PlayerManagerKit.getInstance().pausePlay();
    }

    @Override
    public void stopPlay() {
        PlayerManagerKit.getInstance().stopPlay();
    }

    @Override
    public void release() {
        VideoEditerSDK.getInstance().releaseSDK();
    }
}

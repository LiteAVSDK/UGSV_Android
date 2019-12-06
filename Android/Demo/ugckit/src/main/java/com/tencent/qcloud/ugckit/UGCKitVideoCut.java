package com.tencent.qcloud.ugckit;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.qcloud.ugckit.UGCKitImpl;
import com.tencent.qcloud.ugckit.basic.JumpActivityMgr;
import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.ProcessKit;
import com.tencent.qcloud.ugckit.module.VideoGenerateKit;
import com.tencent.qcloud.ugckit.module.cut.AbsVideoCutUI;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutLayout;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.TelephonyUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

/**
 * 腾讯云短视频TUIKit:视频裁剪控件
 * <p>
 * 功能：用于实现长时间视频裁剪其中一段生成一段短时间的视频。<p/>
 */
public class UGCKitVideoCut extends AbsVideoCutUI implements PlayerManagerKit.OnPreviewListener {
    private static final String TAG = "UGCKitVideoCut";
    private ProgressDialogUtil mProgressDialogUtil;
    private ProgressFragmentUtil mProgressFragmentUtil;

    public UGCKitVideoCut(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoCut(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoCut(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    private void initDefault() {
        mProgressDialogUtil = new ProgressDialogUtil(getContext());
        mProgressFragmentUtil = new ProgressFragmentUtil((FragmentActivity) getContext(), getResources().getString(R.string.video_cutting));

        VideoEditerSDK.getInstance().clear();
        VideoEditerSDK.getInstance().initSDK();

        // 点击"下一步"
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
                    @Override
                    public void onStop() {
                        mProgressFragmentUtil.dismissLoadingProgress();
                        boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
                        if (editFlag) {
                            ProcessKit.getInstance().stopProcess();
                        } else {
                            VideoGenerateKit.getInstance().stopGenerate();
                        }
                        PlayerManagerKit.getInstance().startPlay();
                    }
                });
                PlayerManagerKit.getInstance().stopPlay();

                boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
                if (editFlag) {
                    ProcessKit.getInstance().startProcess();
                } else {
                    VideoGenerateKit.getInstance().startGenerate();
                }
            }
        });
        // 监听电话
        TelephonyUtil.getInstance().initPhoneListener();
    }

    @Override
    public void setVideoPath(final String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.tc_video_cutter_activity_oncreate_an_unknown_error_occurred_the_path_cannot_be_empty));
            return;
        }

        VideoEditerSDK.getInstance().setVideoPath(videoPath);
        // 初始化播放器界面[必须在setPictureList/setVideoPath设置数据源之后]
        getVideoPlayLayout().initPlayerLayout();

        // 显示圆形进度条
        mProgressDialogUtil.showProgressDialog();
        // 加载视频基本信息
        loadVideoInfo(videoPath);
        // 圆形进度条消失
        mProgressDialogUtil.dismissProgressDialog();
    }

    private void loadVideoInfo(String videoPath) {
        // 加载视频信息
        TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(videoPath);
        if (info == null) {
            DialogUtil.showDialog(
                    UGCKitImpl.getAppContext(), getResources().getString(R.string.tc_video_cutter_activity_video_main_handler_edit_failed),
                    getResources().getString(R.string.tc_video_cutter_activity_video_main_handler_does_not_support_android_version_below_4_3), null);
        } else {
            VideoEditerSDK.getInstance().setTXVideoInfo(info);
            getVideoCutLayout().setVideoInfo(info);
            getVideoCutLayout().setOnRotateVideoListener(new IVideoCutLayout.OnRotateVideoListener() {
                @Override
                public void onRotate(int rotation) {
                    VideoEditerSDK.getInstance().getEditer().setRenderRotation(rotation);
                }
            });
            // 初始化缩略图列表
            VideoEditerSDK.getInstance().initThumbnailList(new TXVideoEditer.TXThumbnailListener() {
                @Override
                public void onThumbnail(final int index, long timeMs, final Bitmap bitmap) {
                    TXLog.d(TAG, "onThumbnail index:" + index + ",timeMs:" + timeMs);
                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getVideoCutLayout().addThumbnail(index, bitmap);
                        }
                    });
                }
            });
            // 播放视频
            PlayerManagerKit.getInstance().startPlayCutTime();
        }
    }

    @Override
    public void setOnCutListener(@Nullable final OnCutListener listener) {
        boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
        // 设置生成的监听器，用来更新控件
        if (editFlag) {
            // 裁剪后进入编辑
            ProcessKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
                @Override
                public void onUIProgress(float progress) {
                    mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
                }

                @Override
                public void onUIComplete(int retCode, String descMsg) {
                    mProgressFragmentUtil.dismissLoadingProgress();

                    if (listener != null) {
                        UGCKitResult ugcKitResult = new UGCKitResult();
                        ugcKitResult.errorCode = retCode;
                        ugcKitResult.descMsg = descMsg;
                        listener.onCutterCompleted(ugcKitResult);
                    }
                }

                @Override
                public void onUICancel() {
                    if (listener != null) {
                        listener.onCutterCanceled();
                    }
                }
            });
        } else {
            // 裁剪后输出视频
            VideoGenerateKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
                @Override
                public void onUIProgress(float progress) {
                    mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
                }

                @Override
                public void onUIComplete(int retCode, String descMsg) {
                    mProgressFragmentUtil.dismissLoadingProgress();

                    if (listener != null) {
                        UGCKitResult ugcKitResult = new UGCKitResult();
                        ugcKitResult.errorCode = retCode;
                        ugcKitResult.descMsg = descMsg;
                        ugcKitResult.outputPath = VideoGenerateKit.getInstance().getVideoOutputPath();
                        ugcKitResult.coverPath = VideoGenerateKit.getInstance().getCoverPath();
                        listener.onCutterCompleted(ugcKitResult);
                    }
                }

                @Override
                public void onUICancel() {
                    if (listener != null) {
                        listener.onCutterCanceled();
                    }
                }
            });
        }
    }

    @Override
    public void startPlay() {
        PlayerManagerKit.getInstance().addOnPreviewLitener(this);
        PlayerManagerKit.getInstance().startPlay();
    }

    @Override
    public void stopPlay() {
        PlayerManagerKit.getInstance().stopPlay();
        PlayerManagerKit.getInstance().removeOnPreviewListener(this);
        boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
        if (editFlag) {
            ProcessKit.getInstance().stopProcess();
        } else {
            VideoGenerateKit.getInstance().stopGenerate();
        }
        mProgressFragmentUtil.dismissLoadingProgress();
    }

    @Override
    public void release() {
        TelephonyUtil.getInstance().uninitPhoneListener();
    }

    @Override
    public void setVideoEditFlag(boolean flag) {
        JumpActivityMgr.getInstance().setEditFlagFromCut(flag);
    }

    @Override
    public String getVideoOutputPath() {
        return VideoGenerateKit.getInstance().getVideoOutputPath();
    }

    @Override
    public void onPreviewProgress(int time) {

    }

    @Override
    public void onPreviewFinish() {
        // 循环播放
        PlayerManagerKit.getInstance().startPlay();
    }
}

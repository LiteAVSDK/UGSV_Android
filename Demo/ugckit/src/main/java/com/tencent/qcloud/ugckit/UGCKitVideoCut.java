package com.tencent.qcloud.ugckit;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

/**
 * 腾讯云短视频UGCKit:视频裁剪控件
 * <p>
 * 功能：用于实现长时间视频裁剪其中一段生成一段短时间的视频。<p/>
 */
public class UGCKitVideoCut extends AbsVideoCutUI implements PlayerManagerKit.OnPreviewListener {
    private static final String TAG = "UGCKitVideoCut";

    private ProgressDialogUtil   mProgressDialogUtil;
    private ProgressFragmentUtil mProgressFragmentUtil;
    private boolean              mComplete = false;

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
        mProgressFragmentUtil = new ProgressFragmentUtil((FragmentActivity) getContext(), getResources().getString(R.string.ugckit_video_cutting));

        VideoEditerSDK.getInstance().releaseSDK();
        VideoEditerSDK.getInstance().clear();
        VideoEditerSDK.getInstance().initSDK();

        // 点击"下一步"
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableRightButton(false);
                mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
                    @Override
                    public void onStop() {
                        // 取消裁剪
                        mProgressFragmentUtil.dismissLoadingProgress();
                        setEnableRightButton(true);
                        boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
                        if (editFlag) {
                            ProcessKit.getInstance().stopProcess();
                        } else {
                            VideoGenerateKit.getInstance().stopGenerate();
                        }
                        PlayerManagerKit.getInstance().startPlay();
                        // 未加载完缩略图，重新进行加载
                        if (!mComplete) {
                            Log.i(TAG, "[UGCKit][VideoCut]last load uncomplete, reload thunmail");
                            loadThumbnail();
                        }
                    }
                });
                PlayerManagerKit.getInstance().stopPlay();
                //如果图片没有加载完，先停止加载
                ProcessKit.getInstance().stopProcess();

                boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromCut();
                if (editFlag) {
                    ProcessKit.getInstance().startProcess();
                } else {
                    VideoGenerateKit.getInstance().startGenerate();
                }
            }
        });
    }

    @Override
    public void setVideoPath(final String videoPath) {
        Log.i(TAG, "[UGCKit][VideoCut]setVideoPath:" + videoPath);
        if (TextUtils.isEmpty(videoPath)) {
            ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_video_cutter_activity_oncreate_an_unknown_error_occurred_the_path_cannot_be_empty));
            return;
        }

        VideoEditerSDK.getInstance().setVideoPath(videoPath);
        // 初始化播放器界面[必须在setPictureList/setVideoPath设置数据源之后]
        getVideoPlayLayout().initPlayerLayout();

        // 显示圆形进度条
        mProgressDialogUtil.showProgressDialog();

        // 重新设置路径，缩略图重新加载
        mComplete = false;
        // 加载视频基本信息
        loadVideoInfo(videoPath);
        // 圆形进度条消失
        mProgressDialogUtil.dismissProgressDialog();
    }

    private void loadVideoInfo(String videoPath) {
        // 加载视频信息
        TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance(UGCKit.getAppContext()).getVideoFileInfo(videoPath);
        if (info == null) {
            DialogUtil.showDialog(getContext(), getResources().getString(R.string.ugckit_video_cutter_activity_video_main_handler_edit_failed), getResources().getString(R.string.ugckit_does_not_support_android_version_below_4_3), null);
        } else {
            VideoEditerSDK.getInstance().setTXVideoInfo(info);
            getVideoCutLayout().setVideoInfo(info);
            getVideoCutLayout().setOnRotateVideoListener(new IVideoCutLayout.OnRotateVideoListener() {
                @Override
                public void onRotate(int rotation) {
                    VideoEditerSDK.getInstance().getEditer().setRenderRotation(rotation);
                }
            });
            Log.i(TAG, "[UGCKit][VideoCut]load thunmail");
            loadThumbnail();
            // 播放视频
            PlayerManagerKit.getInstance().startPlayCutTime();
        }
    }

    private void loadThumbnail() {
        // 初始化缩略图列表，裁剪缩略图时间间隔3秒钟一张
        getVideoCutLayout().clearThumbnail();
        final int interval = 3000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int count = (int) (VideoEditerSDK.getInstance().getVideoDuration() / interval);
                VideoEditerSDK.getInstance().initThumbnailList(new TXVideoEditer.TXThumbnailListener() {
                    @Override
                    public void onThumbnail(final int index, long timeMs, final Bitmap bitmap) {
                        Log.d(TAG, "onThumbnail index:" + index + ",timeMs:" + timeMs);
                        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getVideoCutLayout().addThumbnail(index, bitmap);
                                if (index >= count - 1) { // Note: index从0开始增长
                                    Log.i(TAG, "Load Thumbnail Complete");
                                    mComplete = true;
                                }
                            }
                        });
                    }
                }, interval);
            }
        }).start();
    }

    @Override
    public void setOnCutListener(@Nullable final OnCutListener listener) {
        if (listener == null) {
            ProcessKit.getInstance().setOnUpdateUIListener(null);
            VideoGenerateKit.getInstance().setOnUpdateUIListener(null);
            return;
        }
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
                    setEnableRightButton(true);
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
                    setEnableRightButton(true);
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
        setEnableRightButton(true);
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

    private void setEnableRightButton(boolean isOpen) {
        if(null != getTitleBar()) {
            getTitleBar().getRightButton().setEnabled(isOpen);
        }
    }

    @Override
    public void onPreviewFinish() {
        // 循环播放
        PlayerManagerKit.getInstance().startPlay();
    }

    public void onBackPressed() {
        VideoEditerSDK.getInstance().releaseSDK();
        VideoEditerSDK.getInstance().clear();
    }
}

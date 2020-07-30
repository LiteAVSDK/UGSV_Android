package com.tencent.qcloud.ugckit;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;


import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.basic.JumpActivityMgr;
import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.module.VideoGenerateKit;
import com.tencent.qcloud.ugckit.module.editer.AbsVideoEditUI;
import com.tencent.qcloud.ugckit.module.editer.UGCKitEditConfig;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.TelephonyUtil;
import com.tencent.qcloud.ugckit.component.dialog.ActionSheetDialog;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.effect.Config;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

public class UGCKitVideoEdit extends AbsVideoEditUI {
    private static final String TAG = "UGCKitVideoEdit";

    private ProgressFragmentUtil mProgressFragmentUtil;
    @Nullable
    private OnEditListener       mOnEditListener;
    private boolean              mIsPublish;

    public UGCKitVideoEdit(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    private void initDefault() {
        mProgressFragmentUtil = new ProgressFragmentUtil((FragmentActivity) getContext());

        // 点击"完成"
        getTitleBar().setTitle(getResources().getString(R.string.ugckit_complete), ITitleBarLayout.POSITION.RIGHT);
        getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPublishDialog();
            }
        });
        // 监听电话
        TelephonyUtil.getInstance().setOnTelephoneListener(new TelephonyUtil.OnTelephoneListener() {
            @Override
            public void onRinging() {
                // 生成状态 取消生成
                stopGenerate();
                // 直接停止播放
                PlayerManagerKit.getInstance().stopPlay();
            }

            @Override
            public void onOffhook() {
                stopGenerate();
                // 直接停止播放
                PlayerManagerKit.getInstance().stopPlay();
            }

            @Override
            public void onIdle() {
                // 重新开始播放
                PlayerManagerKit.getInstance().restartPlay();
            }
        });
        TelephonyUtil.getInstance().initPhoneListener();

        // 设置默认为全功能导入视频方式
        JumpActivityMgr.getInstance().setQuickImport(false);
    }

    @Override
    public void backPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder.setTitle(getContext().getString(R.string.ugckit_tips)).setCancelable(false).setMessage(R.string.ugckit_confirm_cancel_edit_content)
                .setPositiveButton(R.string.ugckit_btn_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TXCLog.i(TAG,"[UGCKit][VideoEdit]backPressed call stopPlay");
                        PlayerManagerKit.getInstance().stopPlay();
                        // 取消设置的特效
                        VideoEditerSDK.getInstance().releaseSDK();
                        VideoEditerSDK.getInstance().clear();
                        if (mOnEditListener != null) {
                            mOnEditListener.onEditCanceled();
                        }
                    }
                })
                .setNegativeButton(getContext().getString(R.string.ugckit_wrong_click), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 快速导入始终 {@code setVideoPath}
     * 全功能导入不使用此方法
     *
     * @param videoPath 视频裁剪的源路径
     */
    @Override
    public void setVideoPath(String videoPath) {
        // 获取TXVideoEditer，兼容"快速导入"之前没有初始化TXVideoEditer；"全功能导入"，裁剪时已经预处理了视频，此时初始化了TXVideoEditer
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer == null) {
            VideoEditerSDK.getInstance().initSDK();
        }
        TXCLog.i(TAG, "[UGCKit][VideoEdit][QuickImport]setVideoPath:" + videoPath);
        VideoEditerSDK.getInstance().setVideoPath(videoPath);

        // 获取TXVideoInfo，兼容"快速导入"新传入videoPath，之前没有获取视频信息;"全功能导入"，裁剪时已经获取视频基本信息。
        TXVideoEditConstants.TXVideoInfo info = VideoEditerSDK.getInstance().getTXVideoInfo();
        if (info == null) {
            // 从"录制"进入，录制勾选了"进入编辑"，info在录制界面已设置，此处不为null
            // 从"录制"进入，录制不勾选"进入编辑"，info为null，需要重新获取
            info = TXVideoInfoReader.getInstance(UGCKit.getAppContext()).getVideoFileInfo(videoPath);
            VideoEditerSDK.getInstance().setTXVideoInfo(info);
        }

        // 初始化缩略图列表，编辑缩略图1秒钟一张(先清空缩略图列表)
        VideoEditerSDK.getInstance().clearThumbnails();

        long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long endTime = VideoEditerSDK.getInstance().getCutterEndTime();
        if (endTime > startTime) {
            TXCLog.i(TAG, "[UGCKit][VideoEdit][QuickImport]load thumbnail start time:" + startTime + ",end time:" + endTime);
        }

        VideoEditerSDK.getInstance().setCutterStartTime(0, info.duration);
        VideoEditerSDK.getInstance().initThumbnailList(new TXVideoEditer.TXThumbnailListener() {
            @Override
            public void onThumbnail(final int index, long timeMs, final Bitmap bitmap) {
                TXLog.d(TAG, "onThumbnail index:" + index + ",timeMs:" + timeMs);
                VideoEditerSDK.getInstance().addThumbnailBitmap(timeMs, bitmap);
            }
        }, 1000);

        JumpActivityMgr.getInstance().setQuickImport(true);
    }

    /**
     * 显示发布对话框
     */
    private void showPublishDialog() {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(getContext());
        actionSheetDialog.builder();
        actionSheetDialog.setCancelable(false);
        actionSheetDialog.setCancelable(false);
        actionSheetDialog.addSheetItem(getResources().getString(R.string.ugckit_video_editer_activity_show_publish_dialog_save),
                ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        VideoEditerSDK.getInstance().setPublishFlag(false);
                        startGenerate();
                    }
                });

        if (mIsPublish) {
            actionSheetDialog.addSheetItem(getResources().getString(R.string.ugckit_video_editer_activity_show_publish_dialog_publish),
                    ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            VideoEditerSDK.getInstance().setPublishFlag(true);
                            startGenerate();
                        }

                    });
        }
        actionSheetDialog.show();
    }

    @Override
    public void initPlayer() {
        getVideoPlayLayout().initPlayerLayout();

        VideoEditerSDK.getInstance().resetDuration();
    }

    @Override
    public void setConfig(UGCKitEditConfig config) {
        VideoGenerateKit.getInstance().setCustomVideoBitrate(config.videoBitrate);
        VideoGenerateKit.getInstance().setVideoResolution(config.resolution);
        VideoGenerateKit.getInstance().setCoverGenerate(config.isCoverGenerate);
        VideoGenerateKit.getInstance().saveVideoToDCIM(config.isSaveToDCIM);
        VideoGenerateKit.getInstance().setWaterMark(config.mWaterMarkConfig);
        VideoGenerateKit.getInstance().setTailWaterMark(config.mTailWaterMarkConfig);
        mIsPublish = config.isPublish;
    }

    @Override
    public void start() {
        KeyguardManager manager = (KeyguardManager) UGCKit.getAppContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (!manager.inKeyguardRestrictedInputMode()) {
            PlayerManagerKit.getInstance().restartPlay();
        }
    }

    @Override
    public void stop() {
        TXCLog.i(TAG,"[UGCKit][VideoEdit]onStop call stopPlay");
        PlayerManagerKit.getInstance().stopPlay();

        stopGenerate();
    }

    @Override
    public void release() {
        Config.getInstance().clearConfig();
        TelephonyUtil.getInstance().uninitPhoneListener();
    }

    @Override
    public void setOnVideoEditListener(@Nullable final OnEditListener listener) {
        if (listener == null) {
            mOnEditListener = null;
            VideoGenerateKit.getInstance().setOnUpdateUIListener(null);
            return;
        }
        mOnEditListener = listener;

        VideoGenerateKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
            @Override
            public void onUIProgress(float progress) {
                mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
            }

            @Override
            public void onUIComplete(int retCode, String descMsg) {
                mProgressFragmentUtil.dismissLoadingProgress();

                LogReport.getInstance().reportVideoEdit(retCode);

                final UGCKitResult ugcKitResult = new UGCKitResult();
                ugcKitResult.outputPath = VideoGenerateKit.getInstance().getVideoOutputPath();
                ugcKitResult.coverPath = VideoGenerateKit.getInstance().getCoverPath();
                ugcKitResult.errorCode = retCode;
                ugcKitResult.descMsg = descMsg;
                ugcKitResult.isPublish = VideoEditerSDK.getInstance().isPublish();
                if (listener != null) {
                    listener.onEditCompleted(ugcKitResult);
                }
            }

            @Override
            public void onUICancel() {
                // 视频编辑生成取消，UI仅去掉进度条
            }
        });
    }

    private void startGenerate() {
        mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
            @Override
            public void onStop() {
                PlayerManagerKit.getInstance().restartPlay();

                stopGenerate();
            }
        });
        PlayerManagerKit.getInstance().stopPlay();

        VideoGenerateKit.getInstance().addTailWaterMark();
        VideoGenerateKit.getInstance().startGenerate();
    }

    private void stopGenerate() {
        VideoGenerateKit.getInstance().stopGenerate();
        mProgressFragmentUtil.dismissLoadingProgress();
    }

}

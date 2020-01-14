package com.tencent.qcloud.xiaoshipin.videorecord;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCVideoInfo;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.DownloadUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;

/**
 * 合唱视频下载
 */
public class FollowRecordDownloader {
    private static final String TAG = "FollowRecordDownloader";
    private Context mContext;
    private ProgressDialogUtil mProgressDialogUtil;
    private TXVideoInfoReader mVideoInfoReader;
    private float mDuration;

    public FollowRecordDownloader(Context context) {
        mContext = context;
        mProgressDialogUtil = new ProgressDialogUtil(mContext);
        mVideoInfoReader = TXVideoInfoReader.getInstance();
    }

    public void setDuration(float duration) {
        mDuration = duration;
    }

    public void downloadVideo(TCVideoInfo tcVideoInfo) {
        if (tcVideoInfo.review_status == TCVideoInfo.REVIEW_STATUS_NOT_REVIEW) {
            ToastUtil.toastShortMessage(mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_video_state_in_audit));
            return;
        } else if (tcVideoInfo.review_status == TCVideoInfo.REVIEW_STATUS_PORN) {
            ToastUtil.toastShortMessage(mContext.getResources().getString(R.string.tc_ugc_video_list_adapter_video_state_pornographic));
            return;
        }
        mProgressDialogUtil.showProgressDialog();
        File sdcardDir = mContext.getExternalFilesDir(null);
        if (sdcardDir == null) {
            TXCLog.e(TAG, "downloadVideo sdcardDir is null");
            return;
        }
        File downloadFileFolder = new File(sdcardDir, UGCKitConstants.OUTPUT_DIR_NAME);
        File downloadFile = new File(downloadFileFolder, DownloadUtil.getNameFromUrl(tcVideoInfo.playurl));

        if (downloadFile.exists()) {
            mProgressDialogUtil.dismissProgressDialog();
            startRecordActivity(downloadFile.getAbsolutePath());
            return;
        }
        mProgressDialogUtil.setProgressDialogMessage(mContext.getResources().getString(R.string.tc_vod_player_activity_download_video_is_downloading));

        DownloadUtil.get(mContext).download(tcVideoInfo.playurl, UGCKitConstants.OUTPUT_DIR_NAME, new DownloadUtil.DownloadListener() {

            @Override
            public void onDownloadSuccess(final String path) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialogUtil.dismissProgressDialog();
                        startRecordActivity(path);
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {
                TXCLog.i(TAG, "downloadVideo, progress = " + progress);
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialogUtil.setProgressDialogMessage(mContext.getResources().getString(R.string.tc_vod_player_activity_download_video_is_downloading) + progress + "%");
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialogUtil.dismissProgressDialog();
                        ToastUtil.toastShortMessage(mContext.getResources().getString(R.string.tc_vod_player_activity_download_video_download_failed));
                    }
                });
            }
        });
    }

    private void startRecordActivity(String path) {
        Intent intent = new Intent(mContext, TCVideoFollowRecordActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, path);
        mContext.startActivity(intent);
    }
}

package com.tencent.qcloud.xiaoshipin.common;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.DownloadUtil;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.xiaoshipin.play.TCVideoPreviewActivity;
import com.tencent.qcloud.xiaoshipin.videochoose.TCPicturePickerActivity;
import com.tencent.qcloud.xiaoshipin.videochoose.TCVideoPickerActivity;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoFollowRecordActivity;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoRecordActivity;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoTripleScreenActivity;

import java.io.File;

/**
 * 短视频选择界面
 */
public class ShortVideoDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ShortVideoDialog";
    private RelativeLayout mTVVideo;
    private ImageView mIVClose;
    private RelativeLayout mTVEditer;
    private RelativeLayout mTVPicture;
    private RelativeLayout mTVChorus;
    private RelativeLayout mTVTriple;

    private ProgressDialog mDownloadProgressDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_short_video);
        dialog.setCanceledOnTouchOutside(true);

        mTVVideo = (RelativeLayout) dialog.findViewById(R.id.tv_record);
        mTVEditer = (RelativeLayout) dialog.findViewById(R.id.tv_editer);
        mTVPicture = (RelativeLayout) dialog.findViewById(R.id.tv_picture);
        mTVChorus = (RelativeLayout) dialog.findViewById(R.id.tv_chorus);
        mIVClose = (ImageView) dialog.findViewById(R.id.iv_close);
        mTVTriple = (RelativeLayout) dialog.findViewById(R.id.tv_triple_chorus);

        mTVVideo.setOnClickListener(this);
        mTVEditer.setOnClickListener(this);
        mTVPicture.setOnClickListener(this);
        mTVChorus.setOnClickListener(this);
        mIVClose.setOnClickListener(this);
        mTVTriple.setOnClickListener(this);

        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        window.setAttributes(lp);

        mDownloadProgressDialog = new ProgressDialog(getActivity());
        mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 设置进度条的形式为圆形转动的进度条
        mDownloadProgressDialog.setCancelable(false);                           // 设置是否可以通过点击Back键取消
        mDownloadProgressDialog.setCanceledOnTouchOutside(false);               // 设置在点击Dialog外是否取消Dialog进度条

        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_record:
                dismissDialog();
                startActivity(new Intent(getActivity(), TCVideoRecordActivity.class));
                break;
            case R.id.tv_editer:
                dismissDialog();

                Intent intent = new Intent(getActivity(), TCVideoPickerActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_picture:
                dismissDialog();

                Intent intent2 = new Intent(getActivity(), TCPicturePickerActivity.class);
                startActivity(intent2);
                break;
            case R.id.tv_chorus:
                prepareToDownload(false);
                break;
            case R.id.iv_close:
                dismissDialog();
                break;
            case R.id.tv_triple_chorus:
                prepareToDownload(true);
                break;
        }
    }

    private void prepareToDownload(final boolean isTriple) {
        if (getActivity() == null) {
            return;
        }
        File sdcardDir = getActivity().getExternalFilesDir(null);
        if (sdcardDir == null) {
            TXCLog.e(TAG, "prepareToDownload sdcardDir is null");
            return;
        }
        File downloadFileFolder = new File(sdcardDir, UGCKitConstants.OUTPUT_DIR_NAME);
        File downloadFile = new File(downloadFileFolder, DownloadUtil.getNameFromUrl(UGCKitConstants.CHORUS_URL));
        if (downloadFile.exists()) {
            mDownloadProgressDialog.dismiss();
            if (isTriple) {
                startTripleActivity(downloadFile.getAbsolutePath());
            } else {
                startRecordActivity(downloadFile.getAbsolutePath());
            }
            return;
        }
        if (mDownloadProgressDialog != null) {
            mDownloadProgressDialog.show();
        }
        DownloadUtil.get(getActivity()).download(UGCKitConstants.CHORUS_URL, UGCKitConstants.OUTPUT_DIR_NAME, new DownloadUtil.DownloadListener() {
            @Override
            public void onDownloadSuccess(final String path) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.dismiss();
                        if (isTriple) {
                            startTripleActivity(path);
                        } else {
                            startRecordActivity(path);
                        }
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.setMessage("正在下载..." + progress + "%");
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.dismiss();

                        ToastUtil.toastShortMessage("下载失败");
                    }
                });
            }
        });
    }

    private void startRecordActivity(String path) {
        Intent intent = new Intent(getActivity(), TCVideoFollowRecordActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, path);
        startActivity(intent);
    }

    private void startTripleActivity(String path) {
        Intent intent = new Intent(getActivity(), TCVideoTripleScreenActivity.class);//TCVideoPreviewActivity
        intent.putExtra(UGCKitConstants.VIDEO_PATH, path);
        startActivity(intent);

    }

    private void dismissDialog() {
        if (ShortVideoDialog.this.isAdded()) {
            ShortVideoDialog.this.dismiss();
        }
    }
}

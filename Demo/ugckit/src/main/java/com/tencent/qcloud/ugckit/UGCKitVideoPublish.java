package com.tencent.qcloud.ugckit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.ugckit.module.upload.TXUGCPublish;
import com.tencent.qcloud.ugckit.module.upload.TXUGCPublishTypeDef;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.NetworkUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class UGCKitVideoPublish extends RelativeLayout implements View.OnClickListener, TXUGCPublishTypeDef.ITXVideoPublishListener {
    @NonNull
    private String TAG = "UGCKitVideoPublish";

    private Context           mContext;
    private ImageView         mImageBack;      // 返回
    private ImageView         mImageViewBg;
    private ProgressBar       mProgressBar;     // 发布视频进度条
    private TextView          mTextProgress;     // 发布视频进度文字
    @Nullable
    private TXUGCPublish      mVideoPublish  = null;
    private boolean           mIsFetchCosSig = false;
    @Nullable
    private String            mCosSignature  = null;
    @NonNull
    private Handler           mHandler       = new Handler();
    private boolean           mAllDone       = false;
    private boolean           mDisableCache;
    private String            mLocalVideoPath;
    @Nullable
    private String            mVideoPath     = null;  // 视频路径
    @Nullable
    private String            mCoverPath     = null;  // 视频封面路径
    private OnPublishListener mOnPublishListener;

    public UGCKitVideoPublish(Context context) {
        super(context);
        init(context);
    }

    public UGCKitVideoPublish(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UGCKitVideoPublish(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 功能：</p>
     * 1、UGCKit控件初始化</p>
     * 2、加载视频封面</p>
     * 3、发布视频</p>
     *
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        inflate(getContext(), R.layout.ugckit_publish_video_layout, this);

        mImageBack = (ImageView) findViewById(R.id.btn_back);
        mImageBack.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mTextProgress = (TextView) findViewById(R.id.tv_progress);
        mImageViewBg = (ImageView) findViewById(R.id.bg_iv);

        publishVideo();
    }

    /**
     * 检测有网络时获取签名
     */
    private void publishVideo() {
        if (mAllDone) {
//            Intent intent = new Intent(TCVideoPublisherActivity.this, TCMainActivity.class);
//            startActivity(intent);
        } else {
            if (!NetworkUtil.isNetworkAvailable(mContext)) {
                ToastUtil.toastShortMessage(getResources().getString(R.string.ugckit_video_publisher_activity_no_network_connection));
                return;
            }
            fetchSignature();
        }
    }

    /**
     * Step1:获取签名
     */
    private void fetchSignature() {
        if (mIsFetchCosSig) {
            return;
        }
        mIsFetchCosSig = true;

        TCUserMgr.getInstance().getVodSig(new TCUserMgr.Callback() {
            @Override
            public void onSuccess(@NonNull JSONObject data) {
                try {
                    mCosSignature = data.getString("signature");
                    startPublish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /**
                 * ELK数据上报：获取签名成功
                 */
                LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_SIGN, TCUserMgr.SUCCESS_CODE, "获取签名成功");
            }

            @Override
            public void onFailure(int code, final String msg) {
                /**
                 * ELK数据上报：获取签名失败
                 */
                LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_SIGN, code, "获取签名失败");
            }
        });
    }

    /**
     * Step2:开始发布视频（子线程）
     */
    private void startPublish() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPublish == null) {
                    mVideoPublish = new TXUGCPublish(UGCKit.getAppContext(), TCUserMgr.getInstance().getUserId());
                }
                /**
                 * 设置视频发布监听器
                 */
                mVideoPublish.setListener(UGCKitVideoPublish.this);

                TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
                param.signature = mCosSignature;
                param.videoPath = mVideoPath;
                param.coverPath = mCoverPath;
                int publishCode = mVideoPublish.publishVideo(param);
//                if (publishCode != 0) {
//                    mTVPublish.setText("发布失败，错误码：" + publishCode);
//                }
                NetworkUtil.getInstance(UGCKit.getAppContext()).setNetchangeListener(new NetworkUtil.NetchangeListener() {
                    @Override
                    public void onNetworkAvailable() {
                        mTextProgress.setText(getResources().getString(R.string.ugckit_video_publisher_activity_network_connection_is_disconnected_video_upload_failed));
                    }
                });
                NetworkUtil.getInstance(UGCKit.getAppContext()).registerNetChangeReceiver();
            }
        });
    }

    /**
     * 设置发布视频的路径和封面<br>
     * 注意：请检查路径是否正确
     *
     * @param videoPath 视频的路径
     * @param coverPath 封面的路径
     */
    public void setPublishPath(String videoPath, String coverPath) {
        mVideoPath = videoPath;
        mCoverPath = coverPath;

        loadCoverImage();
    }

    /**
     * 开启本地缓存，若关闭本地缓存，则发布完成后删除"已发布"的视频和封面
     *
     * @param disableCache {@code true} 开启本地缓存，设置的视频文件和封面文件不会被删除。<br>
     *                     {@code false} 关闭本地缓存，则发布完成后删除"已发布"的视频和封面；<br>
     *                     默认为true
     */
    public void setCacheEnable(boolean disableCache) {
        mDisableCache = disableCache;
    }

    /**
     * 设置发布视频的监听器
     *
     * @param onUIClickListener
     */
    public void setOnPublishListener(OnPublishListener onUIClickListener) {
        mOnPublishListener = onUIClickListener;
    }

    @Override
    public void onClick(@NonNull View view) {
        int i = view.getId();
        if (i == R.id.btn_back) {
            showCancelPublishDialog();
        }
    }

    /**
     * 加载视频封面
     */
    private void loadCoverImage() {
        if (mCoverPath != null) {
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(mCoverPath)))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(mImageViewBg);
        }
    }

    /**
     * 显示取消发布的Dialog
     */
    private void showCancelPublishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog alertDialog = builder.setTitle(mContext.getString(R.string.ugckit_cancel_publish_title)).setCancelable(false).setMessage(R.string.ugckit_cancel_publish_msg)
                .setPositiveButton(R.string.ugckit_cancel_publish_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        if (mVideoPublish != null) {
                            mVideoPublish.canclePublish();
                        }
                        dialog.dismiss();
                        if (mOnPublishListener != null) {
                            mOnPublishListener.onPublishCancel();
                        }
                    }
                })
                .setNegativeButton(mContext.getString(R.string.ugckit_wrong_click), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 视频发布进度
     *
     * @param uploadBytes
     * @param totalBytes
     */
    @Override
    public void onPublishProgress(long uploadBytes, long totalBytes) {
        int progress = (int) (uploadBytes * 100 / totalBytes);
        Log.d(TAG, "onPublishProgress:" + progress);
        mProgressBar.setProgress(progress);
        mTextProgress.setText(getResources().getString(R.string.ugckit_video_publisher_activity_is_uploading) + progress + "%");
    }

    /**
     * 视频发布结果回调<p/>
     * 当视频发布成功后，发布到点播系统，此时就可以在视频列表看到"已发布的视频"
     *
     * @param publishResult
     */
    @Override
    public void onPublishComplete(@NonNull TXUGCPublishTypeDef.TXPublishResult publishResult) {
        Log.d(TAG, "onPublishComplete:" + publishResult.retCode);

        /**
         * ELK数据上报：视频发布到点播系统
         */
        LogReport.getInstance().reportPublishVideo(publishResult);

        if (publishResult.retCode == TXUGCPublishTypeDef.PUBLISH_RESULT_OK) {
            mImageBack.setVisibility(View.GONE);
            UploadUGCVideo(publishResult.videoId, publishResult.videoURL, publishResult.coverURL);
        } else {
            if (publishResult.descMsg.contains("java.net.UnknownHostException") || publishResult.descMsg.contains("java.net.ConnectException")) {
                mTextProgress.setText(mContext.getResources().getString(R.string.ugckit_video_publisher_activity_network_connection_is_disconnected_video_upload_failed));
            } else {
                mTextProgress.setText(publishResult.descMsg);
            }
            Log.e(TAG, publishResult.descMsg);
        }
    }

    /**
     * 发布视频后删除本地缓存的视频和封面
     */
    private void deleteCache() {
        if (mDisableCache) {
            File file = new File(mVideoPath);
            if (file.exists()) {
                file.delete();
            }
            if (!TextUtils.isEmpty(mCoverPath)) {
                file = new File(mCoverPath);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (mLocalVideoPath != null) {
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(new File(mLocalVideoPath)));
                mContext.sendBroadcast(scanIntent);
            }
        }
    }

    /**
     * 发布到服务器
     *
     * @param videoId
     * @param videoURL
     * @param coverURL
     */
    private void UploadUGCVideo(final String videoId, final String videoURL, final String coverURL) {
        String title = null; //TODO:传入本地视频文件名称
        if (TextUtils.isEmpty(title)) {
            title = "小视频";
        }
        try {
            JSONObject body = new JSONObject().put("file_id", videoId)
                    .put("title", title)
                    .put("frontcover", coverURL)
                    .put("location", "未知")
                    .put("play_url", videoURL);
            TCUserMgr.getInstance().request("/upload_ugc", body, new TCUserMgr.HttpCallback("upload_ugc", new TCUserMgr.Callback() {
                @Override
                public void onSuccess(JSONObject data) {
                    /**
                     * ELK上报：发布视频到服务器
                     */
                    LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_UPLOAD_SERVER, TCUserMgr.SUCCESS_CODE, "UploadUGCVideo Sucess");

                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(UGCKitConstants.EVENT_MSG_PUBLISH_DONE);

                            if (mOnPublishListener != null) {
                                mOnPublishListener.onPublishComplete();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(int code, final String msg) {
                    /**
                     * ELK上报：发布视频到服务器
                     */
                    LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_UPLOAD_SERVER, code, msg);
                }
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        NetworkUtil.getInstance(UGCKit.getAppContext()).unregisterNetChangeReceiver();
        NetworkUtil.getInstance(UGCKit.getAppContext()).setNetchangeListener(null);
        deleteCache();
    }


    public interface OnPublishListener {

        void onPublishComplete();

        void onPublishCancel();
    }
}

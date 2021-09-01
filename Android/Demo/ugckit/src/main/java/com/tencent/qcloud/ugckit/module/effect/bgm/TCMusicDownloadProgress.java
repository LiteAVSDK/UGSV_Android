package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;


import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.utils.HttpFileListener;
import com.tencent.qcloud.ugckit.utils.HttpFileUtil;
import com.tencent.qcloud.ugckit.utils.VideoDeviceUtil;
import com.tencent.qcloud.ugckit.R;


import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TCMusicDownloadProgress {

    public static final  String BGM_FOLDER     = "bgm";
    private static final int    CORE_POOL_SIZE = 8;

    private int                mBgmPosition;
    private boolean            mProcessing;
    private String             mUrl;
    private String             mBgmName;
    @Nullable
    private Downloadlistener   mListener;
    private DownloadThreadPool sDownloadThreadPool;

    public TCMusicDownloadProgress(String bgmName, int position, String url) {
        this.mBgmName = bgmName;
        this.mBgmPosition = position;
        this.mUrl = url;
        mProcessing = false;
    }

    public void start(@Nullable Downloadlistener listener) {
        if (listener == null || TextUtils.isEmpty(mUrl) || mProcessing) {
            return;
        }
        this.mListener = listener;
        mProcessing = true;
        mListener.onDownloadProgress(0);
        HttpFileListener fileListener = new HttpFileListener() {
            @Override
            public void onSaveSuccess(@NonNull File file) {
                mListener.onDownloadSuccess(file.getPath());
                stop();
            }

            @Override
            public void onSaveFailed(File file, @NonNull Exception e) {
                mListener.onDownloadFail(e.getMessage());
                stop();
            }

            @Override
            public void onProgressUpdate(int progress) {
                mListener.onDownloadProgress(progress);
            }

            @Override
            public void onProcessEnd() {
                mProcessing = false;
            }

        };
        File onlineMaterialDir = VideoDeviceUtil.getExternalFilesDir(UGCKit.getAppContext(), BGM_FOLDER);
        if (onlineMaterialDir == null || onlineMaterialDir.getName().startsWith("null")) {
            mListener.onDownloadFail(UGCKit.getAppContext().getResources().getString(R.string.ugckit_bgm_download_progress_no_enough_storage_space));
            stop();
            return;
        }
        if (!onlineMaterialDir.exists()) {
            onlineMaterialDir.mkdirs();
        }

        ThreadPoolExecutor threadPool = getThreadExecutor();
        HttpFileUtil httpFileUtil = new HttpFileUtil(mUrl, onlineMaterialDir.getPath(), mBgmName, fileListener, true);
        threadPool.execute(httpFileUtil);
    }

    public void stop() {
        mListener = null;
    }

    public synchronized ThreadPoolExecutor getThreadExecutor() {
        if (sDownloadThreadPool == null || sDownloadThreadPool.isShutdown()) {
            sDownloadThreadPool = new DownloadThreadPool(CORE_POOL_SIZE);
        }
        return sDownloadThreadPool;
    }

    public static class DownloadThreadPool extends ThreadPoolExecutor {

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public DownloadThreadPool(int poolSize) {
            super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<Runnable>(),
                    Executors.defaultThreadFactory(), new AbortPolicy());
        }
    }

    public interface Downloadlistener {
        void onDownloadFail(String errorMsg);

        void onDownloadProgress(final int progress);

        void onDownloadSuccess(String filePath);
    }
}

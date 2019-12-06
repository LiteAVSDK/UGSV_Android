package com.tencent.qcloud.ugckit.module.record;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.tencent.qcloud.ugckit.UGCKitImpl;
import com.tencent.qcloud.ugckit.utils.HttpFileListener;
import com.tencent.qcloud.ugckit.utils.HttpFileUtil;
import com.tencent.qcloud.ugckit.utils.VideoDeviceUtil;
import com.tencent.qcloud.ugckit.utils.VideoUtil;
import com.tencent.qcloud.ugckit.R;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoMaterialDownloadProgress {
    public static final String DOWNLOAD_FILE_POSTFIX = ".zip";
    public static final String ONLINE_MATERIAL_FOLDER = "cameraVideoAnimal";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private boolean mProcessing;

    private String mUrl;
    @Nullable
    private Downloadlistener mListener;
    private DownloadThreadPool sDownloadThreadPool;
    private String mMaterialId;

    public VideoMaterialDownloadProgress(String materialId, String url) {
        this.mMaterialId = materialId;
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

                //删除该素材目录下的旧文件
                File path = new File(file.toString().substring(0, file.toString().indexOf(DOWNLOAD_FILE_POSTFIX)));
                if (path.exists() && path.isDirectory()) {
                    File[] oldFiles = path.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) {
                            f.delete();
                        }
                    }
                }

                String dataDir = VideoUtil.unZip(file.getPath(), file.getParentFile().getPath());
                if (TextUtils.isEmpty(dataDir)) {
                    mListener.onDownloadFail(UGCKitImpl.getAppContext().getString(R.string.video_material_download_progress_material_unzip_failed));
                    stop();
                    return;
                }
                file.delete();
                mListener.onDownloadSuccess(dataDir);
                stop();
            }

            @Override
            public void onSaveFailed(File file, Exception e) {
                mListener.onDownloadFail(UGCKitImpl.getAppContext().getString(R.string.video_material_download_progress_download_failed));
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
        File onlineMaterialDir = VideoDeviceUtil.getExternalFilesDir(UGCKitImpl.getAppContext(), ONLINE_MATERIAL_FOLDER);
        if (onlineMaterialDir == null || onlineMaterialDir.getName().startsWith("null")) {
            mListener.onDownloadFail(UGCKitImpl.getAppContext().getString(R.string.video_material_download_progress_no_enough_storage_space));
            stop();
            return;
        }
        if (!onlineMaterialDir.exists()) {
            onlineMaterialDir.mkdirs();
        }

        ThreadPoolExecutor threadPool = getThreadExecutor();
        threadPool.execute(new HttpFileUtil(mUrl, onlineMaterialDir.getPath(), mMaterialId + DOWNLOAD_FILE_POSTFIX, fileListener, true));
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
                    Executors.defaultThreadFactory(), new DiscardOldestPolicy());
        }
    }

    public interface Downloadlistener {
        void onDownloadFail(String errorMsg);

        void onDownloadProgress(final int progress);

        void onDownloadSuccess(String filePath);
    }
}

package com.tencent.xmagic;

import android.content.Context;
import android.util.Log;

import com.tencent.xmagic.download.MotionDLModel;
import com.tencent.xmagic.download.ResDownloadConfig;
import com.tencent.xmagic.download.ResDownloadUtil;
import com.tencent.xmagic.utils.OnDownloadListener;

import java.util.List;

public class MotionsDownloader {

    private static final String TAG = MotionsDownloader.class.getName();

    private int successCount = 0;
    private int failedCount = 0;
    private int motionsCount = 11;
    private List<MotionDLModel> motionDLModels = null;
    private MotionsDownLoadCallBack callBack;

    public MotionsDownloader(MotionsDownLoadCallBack motionsDownLoadCallBack) {
        motionDLModels = ResDownloadConfig.getMotionList();
        callBack = motionsDownLoadCallBack;
    }

    public void startDownload(final Context context) {
        MotionDLModel model = motionDLModels.get(successCount + failedCount);
        ResDownloadUtil.checkOrDownloadMotions(context, model, new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String directory) {
                Log.e(TAG, "onDownloadSuccess  " + directory);
                successCount++;
                if (successCount + failedCount == motionsCount) {
                    if (callBack != null) {
                        callBack.onComplete(successCount, failedCount);
                    }
                    return;
                }
                startDownload(context);
            }

            @Override
            public void onDownloading(int progress) {
                Log.e(TAG, "onDownloading  " + progress);
            }

            @Override
            public void onDownloadFailed(int errorCode) {
                Log.e(TAG, "onDownloadFailed  " + errorCode);
                failedCount++;
                if (successCount + failedCount == motionsCount) {
                    if (callBack != null) {
                        callBack.onComplete(successCount, failedCount);
                    }
                    return;
                }
                startDownload(context);
            }
        });
    }

    public interface MotionsDownLoadCallBack {
        void onComplete(int successCount, int failedCount);
    }


}

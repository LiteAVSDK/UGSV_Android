package com.tencent.qcloud.ugckit.module;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;

import com.tencent.qcloud.ugckit.basic.BaseGenerateKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

/**
 * 视频预处理
 */
public class ProcessKit extends BaseGenerateKit implements TXVideoEditer.TXVideoProcessListener, TXVideoEditer.TXThumbnailListener {
    private static final String TAG = "ProcessKit";

    @NonNull
    private static ProcessKit instance = new ProcessKit();

    private ProcessKit() {

    }

    @NonNull
    public static ProcessKit getInstance() {
        return instance;
    }

    /**
     * 开始预处理视频
     * FIXBUG：缩略图getThumbnail和processVideo同时使用时，会有两个进度条，一个是视频预处理的进度条，一个是缩略图生成进度的进度条。
     * 缩略图setThumbnail和processVideo同时使用，只有一个进度条。
     * <p>
     * 1.录制进入编辑：缩略图使用预处理的进度条
     * 2.裁剪进入编辑：使用预处理的进度条，此时使用getThumbnail会产生两个进度条，进度条会来回闪烁
     */
    public void startProcess() {
        VideoEditerSDK.getInstance().clearThumbnails();

        long cutterStartTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long cutterEndTime = VideoEditerSDK.getInstance().getCutterEndTime();

        int thumbnailCount = (int) (cutterEndTime - cutterStartTime) / 1000;

        TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
        thumbnail.count = thumbnailCount;
        thumbnail.width = 100;
        thumbnail.height = 100;

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            Log.i(TAG, "[UGCKit][VideoProcess]generate thumbnail start time:" + cutterStartTime + ",end time:" + cutterEndTime + ",thumbnail count:" + thumbnailCount);
            editer.setThumbnail(thumbnail);
            editer.setThumbnailListener(this);
            editer.setVideoProcessListener(this);
            editer.setCutFromTime(cutterStartTime, cutterEndTime);
            Log.i(TAG, "[UGCKit][VideoProcess]generate video start time:" + cutterStartTime + ",end time:" + cutterEndTime);
            editer.processVideo();
        }
    }

    /**
     * 停止预处理视频[包括一些异常操作导致的合成取消]
     */
    public void stopProcess() {
        Log.d(TAG, "stopProcess");

        //FIXBUG:如果上一次如生成缩略图没有停止，先停止，在进行下一次生成
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.cancel();
            editer.setVideoProcessListener(null);
        }
        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUICancel();
        }
    }

    @Override
    public void onProcessProgress(float progress) {
        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIProgress(progress);
        }
    }

    @Override
    public void onProcessComplete(@NonNull TXVideoEditConstants.TXGenerateResult result) {
        Log.d(TAG, "onProcessComplete");
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoProcessListener(null);
        }

        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIComplete(result.retCode, result.descMsg);
        }
    }

    @Override
    public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
        Log.d(TAG, "onThumbnail index:" + index + ",timeMs:" + timeMs);
        VideoEditerSDK.getInstance().addThumbnailBitmap(timeMs, bitmap);
    }

}

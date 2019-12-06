package com.tencent.qcloud.ugckit.module;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.tencent.qcloud.ugckit.basic.BaseGenerateKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

/**
 * 视频预处理
 */
public class ProcessKit extends BaseGenerateKit implements TXVideoEditer.TXVideoProcessListener, TXVideoEditer.TXThumbnailListener {
    private static final String TAG = "ProcessKit";
    private int mCurrentState;

    @NonNull
    private static ProcessKit instance = new ProcessKit();
    private OnThumbnailListener mThumbnailListener;

    private ProcessKit() {

    }

    @NonNull
    public static ProcessKit getInstance() {
        return instance;
    }

    /**
     * 开始预处理视频
     */
    public void startProcess() {
        mCurrentState = PlayState.STATE_GENERATE;

        long cutterStartTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long cutterEndTime = VideoEditerSDK.getInstance().getCutterEndTime();

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoProcessListener(this);
            editer.setCutFromTime(cutterStartTime, cutterEndTime);
            editer.processVideo();
        }
    }

    /**
     * 停止预处理视频[包括一些异常操作导致的合成取消]
     */
    public void stopProcess() {
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mCurrentState = PlayState.STATE_NONE;

            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            if (editer != null) {
                editer.cancel();
                editer.setVideoProcessListener(null);
            }

            if (mOnUpdateUIListener != null) {
                mOnUpdateUIListener.onUICancel();
            }
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
        mCurrentState = PlayState.STATE_NONE;

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoProcessListener(null);
        }
        long cutterStartTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long cutterEndTime = VideoEditerSDK.getInstance().getCutterEndTime();
        int thumbnailCount = (int) (cutterEndTime - cutterStartTime) / 1000;
        TXLog.d(TAG, "thumbnailCount:" + thumbnailCount);

        TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
        thumbnail.count = thumbnailCount;
        thumbnail.width = 100;
        thumbnail.height = 100;
        if (editer != null) {
            editer.getThumbnail(thumbnail.count, thumbnail.width, thumbnail.height, false, this);
        }

        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIComplete(result.retCode, result.descMsg);
        }
    }

    @Override
    public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
        VideoEditerSDK.getInstance().addThumbnailBitmap(timeMs, bitmap);
    }

    public void setOnThumbnailListener(OnThumbnailListener listener) {
        mThumbnailListener = listener;
    }

    public interface OnThumbnailListener {
        void onThumbnail(int index, long timeMs, Bitmap bitmap);
    }

}

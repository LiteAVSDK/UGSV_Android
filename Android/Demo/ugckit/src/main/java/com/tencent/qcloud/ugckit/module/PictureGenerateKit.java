package com.tencent.qcloud.ugckit.module;

import android.support.annotation.NonNull;

import com.tencent.qcloud.ugckit.basic.BaseGenerateKit;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoEditer.TXVideoGenerateListener;

public class PictureGenerateKit extends BaseGenerateKit implements TXVideoGenerateListener {
    private int mCurrentState;
    private String mVideoOutputPath;

    @NonNull
    private static PictureGenerateKit instance = new PictureGenerateKit();

    private PictureGenerateKit() {

    }

    @NonNull
    public static PictureGenerateKit getInstance() {
        return instance;
    }

    /**
     * 开始合成视频
     */
    public void startGenerate() {
        mCurrentState = PlayState.STATE_GENERATE;
        mVideoOutputPath = VideoPathUtil.generateVideoPath();

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoGenerateListener(this);
            editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
        }
    }

    /**
     * 停止合成视频[包括一些异常操作导致的合成取消]
     */
    public void stopGenerate() {
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mCurrentState = PlayState.STATE_NONE;

            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            if (editer != null) {
                editer.cancel();
                editer.setVideoGenerateListener(null);
            }

            if (mOnUpdateUIListener != null) {
                mOnUpdateUIListener.onUICancel();
            }
        }
    }

    /**
     * 获取生成视频输出路径
     *
     * @return
     */
    public String getVideoOutputPath() {
        return mVideoOutputPath;
    }

    @Override
    public void onGenerateProgress(float progress) {
        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIProgress(progress);
        }
    }

    @Override
    public void onGenerateComplete(@NonNull TXVideoEditConstants.TXGenerateResult result) {
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_PICTURE_EDIT, result.retCode, result.descMsg);

        mCurrentState = PlayState.STATE_NONE;
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            // SDK释放资源
            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            if (editer != null) {
                editer.release();
            }
            VideoEditerSDK.getInstance().clear();

            // UI更新
            if (mOnUpdateUIListener != null) {
                mOnUpdateUIListener.onUIComplete(result.retCode, result.descMsg);
            }
        }
    }

}

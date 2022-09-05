package com.tencent.qcloud.ugckit.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.util.Log;


import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.basic.BaseGenerateKit;
import com.tencent.qcloud.ugckit.module.editer.TailWaterMarkConfig;
import com.tencent.qcloud.ugckit.module.editer.WaterMarkConfig;
import com.tencent.qcloud.ugckit.utils.AlbumSaver;
import com.tencent.qcloud.ugckit.utils.CoverUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoEditer.TXVideoGenerateListener;

import java.io.File;

/**
 * 视频生成管理
 */
public class VideoGenerateKit extends BaseGenerateKit implements TXVideoGenerateListener {
    private static final String           TAG                    = "VideoGenerateKit";
    private static final int              DURATION_TAILWATERMARK = 3;
    @NonNull
    private static       VideoGenerateKit instance               = new VideoGenerateKit();

    private int                 mCurrentState;
    private int                 mVideoResolution = TXVideoEditConstants.VIDEO_COMPRESSED_720P;
    private boolean             mSaveToDCIM;
    private boolean             mCoverGenerate;
    private String              mVideoOutputPath;
    private String              mCoverPath;
    private WaterMarkConfig     mWaterMark;
    private TailWaterMarkConfig mTailWaterMarkConfig;

    private VideoGenerateKit() {
        // 默认生成封面
        mCoverGenerate = true;
        // 默认保存到相册
        mSaveToDCIM = true;
    }

    @NonNull
    public static VideoGenerateKit getInstance() {
        return instance;
    }

    /**
     * 设置视频分辨率
     *
     * @param resolution
     */
    public void setVideoResolution(int resolution) {
        mVideoResolution = resolution;
    }

    /**
     * 自定义视频码率
     *
     * @param videoBitrate
     */
    public void setCustomVideoBitrate(int videoBitrate) {
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoBitrate(videoBitrate);
        }
    }

    /**
     * 开始合成视频
     */
    public void startGenerate() {
        mCurrentState = PlayState.STATE_GENERATE;
        mVideoOutputPath = VideoPathUtil.generateVideoPath();
        Log.d(TAG, "startGenerate mVideoOutputPath:" + mVideoOutputPath);

        long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long endTime = VideoEditerSDK.getInstance().getCutterEndTime();

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setCutFromTime(startTime, endTime);
            editer.setVideoGenerateListener(this);
            if (mWaterMark != null) {
                editer.setWaterMark(mWaterMark.watermark, mWaterMark.rect);
            }
            if (mTailWaterMarkConfig != null) {
                editer.setTailWaterMark(mTailWaterMarkConfig.tailwatermark, mTailWaterMarkConfig.rect, mTailWaterMarkConfig.duration);
            }

            switch (mVideoResolution) {
                case TXVideoEditConstants.VIDEO_COMPRESSED_360P:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_360P, mVideoOutputPath);
                    break;
                case TXVideoEditConstants.VIDEO_COMPRESSED_480P:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_480P, mVideoOutputPath);
                    break;
                case TXVideoEditConstants.VIDEO_COMPRESSED_540P:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
                    break;
                case TXVideoEditConstants.VIDEO_COMPRESSED_720P:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
                    break;
                case TXVideoEditConstants.VIDEO_COMPRESSED_1080P:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_1080P, mVideoOutputPath);
                    break;
                default:
                    editer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
                    break;

            }
        }
    }

    /**
     * 停止合成视频[包括一些异常操作导致的合成取消]
     */
    public void stopGenerate() {
        //FIXBUG:如果上一次如生成缩略图没有停止，先停止，在进行下一次生成
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.cancel();
            editer.setVideoGenerateListener(null);
        }
        if (mCurrentState == PlayState.STATE_GENERATE) {
            ToastUtil.toastShortMessage(UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_editer_activity_cancel_video_generation));
            mCurrentState = PlayState.STATE_NONE;

            if (mOnUpdateUIListener != null) {
                mOnUpdateUIListener.onUICancel();
            }
        }
    }

    @Override
    public void onGenerateProgress(float progress) {
        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIProgress(progress);
        }
    }

    /**
     * 添加片尾水印
     */
    public void addTailWaterMark() {
        TXVideoEditConstants.TXVideoInfo info = VideoEditerSDK.getInstance().getTXVideoInfo();

        if (info == null) {
            Log.e(TAG, "addTailWaterMark info is null");
            return;
        }
        Bitmap tailWaterMarkBitmap = BitmapFactory.decodeResource(UGCKit.getAppContext().getResources(), R.drawable.ugckit_tcloud_logo);
        float widthHeightRatio = tailWaterMarkBitmap.getWidth() / (float) tailWaterMarkBitmap.getHeight();

        TXVideoEditConstants.TXRect rect = new TXVideoEditConstants.TXRect();
        // 归一化的片尾水印，这里设置了一个固定值，水印占屏幕宽度的0.25。
        rect.width = 0.25f;
        // 后面根据实际图片的宽高比，计算出对应缩放后的图片的宽度：txRect.width * videoInfo.width 和高度：txRect.width * videoInfo.width / widthHeightRatio，然后计算出水印放中间时的左上角位置
        rect.x = (info.width - rect.width * info.width) / (2f * info.width);
        rect.y = (info.height - rect.width * info.width / widthHeightRatio) / (2f * info.height);

        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setTailWaterMark(tailWaterMarkBitmap, rect, DURATION_TAILWATERMARK);
        }
    }

    @Override
    public void onGenerateComplete(@NonNull final TXVideoEditConstants.TXGenerateResult result) {
        mCurrentState = PlayState.STATE_NONE;
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            if (mCoverGenerate) {
                Log.d(TAG, "onGenerateComplete outputPath:" + mVideoOutputPath);
                // 获取哪个视频的封面
                CoverUtil.getInstance().setInputPath(mVideoOutputPath);
                // 创建新的封面
                CoverUtil.getInstance().createThumbFile(new CoverUtil.ICoverListener() {
                    @Override
                    public void onCoverPath(String coverPath) {
                        mCoverPath = coverPath;
                        Log.d(TAG, "onGenerateComplete coverPath:" + coverPath);
                        saveAndUpdate(result);
                        release();
                    }
                });
            } else {
                saveAndUpdate(result);
                release();
            }
        }
    }

    private void release() {
        // SDK释放资源
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            editer.setVideoGenerateListener(null);
            editer.release();
        }
        VideoEditerSDK.getInstance().clear();
        AlbumSaver.getInstance(UGCKit.getAppContext()).release();
    }

    private void saveAndUpdate(@NonNull TXVideoEditConstants.TXGenerateResult result) {
        if (mSaveToDCIM) {
            long duration = VideoEditerSDK.getInstance().getVideoDuration();
            AlbumSaver.getInstance(UGCKit.getAppContext()).setOutputProfile(mVideoOutputPath, duration, mCoverPath);
            AlbumSaver.getInstance(UGCKit.getAppContext()).saveVideoToDCIMAsync(null);
        }
        // UI更新
        if (mOnUpdateUIListener != null) {
            mOnUpdateUIListener.onUIComplete(result.retCode, result.descMsg);
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

    /**
     * 获取视频封面路径
     */
    public String getCoverPath() {
        return mCoverPath;
    }

    public void saveVideoToDCIM(boolean flag) {
        mSaveToDCIM = flag;
    }

    public void setCoverGenerate(boolean coverGenerate) {
        mCoverGenerate = coverGenerate;
    }

    public void setWaterMark(WaterMarkConfig waterMark) {
        mWaterMark = waterMark;
    }

    public void setTailWaterMark(TailWaterMarkConfig tailWaterMarkConfig) {
        mTailWaterMarkConfig = tailWaterMarkConfig;
    }
}

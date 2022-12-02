package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.tencent.liteav.demo.beauty.BeautyParams;
import com.tencent.qcloud.ugckit.R;
import com.tencent.liteav.audio.TXCAudioUGCRecorder;
import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.module.record.draft.RecordDraftInfo;
import com.tencent.qcloud.ugckit.module.record.draft.RecordDraftManager;
import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCPartsManager;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

import java.util.List;

public class VideoRecordSDK implements TXRecordCommon.ITXVideoRecordListener {
    private static final String TAG = "VideoRecordSDK";

    public static  int                    STATE_START       = 1;
    public static  int                    STATE_STOP        = 2;
    public static  int                    STATE_RESUME      = 3;
    public static  int                    STATE_PAUSE       = 4;
    public static  int                    START_RECORD_SUCC = 0;
    public static  int                    START_RECORD_FAIL = -1;
    @NonNull
    private static VideoRecordSDK         sInstance         = new VideoRecordSDK();
    @Nullable
    private        TXUGCRecord            mRecordSDK;
    private        UGCKitRecordConfig     mUGCKitRecordConfig;
    private        RecordDraftManager     mRecordDraftManager;
    private        OnVideoRecordListener  mOnVideoRecordListener;
    private        OnRestoreDraftListener mOnRestoreDraftListener;
    private        int                    mCurrentState     = STATE_STOP;
    private        boolean                mPreviewFlag;
    private        String                 mRecordVideoPath;

    private VideoRecordSDK() {

    }

    @NonNull
    public static VideoRecordSDK getInstance() {
        return sInstance;
    }

    /**
     * 初始化SDK：TXUGCRecord
     */
    public void initSDK() {
        if (mRecordSDK == null) {
            mRecordSDK = TXUGCRecord.getInstance(UGCKit.getAppContext());
        }
        mCurrentState = STATE_STOP;
        Log.d(TAG, "initSDK");
    }

    @Nullable
    public TXUGCRecord getRecorder() {
        Log.d(TAG, "getRecorder mTXUGCRecord:" + mRecordSDK);
        return mRecordSDK;
    }

    public void initConfig(@NonNull UGCKitRecordConfig config) {
        mUGCKitRecordConfig = config;
        Log.d(TAG, "initConfig mBeautyParam:" + mUGCKitRecordConfig.mBeautyParams);
    }

    public UGCKitRecordConfig getConfig() {
        return mUGCKitRecordConfig;
    }

    public void startCameraPreview(TXCloudVideoView videoView) {
        Log.d(TAG, "startCameraPreview");
        if (mPreviewFlag) {
            return;
        }
        mPreviewFlag = true;

        if (mUGCKitRecordConfig.mQuality >= 0) {
            // 推荐配置
            TXRecordCommon.TXUGCSimpleConfig simpleConfig = new TXRecordCommon.TXUGCSimpleConfig();
            simpleConfig.videoQuality = mUGCKitRecordConfig.mQuality;
            simpleConfig.minDuration = mUGCKitRecordConfig.mMinDuration;
            simpleConfig.maxDuration = mUGCKitRecordConfig.mMaxDuration;
            simpleConfig.isFront = mUGCKitRecordConfig.mFrontCamera;
            simpleConfig.touchFocus = mUGCKitRecordConfig.mTouchFocus;
            simpleConfig.needEdit = mUGCKitRecordConfig.mIsNeedEdit;

            if (mRecordSDK != null) {
                mRecordSDK.setVideoRenderMode(mUGCKitRecordConfig.mRenderMode);
                mRecordSDK.setMute(mUGCKitRecordConfig.mIsMute);
            }
            mRecordSDK.startCameraSimplePreview(simpleConfig, videoView);
            TXCAudioUGCRecorder.getInstance().setAECType(mUGCKitRecordConfig.mAECType, UGCKit.getAppContext());
        } else {
            // 自定义配置
            TXRecordCommon.TXUGCCustomConfig customConfig = new TXRecordCommon.TXUGCCustomConfig();
            customConfig.videoResolution = mUGCKitRecordConfig.mResolution;
            customConfig.minDuration = mUGCKitRecordConfig.mMinDuration;
            customConfig.maxDuration = mUGCKitRecordConfig.mMaxDuration;
            customConfig.videoBitrate = mUGCKitRecordConfig.mVideoBitrate;
            customConfig.videoGop = mUGCKitRecordConfig.mGOP;
            customConfig.videoFps = mUGCKitRecordConfig.mFPS;
            customConfig.isFront = mUGCKitRecordConfig.mFrontCamera;
            customConfig.touchFocus = mUGCKitRecordConfig.mTouchFocus;
            customConfig.needEdit = mUGCKitRecordConfig.mIsNeedEdit;

            mRecordSDK.startCameraCustomPreview(customConfig, videoView);
        }

        if (mRecordSDK != null) {
            mRecordSDK.setRecordSpeed(mUGCKitRecordConfig.mRecordSpeed);
            mRecordSDK.setHomeOrientation(mUGCKitRecordConfig.mHomeOrientation);
            mRecordSDK.setRenderRotation(mUGCKitRecordConfig.mRenderRotation);
            mRecordSDK.setAspectRatio(mUGCKitRecordConfig.mAspectRatio);
            mRecordSDK.setVideoRecordListener(this);
        }
    }

    public void stopCameraPreview() {
        Log.d(TAG, "stopCameraPreview");
        if (mRecordSDK != null) {
            mRecordSDK.stopCameraPreview();
        }
        mPreviewFlag = false;
    }

    public int getRecordState() {
        return mCurrentState;
    }

    public void updateBeautyParam(@NonNull BeautyParams beautyParams) {
        mUGCKitRecordConfig.mBeautyParams = beautyParams;
        if (mRecordSDK != null && beautyParams != null) {
            mRecordSDK.getBeautyManager().setBeautyStyle(beautyParams.mBeautyStyle);
            mRecordSDK.getBeautyManager().setBeautyLevel(beautyParams.mBeautyLevel);
            mRecordSDK.getBeautyManager().setWhitenessLevel(beautyParams.mWhiteLevel);
            mRecordSDK.getBeautyManager().setRuddyLevel(beautyParams.mRuddyLevel);
            mRecordSDK.getBeautyManager().setFaceSlimLevel(beautyParams.mFaceSlimLevel);
            mRecordSDK.getBeautyManager().setEyeScaleLevel(beautyParams.mBigEyeLevel);
            mRecordSDK.getBeautyManager().setFaceVLevel(beautyParams.mFaceVLevel);
            mRecordSDK.getBeautyManager().setFaceShortLevel(beautyParams.mFaceShortLevel);
            mRecordSDK.getBeautyManager().setChinLevel(beautyParams.mChinSlimLevel);
            mRecordSDK.getBeautyManager().setNoseSlimLevel(beautyParams.mNoseSlimLevel);
            mRecordSDK.getBeautyManager().setMotionTmpl(beautyParams.mMotionTmplPath);
            mRecordSDK.getBeautyManager().setEyeLightenLevel(beautyParams.mEyeLightenLevel);
            mRecordSDK.getBeautyManager().setToothWhitenLevel(beautyParams.mToothWhitenLevel);
            mRecordSDK.getBeautyManager().setWrinkleRemoveLevel(beautyParams.mWrinkleRemoveLevel);
            mRecordSDK.getBeautyManager().setPounchRemoveLevel(beautyParams.mPounchRemoveLevel);
            mRecordSDK.getBeautyManager().setSmileLinesRemoveLevel(beautyParams.mSmileLinesRemoveLevel);
            mRecordSDK.getBeautyManager().setForeheadLevel(beautyParams.mForeheadLevel);
            mRecordSDK.getBeautyManager().setEyeDistanceLevel(beautyParams.mEyeDistanceLevel);
            mRecordSDK.getBeautyManager().setEyeAngleLevel(beautyParams.mEyeAngleLevel);
            mRecordSDK.getBeautyManager().setMouthShapeLevel(beautyParams.mMouthShapeLevel);
            mRecordSDK.getBeautyManager().setNoseWingLevel(beautyParams.mNoseWingLevel);
            mRecordSDK.getBeautyManager().setNosePositionLevel(beautyParams.mNosePositionLevel);
            mRecordSDK.getBeautyManager().setLipsThicknessLevel(beautyParams.mLipsThicknessLevel);
            mRecordSDK.getBeautyManager().setFaceBeautyLevel(beautyParams.mFaceBeautyLevel);
            mRecordSDK.getBeautyManager().setGreenScreenFile(beautyParams.mGreenFile);
            mRecordSDK.getBeautyManager().setFilterStrength(beautyParams.mFilterStrength / 10.f);
        }
    }

    public void updateMotionParam(@NonNull BeautyParams beautyParams) {
        if (mRecordSDK != null) {
            mRecordSDK.getBeautyManager().setMotionTmpl(beautyParams.mMotionTmplPath);
        }
    }

    /**
     * 更新当前屏比
     */
    public void updateAspectRatio() {
        int aspectRatio = UGCKitRecordConfig.getInstance().mAspectRatio;
        switch (aspectRatio) {
            case TXRecordCommon.VIDEO_ASPECT_RATIO_9_16:
                if (mRecordSDK != null) {
                    mRecordSDK.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
                }
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_3_4:
                if (mRecordSDK != null) {
                    mRecordSDK.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_3_4);
                }
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_1_1:
                if (mRecordSDK != null) {
                    mRecordSDK.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_1_1);
                }
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_4_3:
                if (mRecordSDK != null) {
                    mRecordSDK.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_4_3);
                }
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_16_9:
                if (mRecordSDK != null) {
                    mRecordSDK.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_16_9);
                }
                break;
        }
    }

    /**
     * 拍照API {@link TXUGCRecord#snapshot(TXRecordCommon.ITXSnapshotListener)}
     */
    public void takePhoto(@Nullable final RecordModeView.OnSnapListener listener) {
        if (mRecordSDK != null) {
            mRecordSDK.snapshot(new TXRecordCommon.ITXSnapshotListener() {
                @Override
                public void onSnapshot(final Bitmap bitmap) {
                    String fileName = System.currentTimeMillis() + ".jpg";
                    MediaStore.Images.Media.insertImage(UGCKit.getAppContext().getContentResolver(), bitmap, fileName, null);

                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSnap(bitmap);
                            }
                        }
                    });
                }
            });
        }
    }

    public TXUGCPartsManager getPartManager() {
        if (mRecordSDK != null) {
            return mRecordSDK.getPartsManager();
        }
        return null;
    }

    /**
     * 初始化草稿箱功能
     *
     * @param context
     */
    public void initRecordDraft(Context context) {
        mRecordDraftManager = new RecordDraftManager(context);
        RecordDraftInfo lastDraftInfo = mRecordDraftManager.getLastDraftInfo();
        if (lastDraftInfo == null) {
            return;
        }
        List<RecordDraftInfo.RecordPart> recordPartList = lastDraftInfo.getPartList();
        if (recordPartList == null || recordPartList.size() == 0) {
            return;
        }

        long duration = 0;
        int recordPartSize = recordPartList.size();
        Log.d(TAG, "initRecordDraft recordPartSize:" + recordPartSize);
        for (int i = 0; i < recordPartSize; i++) {
            RecordDraftInfo.RecordPart recordPart = recordPartList.get(i);
            if (mRecordSDK != null) {
                mRecordSDK.getPartsManager().insertPart(recordPart.getPath(), i);
            }
            TXVideoEditConstants.TXVideoInfo txVideoInfo = TXVideoInfoReader.getInstance(context).getVideoFileInfo(recordPart.getPath());
            if (txVideoInfo != null) {
                duration = duration + txVideoInfo.duration;
            }

            if (mOnRestoreDraftListener != null) {
                mOnRestoreDraftListener.onDraftProgress((int) duration);
            }
        }

        if (recordPartList != null && recordPartList.size() > 0) {
            if (mOnRestoreDraftListener != null) {
                mOnRestoreDraftListener.onDraftTotal(mRecordSDK.getPartsManager().getDuration());
            }
        }
    }

    public void setOnRestoreDraftListener(OnRestoreDraftListener listener) {
        mOnRestoreDraftListener = listener;
    }

    public void setConfig(UGCKitRecordConfig config) {
        mUGCKitRecordConfig = config;
    }

    public interface OnRestoreDraftListener {
        void onDraftProgress(long duration);

        void onDraftTotal(long duration);
    }

    public void deleteAllParts() {
        if (mRecordSDK != null) {
            mRecordSDK.getPartsManager().deleteAllParts();
        }
        // 草稿箱也相应删除
        if (mRecordDraftManager != null) {
            mRecordDraftManager.deleteLastRecordDraft();
        }
    }

    /**
     * 保存上一段录制的视频到草稿箱
     */
    public void saveLastPart() {
        if (mRecordSDK != null && mRecordDraftManager != null) {
            List<String> pathList = mRecordSDK.getPartsManager().getPartsPathList();
            if (pathList != null && pathList.size() > 0) {
                String lastPath = pathList.get(pathList.size() - 1);
                mRecordDraftManager.saveLastPart(lastPath);
            }
        }
    }

    /**
     * 从草稿箱删除上一段录制的视频
     */
    public void deleteLastPart() {
        mRecordSDK.getPartsManager().deleteLastPart();
        // 删除草稿
        if (mRecordDraftManager != null) {
            mRecordDraftManager.deleteLastPart();
        }
    }

    /**
     * 开始录制
     */
    public int startRecord() {
        Log.d(TAG, "startRecord mCurrentState" + mCurrentState);
        if (mCurrentState == STATE_STOP) {
            String customVideoPath = VideoPathUtil.getCustomVideoOutputPath();
            String customCoverPath = customVideoPath.replace(".mp4", ".jpg");

            int retCode = 0;

            if (mRecordSDK != null) {
                retCode = mRecordSDK.startRecord(customVideoPath, customCoverPath);
            }
            Log.d(TAG, "startRecord retCode:" + retCode);
            if (retCode != TXRecordCommon.START_RECORD_OK) {
                if (retCode == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                    ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_start_record_not_init));
                } else if (retCode == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                    ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_start_record_not_finish));
                } else if (retCode == TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY) {
                    ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_start_record_path_empty));
                } else if (retCode == TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18) {
                    ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_start_record_version_below));
                }
                // 增加了TXUgcSDK.licence校验的返回错误码
                else if (retCode == TXRecordCommon.START_RECORD_ERR_LICENCE_VERIFICATION_FAILED) {
                    ToastUtil.toastShortMessage("licence校验失败，请调用TXUGCBase.getLicenceInfo(Context context)获取licence信息");
                }
                return START_RECORD_FAIL;
            }
            LogReport.getInstance().reportStartRecord(retCode);

            RecordMusicManager.getInstance().startMusic();
        } else if (mCurrentState == STATE_PAUSE) {
            resumeRecord();
        }

        mCurrentState = STATE_START;
        return START_RECORD_SUCC;
    }

    /**
     * 继续录制
     */
    public void resumeRecord() {
        Log.d(TAG, "resumeRecord");
        if (mRecordSDK != null) {
            mRecordSDK.resumeRecord();
        }
        RecordMusicManager.getInstance().resumeMusic();

        mCurrentState = STATE_RESUME;
    }

    /**
     * 暂停录制
     * FIXBUG:被打断时调用，暂停录制，修改状态，跳转到音乐界面也会被调用
     */
    public void pauseRecord() {
        Log.d(TAG, "pauseRecord");
        if (mCurrentState == STATE_START || mCurrentState == STATE_RESUME) {
            RecordMusicManager.getInstance().pauseMusic();
            if (mRecordSDK != null) {
                mRecordSDK.pauseRecord();
            }
            mCurrentState = STATE_PAUSE;
        }
        mPreviewFlag = false;

    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        Log.d(TAG, "stopRecord");
        int size = 0;
        if (mRecordSDK != null) {
            size = mRecordSDK.getPartsManager().getPartsPathList().size();
        }
        if (mCurrentState == STATE_STOP && size == 0) {
            //如果录制未开始，且录制片段个数为0，则不需要停止录制
            return;
        }
        if (mRecordSDK != null) {
            mRecordSDK.stopBGM();
            mRecordSDK.stopRecord();
        }

        mCurrentState = STATE_STOP;
    }

    /**
     * 释放Record SDK资源
     */
    public void releaseRecord() {
        Log.d(TAG, "releaseRecord");
        if (mRecordSDK != null) {
            mRecordSDK.stopBGM();
            mRecordSDK.stopCameraPreview();
            mRecordSDK.setVideoRecordListener(null);
            mRecordSDK.getPartsManager().deleteAllParts();
            mRecordSDK.release();
            mRecordSDK = null;
            mPreviewFlag = false;

            RecordMusicManager.getInstance().deleteMusic();
        }
        // 删除草稿箱视频片段
        if (mRecordDraftManager != null) {
            mRecordDraftManager.deleteLastRecordDraft();
        }

//        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    public void setFilter(Bitmap leftBmp, float leftSpecialRatio, Bitmap rightBmp,
                          float rightSpecialRatio, float leftRatio) {
        if (mRecordSDK != null) {
            mRecordSDK.setFilter(leftBmp, leftSpecialRatio, rightBmp, rightSpecialRatio, leftRatio);
        }
    }

    public void setRecordSpeed(int speed) {
        if (mRecordSDK != null) {
            mRecordSDK.setRecordSpeed(speed);
        }
    }

    public void setVideoRecordListener(OnVideoRecordListener listener) {
        mOnVideoRecordListener = listener;
    }

    @Override
    public void onRecordEvent(int event, Bundle param) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            saveLastPart();
            if (mOnVideoRecordListener != null) {
                mOnVideoRecordListener.onRecordEvent(event);
            }
        } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
            ToastUtil.toastShortMessage(UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_on_record_event_evt_camera_cannot_use));
        } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
            ToastUtil.toastShortMessage(UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_on_record_event_evt_mic_cannot_use));
        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        if (mOnVideoRecordListener != null) {
            mOnVideoRecordListener.onRecordProgress(milliSecond);
        }
    }

    @Override
    public void onRecordComplete(@NonNull TXRecordCommon.TXRecordResult result) {
        Log.d(TAG, "onRecordComplete");
        if (result.retCode < 0) {
            ToastUtil.toastShortMessage(UGCKit.getAppContext().getResources().getString(R.string.ugckit_video_record_activity_on_record_complete_fail_tip) + result.descMsg);
        } else {
            mCurrentState = STATE_STOP;
            pauseRecord();
            mRecordVideoPath = result.videoPath;
            if (mOnVideoRecordListener != null) {
                mOnVideoRecordListener.onRecordComplete(result);
            }
        }
    }

    public String getRecordVideoPath() {
        return mRecordVideoPath;
    }

    public void switchCamera(boolean isFront) {
        TXUGCRecord record = getRecorder();
        if (record != null) {
            record.switchCamera(isFront);
        }
        if (mUGCKitRecordConfig != null) {
            mUGCKitRecordConfig.mFrontCamera = isFront;
        }
    }

    public interface OnVideoRecordListener {
        void onRecordProgress(long milliSecond);

        void onRecordEvent(int event);

        void onRecordComplete(TXRecordCommon.TXRecordResult result);
    }
}

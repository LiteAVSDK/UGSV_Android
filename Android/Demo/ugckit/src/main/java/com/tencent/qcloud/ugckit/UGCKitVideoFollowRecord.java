package com.tencent.qcloud.ugckit;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.basic.JumpActivityMgr;
import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.ProcessKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.followRecord.AbsVideoFollowRecordUI;
import com.tencent.qcloud.ugckit.module.followRecord.CountDownTimerView;
import com.tencent.qcloud.ugckit.module.followRecord.FollowRecordConfig;
import com.tencent.qcloud.ugckit.module.followRecord.FollowRecordInfo;
import com.tencent.qcloud.ugckit.module.followRecord.FollowRecordJoiner;
import com.tencent.qcloud.ugckit.module.followRecord.ICountDownTimerView;
import com.tencent.qcloud.ugckit.module.followRecord.IFollowRecordRightLayout;
import com.tencent.qcloud.ugckit.module.record.AudioFocusManager;
import com.tencent.qcloud.ugckit.module.record.ScrollFilterView;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.VideoRecordSDK;
import com.tencent.qcloud.ugckit.module.record.beauty.BeautyPannel;
import com.tencent.qcloud.ugckit.module.record.beauty.BeautyParams;
import com.tencent.qcloud.ugckit.module.record.interfaces.IBeautyPannel;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.record.RecordButton;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

public class UGCKitVideoFollowRecord extends AbsVideoFollowRecordUI implements IFollowRecordRightLayout.OnItemClickListener, RecordButton.OnRecordButtonListener,
        IBeautyPannel.IOnBeautyParamsChangeListener, ScrollFilterView.OnRecordFilterListener, VideoRecordSDK.OnVideoRecordListener,
        FollowRecordJoiner.OnFollowRecordJoinListener {

    private static final String TAG = "UGCKitVideoFollowRecord";
    private ProgressDialog mPDLoading;
    private OnFollowRecordListener mOnFollowRecordListener;
    private ProgressDialogUtil mProgressDialogUtil;
    private ProgressFragmentUtil mProgressFragmentUtil;
    private FragmentActivity mActivity;

    public UGCKitVideoFollowRecord(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoFollowRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoFollowRecord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
    }

    private void initDefault() {
        mActivity = (FragmentActivity) getContext();
        // 初始化SDK:TXUGCRecord
        VideoRecordSDK.getInstance().initSDK();
        VideoRecordSDK.getInstance().setVideoRecordListener(this);

        // 设置默认的录制模式
        getFollowRecordBottomLayout().getRecordButton().setCurrentRecordMode(UGCKitRecordConfig.getInstance().mRecordMode);
        // 点击"下一步"
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();

                VideoRecordSDK.getInstance().stopRecord();
            }
        });

        // 点击"右侧工具栏"（包括"美颜"，"倒计时"）
        getFollowRecordRightLayout().setOnItemClickListener(this);

        // 点击"录制按钮"（包括"单击拍"，"按住拍"）
        getFollowRecordBottomLayout().setOnRecordButtonListener(this);

        // 设置"美颜面板"监听器
        getBeautyPannel().setBeautyParamsChangeListener(this);
        getScrollFilterView().setOnRecordFilterListener(this);

        mProgressDialogUtil = new ProgressDialogUtil(mActivity);
    }

    @Override
    public void start() {
        FollowRecordConfig config = FollowRecordConfig.getInstance();
        config.mBeautyParams = new BeautyParams();

        VideoRecordSDK.getInstance().initConfig(config);
        // 打开合唱预览界面
        VideoRecordSDK.getInstance().startCameraPreview(getChorusPlayView().getChorusRecordView());
        // 播放跟拍视频
        getChorusPlayView().startPlayChorusVideo();
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop");
        getFollowRecordBottomLayout().getRecordButton().pauseRecordAnim();
        getFollowRecordBottomLayout().closeTorch();
        // 停止录制预览界面
        VideoRecordSDK.getInstance().stopCameraPreview();
        // 暂停录制
        VideoRecordSDK.getInstance().pauseRecord();
        // 停止播放跟拍视频
        getChorusPlayView().stopPlayChorusVideo();
    }

    @Override
    public void release() {
        getFollowRecordBottomLayout().getRecordProgressView().release();
        // 停止录制
        VideoRecordSDK.getInstance().releaseRecord();
    }

    @Override
    public void setFollowRecordInfo(FollowRecordInfo info) {
        // 设置合唱基本信息
        FollowRecordConfig.getInstance().videoInfo = info;
        // 获取跟拍基本信息
        FollowRecordConfig.getInstance().loadPlayVideoInfo();
        // 初始化播放器界面
        getChorusPlayView().initPlayerView();
        // 初始化最大/最小视频录制时长
        getFollowRecordBottomLayout().initDuration();
    }

    @Override
    public void screenOrientationChange() {
        Log.d(TAG, "screenOrientationChange");
        VideoRecordSDK.getInstance().stopCameraPreview();

        VideoRecordSDK.getInstance().pauseRecord();

        VideoRecordSDK.getInstance().startCameraPreview(getChorusPlayView().getChorusRecordView());
    }

    @Override
    public void backPressed() {
        Log.d(TAG, "backPressed");
        // 录制已停止，则回调"录制被取消"
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_STOP) {
            getChorusPlayView().stopPlayChorusVideo();
            getChorusPlayView().release();

            if (mOnFollowRecordListener != null) {
                mOnFollowRecordListener.onFollowRecordCanceled();
            }
            return;
        }
        // 录制已开始，点击返回键，暂停录制
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_START) {
            VideoRecordSDK.getInstance().pauseRecord();
        }

        if (mOnFollowRecordListener != null) {
            mOnFollowRecordListener.onFollowRecordCanceled();
        }
    }

    @Override
    public void setOnFollowRecordListener(OnFollowRecordListener listener) {
        mOnFollowRecordListener = listener;
    }

    /**
     * 点击录制开始按钮
     */
    @Override
    public void onRecordStart() {
        Log.d(TAG, "onRecordStart");
        getFollowRecordBottomLayout().disableFunction();
        getFollowRecordRightLayout().setVisibility(View.INVISIBLE);

        VideoRecordSDK.getInstance().startRecord();
        getChorusPlayView().startPlayChorusVideo();

        AudioFocusManager.getInstance().setAudioFocusListener(new AudioFocusManager.OnAudioFocusListener() {
            @Override
            public void onAudioFocusChange() {
                getFollowRecordBottomLayout().getRecordButton().pauseRecordAnim();
            }
        });
        AudioFocusManager.getInstance().requestAudioFocus();
    }

    /**
     * 点击录制暂停按钮
     */
    @Override
    public void onRecordPause() {
        Log.d(TAG, "onRecordPause");
        getFollowRecordBottomLayout().enableFunction();
        getFollowRecordRightLayout().setVisibility(View.VISIBLE);

        VideoRecordSDK.getInstance().pauseRecord();
        getChorusPlayView().pausePlayChorusVideo();

        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    @Override
    public void onTakePhoto() {

    }

    @Override
    public void onShowBeautyPannel() {
        // 隐藏底部工具栏
        getFollowRecordBottomLayout().setVisibility(View.GONE);
        // 隐藏右侧工具栏
        getFollowRecordRightLayout().setVisibility(View.GONE);
        // 显示美颜Pannel
        getBeautyPannel().setVisibility(View.VISIBLE);
    }

    @Override
    public void countDownTimer() {
        getFollowRecordRightLayout().setVisibility(View.GONE);
        getFollowRecordBottomLayout().setVisibility(View.GONE);

        getCountDownTimerView().setOnCountDownListener(new ICountDownTimerView.ICountDownListener() {
            @Override
            public void onCountDownComplete() {
                getFollowRecordBottomLayout().getRecordButton().startRecordAnim();
                getFollowRecordRightLayout().setVisibility(View.VISIBLE);
                getFollowRecordBottomLayout().setVisibility(View.VISIBLE);
            }
        });
        getCountDownTimerView().countDownAnimation(CountDownTimerView.DEFAULT_COUNTDOWN_NUMBER);
    }

    /************************************   美颜Pannel回调接口 Begin  ********************************************/
    @Override
    public void onBeautyParamsChange(@NonNull BeautyParams params, int key) {
        switch (key) {
            case BeautyPannel.BEAUTYPARAM_FILTER:
                FollowRecordConfig.getInstance().mBeautyParams.mFilterBmp = params.mFilterBmp;

                Bitmap filterBmp = params.mFilterBmp;
                TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
                if (record != null) {
                    record.setFilter(filterBmp);
                }

                float specialRatio = getBeautyPannel().getFilterProgress(params.filterIndex) / 10.f;
                VideoRecordSDK.getInstance().setSpecialRatio(specialRatio);

                getScrollFilterView().doTextAnimator();
                break;
            case BeautyPannel.BEAUTYPARAM_BEAUTY:
                FollowRecordConfig.getInstance().mBeautyParams.mBeautyLevel = params.mBeautyLevel;
                FollowRecordConfig.getInstance().mBeautyParams.mBeautyStyle = params.mBeautyStyle;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_WHITE:
                FollowRecordConfig.getInstance().mBeautyParams.mWhiteLevel = params.mWhiteLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACESLIM:
                FollowRecordConfig.getInstance().mBeautyParams.mFaceSlimLevel = params.mFaceSlimLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);

                break;
            case BeautyPannel.BEAUTYPARAM_BIG_EYE:
                FollowRecordConfig.getInstance().mBeautyParams.mBigEyeLevel = params.mBigEyeLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_MOTION_TMPL:
                FollowRecordConfig.getInstance().mBeautyParams.mMotionTmplPath = params.mMotionTmplPath;

                VideoRecordSDK.getInstance().updateMotionParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_GREEN:
                FollowRecordConfig.getInstance().mBeautyParams.mGreenFile = params.mGreenFile;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_RUDDY:
                FollowRecordConfig.getInstance().mBeautyParams.mRuddyLevel = params.mRuddyLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACEV:
                FollowRecordConfig.getInstance().mBeautyParams.mFaceVLevel = params.mFaceVLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACESHORT:
                FollowRecordConfig.getInstance().mBeautyParams.mFaceShortLevel = params.mFaceShortLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_CHINSLIME:
                FollowRecordConfig.getInstance().mBeautyParams.mChinSlimLevel = params.mChinSlimLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_NOSESCALE:
                FollowRecordConfig.getInstance().mBeautyParams.mNoseScaleLevel = params.mNoseScaleLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FILTER_MIX_LEVEL:
                FollowRecordConfig.getInstance().mBeautyParams.mFilterMixLevel = params.mFilterMixLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(FollowRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_EYELIGHTEN:
                UGCKitRecordConfig.getInstance().mBeautyParams.mEyeLightenLevel = params.mEyeLightenLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_TOOTHWHITEN:
                UGCKitRecordConfig.getInstance().mBeautyParams.mToothWhitenLevel = params.mToothWhitenLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_WRINKLEREMOVE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mWrinkleRemoveLevel = params.mWrinkleRemoveLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_POUNCHREMOVE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mPounchRemoveLevel = params.mPounchRemoveLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_SMILELINESREMOVE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mSmileLinesRemoveLevel = params.mSmileLinesRemoveLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FOREHEAD:
                UGCKitRecordConfig.getInstance().mBeautyParams.mForeheadLevel = params.mForeheadLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_EYEDISTANCE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mEyeDistanceLevel = params.mEyeDistanceLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_EYEANGLE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mEyeAngleLevel = params.mEyeAngleLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_MOUTHSHAPE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mMouthShapeLevel = params.mMouthShapeLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_NOSEWING:
                UGCKitRecordConfig.getInstance().mBeautyParams.mNoseWingLevel = params.mNoseWingLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_NOSEPOSITION:
                UGCKitRecordConfig.getInstance().mBeautyParams.mNosePositionLevel = params.mNosePositionLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_LIPSTHICKNESS:
                UGCKitRecordConfig.getInstance().mBeautyParams.mLipsThicknessLevel = params.mLipsThicknessLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACEBEAUTY:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFaceBeautyLevel = params.mFaceBeautyLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
        }
    }

    /************************************   美颜Pannel回调接口 End    ********************************************/

    @Override
    public void onSingleClick(float x, float y) {
        getBeautyPannel().setVisibility(View.GONE);
        getFollowRecordBottomLayout().setVisibility(View.VISIBLE);
        getFollowRecordRightLayout().setVisibility(View.VISIBLE);

        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setFocusPosition(x, y);
        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        getFollowRecordBottomLayout().updateProgress(milliSecond);
    }

    @Override
    public void onRecordEvent() {
        getFollowRecordBottomLayout().getRecordProgressView().clipComplete();
    }

    @Override
    public void onRecordComplete(@NonNull TXRecordCommon.TXRecordResult result) {
        Log.d(TAG, "onRecordComplete result:" + result.retCode);

        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_RECORD, result.retCode, result.descMsg);

        if (result.retCode >= 0) {
            dissmissLoadingDialog();
            getFollowRecordBottomLayout().getRecordButton().pauseRecordAnim();

            mProgressDialogUtil.showProgressDialog();
            mProgressDialogUtil.setProgressDialogMessage(getResources().getString(R.string.tc_video_record_activity_on_record_complete_synthesizing));

            // 加载视频信息
            FollowRecordConfig.getInstance().videoInfo.recordPath = result.videoPath;
            FollowRecordConfig.getInstance().loadRecordVideoInfo();

            // 开始合成
            FollowRecordJoiner.getInstance(getContext()).setChorusJoinListener(this);
            FollowRecordJoiner.getInstance(getContext()).joinChorusVideo();
        }
    }

    private void showLoadingDialog() {
        mPDLoading = new ProgressDialog(getContext());
        mPDLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mPDLoading.setCancelable(false);
        mPDLoading.setCanceledOnTouchOutside(false);
        mPDLoading.show();
    }

    private void dissmissLoadingDialog() {
        if (mPDLoading != null) {
            mPDLoading.dismiss();
        }
    }

    @Override
    public void onChorusProgress(float progress) {
        int progressInt = (int) (progress * 100);
        mProgressDialogUtil.setProgressDialogMessage(getResources().getString(R.string.tc_video_record_activity_on_join_progress_synthesizing) + progressInt + "%");
    }

    @Override
    public void onChorusCompleted(String outputPath) {
        Log.d(TAG, "onFollowRecordCompleted outputPath:" + outputPath);
        mProgressDialogUtil.dismissProgressDialog();

        boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromRecord();
        Log.d(TAG, "onFollowRecordCompleted editFlag:" + editFlag);
        if (editFlag) {
            startPreprocess(outputPath);
        } else {
            if (mOnFollowRecordListener != null) {
                UGCKitResult ugcKitResult = new UGCKitResult();
                ugcKitResult.errorCode = 0;
                ugcKitResult.outputPath = outputPath;

                mOnFollowRecordListener.onFollowRecordCompleted(ugcKitResult);
            }
        }
    }

    private void startPreprocess(String videoPath) {
        mProgressFragmentUtil = new ProgressFragmentUtil(mActivity);
        mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
            @Override
            public void onStop() {
                mProgressFragmentUtil.dismissLoadingProgress();

                ProcessKit.getInstance().stopProcess();
            }
        });

        loadVideoInfo(videoPath);
    }

    private void loadVideoInfo(String videoPath) {
        // 加载视频信息
        TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(videoPath);
        if (info == null) {
            DialogUtil.showDialog(UGCKitImpl.getAppContext(), getResources().getString(R.string.tc_video_preprocess_activity_edit_failed),
                    getResources().getString(R.string.tc_video_preprocess_activity_does_not_support_android_version_below_4_3), null);
        } else {
            // 设置视频基本信息
            VideoEditerSDK.getInstance().initSDK();
            VideoEditerSDK.getInstance().getEditer().setVideoPath(videoPath);
            VideoEditerSDK.getInstance().setTXVideoInfo(info);
            VideoEditerSDK.getInstance().setCutterStartTime(0, info.duration);
            // 开始视频预处理，产生录制的缩略图
            ProcessKit.getInstance().startProcess();
            ProcessKit.getInstance().setOnThumbnailListener(new ProcessKit.OnThumbnailListener() {
                @Override
                public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
                    VideoEditerSDK.getInstance().addThumbnailBitmap(timeMs, bitmap);
                }
            });
            ProcessKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
                @Override
                public void onUIProgress(float progress) {
                    mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
                }

                @Override
                public void onUIComplete(int retCode, String desc) {
                    // 更新UI控件
                    mProgressFragmentUtil.dismissLoadingProgress();
                    if (mOnFollowRecordListener != null) {
                        UGCKitResult ugcKitResult = new UGCKitResult();
                        ugcKitResult.errorCode = 0;
                        mOnFollowRecordListener.onFollowRecordCompleted(ugcKitResult);
                    }
                }

                @Override
                public void onUICancel() {
                    // 更新Activity
                    if (mOnFollowRecordListener != null) {
                        mOnFollowRecordListener.onFollowRecordCanceled();
                    }
                }
            });
        }
    }

    @Override
    public void setEditVideoFlag(boolean enable) {
        JumpActivityMgr.getInstance().setEditFlagFromFollowRecord(enable);
    }

}

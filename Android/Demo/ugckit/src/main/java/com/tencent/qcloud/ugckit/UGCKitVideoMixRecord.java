package com.tencent.qcloud.ugckit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordConfigBuildInfo;
import com.tencent.qcloud.ugckit.utils.VideoPathUtil;
import com.tencent.liteav.demo.beauty.BeautyParams;
import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.ProcessKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.mixrecord.CountDownTimerView;
import com.tencent.qcloud.ugckit.module.mixrecord.ICountDownTimerView;
import com.tencent.qcloud.ugckit.module.mixrecord.IMixRecordRightLayout;
import com.tencent.qcloud.ugckit.module.mixrecord.AbsVideoTripleMixRecordUI;
import com.tencent.qcloud.ugckit.module.mixrecord.IMixRecordJoinListener;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordActionData;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordConfig;
import com.tencent.qcloud.ugckit.module.mixrecord.MixRecordJoiner;
import com.tencent.qcloud.ugckit.module.record.AudioFocusManager;
import com.tencent.qcloud.ugckit.module.record.RecordButton;
import com.tencent.qcloud.ugckit.module.record.ScrollFilterView;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.VideoRecordSDK;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

import java.util.List;

public class UGCKitVideoMixRecord extends AbsVideoTripleMixRecordUI implements IMixRecordRightLayout.OnItemClickListener, RecordButton.OnRecordButtonListener,
        ScrollFilterView.OnRecordFilterListener, VideoRecordSDK.OnVideoRecordListener,
        IMixRecordJoinListener {
    private static final String TAG = "UGCKitVideoTripleRecord";
    private OnMixRecordListener mOnMixRecordListener;
    private ProgressDialogUtil mProgressDialogUtil;
    private ProgressFragmentUtil mProgressFragmentUtil;
    private FragmentActivity mActivity;
    private MixRecordJoiner mJoiner;
    private MixRecordConfig mConfig;

    public UGCKitVideoMixRecord(Context context) {
        super(context);
        initDefault();
    }

    public UGCKitVideoMixRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault();
    }

    public UGCKitVideoMixRecord(Context context, AttributeSet attrs, int defStyleAttr) {
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


        // 点击"右侧工具栏"（包括"美颜"，"倒计时"）
        getFollowRecordRightLayout().setOnItemClickListener(this);

        // 点击"录制按钮"（包括"单击拍"，"按住拍"）
        getFollowRecordBottomLayout().setOnRecordButtonListener(this);

        getScrollFilterView().setOnRecordFilterListener(this);

        mProgressDialogUtil = new ProgressDialogUtil(mActivity);
        mJoiner = new MixRecordJoiner(getContext());
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MixRecordActionData data = new MixRecordActionData();
                mOnMixRecordListener.onMixRecordAction(MixRecordActionT.MIX_RECORD_ACTION_T_SELECT, data);
            }
        });

        TXUGCRecord txugcRecord = VideoRecordSDK.getInstance().getRecorder();
        UGCBeautyKit manager = new UGCBeautyKit(txugcRecord);
        getBeautyPanel().setProxy(manager);
    }

    @Override
    public void start() {
        // 打开合唱预览界面
        VideoRecordSDK.getInstance().startCameraPreview(getPlayViews().getVideoView());
        // 播放跟拍视频
        getPlayViews().startVideo();
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
        getPlayViews().stopVideo();
    }

    @Override
    public void release() {
        getFollowRecordBottomLayout().getRecordProgressView().release();
        // 停止录制
        VideoRecordSDK.getInstance().releaseRecord();
        getPlayViews().releaseVideo();

        // 录制TXUGCRecord是单例，需要释放时还原配置
        getBeautyPanel().clear();

        VideoRecordSDK.getInstance().setVideoRecordListener(null);
    }

    @Override
    public void setMixRecordInfo(MixRecordConfigBuildInfo buildInfo) {
        MixRecordConfig info = new MixRecordConfig();
        info.setInfo(buildInfo.getVideoPaths(), buildInfo.getRecordIndex(), buildInfo.getWidth(), buildInfo.getHeight(), buildInfo.getRecordRatio());
        mConfig = info;
        mConfig.mBeautyParams = new BeautyParams();
        mConfig.mIsMute = true;
        // 设置默认美颜
        mConfig.mBeautyParams.mBeautyStyle = 0;
        mConfig.mBeautyParams.mBeautyLevel = 4;
        mConfig.mBeautyParams.mWhiteLevel = 1;

        List<String> paths = mConfig.getPaths();
        for (int i = 0; i < paths.size(); i++) {
            getPlayViews().init(i, paths.get(i));
        }
        // 初始化最大/最小视频录制时长
        getFollowRecordBottomLayout().setDuration(mConfig.mMinDuration, mConfig.mMaxDuration);
        // 初始化默认配置
        VideoRecordSDK.getInstance().initConfig(mConfig);
        VideoRecordSDK.getInstance().updateBeautyParam(mConfig.mBeautyParams);
    }

    @Override
    public void screenOrientationChange() {
        Log.d(TAG, "screenOrientationChange");
        VideoRecordSDK.getInstance().stopCameraPreview();

        VideoRecordSDK.getInstance().pauseRecord();

        VideoRecordSDK.getInstance().startCameraPreview(getPlayViews().getVideoView());//
    }

    @Override
    public void backPressed() {
        Log.d(TAG, "backPressed");
        // 录制已停止，则回调"录制被取消"
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_STOP) {
            getPlayViews().releaseVideo();

            if (mOnMixRecordListener != null) {
                mOnMixRecordListener.onMixRecordCanceled();
            }
            return;
        }
        // 录制已开始，点击返回键，暂停录制
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_START) {
            VideoRecordSDK.getInstance().pauseRecord();
        }

        if (mOnMixRecordListener != null) {
            mOnMixRecordListener.onMixRecordCanceled();
        }
    }

    @Override
    public void setOnMixRecordListener(OnMixRecordListener listener) {
        mOnMixRecordListener = listener;
    }

    @Override
    public void updateMixFile(int index, String filePath) {
        if (mConfig != null) {
            mConfig.updateInfo(index, filePath);
            getFollowRecordBottomLayout().setDuration(mConfig.mMinDuration, mConfig.mMaxDuration);
        }
        getPlayViews().updateFile(index, filePath);
    }

    /**
     * 点击录制开始按钮
     */
    @Override
    public void onRecordStart() {
        Log.d(TAG, "onRecordStart");
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getFollowRecordBottomLayout().disableFunction();
        getFollowRecordRightLayout().setVisibility(View.INVISIBLE);

        int retCode = VideoRecordSDK.getInstance().startRecord();
        if (retCode == VideoRecordSDK.START_RECORD_FAIL) { //点击开始录制失败，录制按钮状态变为暂停
            getFollowRecordBottomLayout().getRecordButton().pauseRecordAnim();
            return;
        }
        if (VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size() == 0) {
            getPlayViews().seekVideo(0);
        }
        getPlayViews().startVideo();

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
        getPlayViews().pauseVideo();

        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    @Override
    public void onTakePhoto() {

    }

    @Override
    public void onDeleteParts(int partsSize, long duration) {
        if (partsSize == 0) { //可以编辑
            getTitleBar().setVisible(true, ITitleBarLayout.POSITION.RIGHT);
        }
        //seek to duration
        getPlayViews().seekVideo(duration);
    }

    @Override
    public void onShowBeautyPanel() {
        // 隐藏底部工具栏
        getFollowRecordBottomLayout().setVisibility(View.GONE);
        // 隐藏右侧工具栏
        getFollowRecordRightLayout().setVisibility(View.GONE);
        // 显示美颜Pannel
        getBeautyPanel().setVisibility(View.VISIBLE);
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

    @Override
    public void onSingleClick(float x, float y) {
        getBeautyPanel().setVisibility(View.GONE);
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
            getFollowRecordBottomLayout().getRecordButton().pauseRecordAnim();

            mProgressDialogUtil.showProgressDialog();
            mProgressDialogUtil.setProgressDialogMessage(getResources().getString(R.string.tc_video_record_activity_on_record_complete_synthesizing));

            mConfig.setRecordPath(result.videoPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 开始合成
                    MixRecordJoiner.JoinerParams params = new MixRecordJoiner.JoinerParams();
                    params.videoSourceList = mConfig.getPaths();
                    params.mCavasWith = mConfig.getWidth();
                    params.mCavasHeight = mConfig.getHeight();
                    params.mRects = getPlayViews().getCombineRects(mConfig);
                    params.mVideoOutputPath = VideoPathUtil.getCustomVideoOutputPath("Triple_Shot_");
                    MixRecordJoiner joiner = new MixRecordJoiner(getContext());
                    params.mVolumes = mConfig.getVolumes();
                    joiner.setmListener(UGCKitVideoMixRecord.this);
                    joiner.joinVideo(params);
                }
            }, "MixRecordT").start();
        }
    }

    @Override
    public void onChorusProgress(float progress) {
        int progressInt = (int) (progress * 100);
        mProgressDialogUtil.setProgressDialogMessage(getResources().getString(R.string.tc_video_record_activity_on_join_progress_synthesizing) + progressInt + "%");
    }

    @Override
    public void onChorusCompleted(String outputPath) {
        Log.d(TAG, "onMixRecordCompleted outputPath:" + outputPath);
        mProgressDialogUtil.dismissProgressDialog();

        boolean editFlag = mConfig.mIsNeedEdit;
        Log.d(TAG, "onMixRecordCompleted editFlag:" + editFlag);
        if (editFlag) {
            startPreprocess(outputPath);
        } else {
            if (mOnMixRecordListener != null) {
                UGCKitResult ugcKitResult = new UGCKitResult();
                ugcKitResult.errorCode = 0;
                ugcKitResult.outputPath = outputPath;

                mOnMixRecordListener.onMixRecordCompleted(ugcKitResult);
            }
        }
    }

    private void startPreprocess(final String videoPath) {
        mProgressFragmentUtil = new ProgressFragmentUtil(mActivity);
        mProgressFragmentUtil.showLoadingProgress(new ProgressFragmentUtil.IProgressListener() {
            @Override
            public void onStop() {
                mProgressFragmentUtil.dismissLoadingProgress();

                ProcessKit.getInstance().stopProcess();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadVideoInfo(videoPath);
            }
        }, "MixRecord_preprocess").start();
    }

    private void loadVideoInfo(String videoPath) {
        // 加载视频信息
        TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance(UGCKit.getAppContext()).getVideoFileInfo(videoPath);
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
            ProcessKit.getInstance().setOnUpdateUIListener(new OnUpdateUIListener() {
                @Override
                public void onUIProgress(float progress) {
                    mProgressFragmentUtil.updateGenerateProgress((int) (progress * 100));
                }

                @Override
                public void onUIComplete(int retCode, String desc) {
                    // 更新UI控件
                    mProgressFragmentUtil.dismissLoadingProgress();
                    if (mOnMixRecordListener != null) {
                        UGCKitResult ugcKitResult = new UGCKitResult();
                        ugcKitResult.errorCode = 0;
                        mOnMixRecordListener.onMixRecordCompleted(ugcKitResult);
                    }
                }

                @Override
                public void onUICancel() {
                    // 更新Activity
                    if (mOnMixRecordListener != null) {
                        mOnMixRecordListener.onMixRecordCanceled();
                    }
                }
            });
        }
    }

    @Override
    public void setEditVideoFlag(boolean enable) {
        mConfig.mIsNeedEdit = enable;
    }

}

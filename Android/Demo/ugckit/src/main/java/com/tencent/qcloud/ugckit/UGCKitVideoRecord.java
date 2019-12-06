package com.tencent.qcloud.ugckit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.tencent.qcloud.ugckit.UGCKitImpl;
import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.basic.JumpActivityMgr;
import com.tencent.qcloud.ugckit.basic.OnUpdateUIListener;
import com.tencent.qcloud.ugckit.basic.UGCKitResult;
import com.tencent.qcloud.ugckit.module.ProcessKit;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.bgm.view.SoundEffectsPannel;
import com.tencent.qcloud.ugckit.module.record.AbsVideoRecordUI;
import com.tencent.qcloud.ugckit.module.record.AudioFocusManager;
import com.tencent.qcloud.ugckit.module.record.MusicInfo;
import com.tencent.qcloud.ugckit.module.record.PhotoSoundPlayer;
import com.tencent.qcloud.ugckit.module.record.RecordBottomLayout;
import com.tencent.qcloud.ugckit.module.record.RecordModeView;
import com.tencent.qcloud.ugckit.module.record.RecordMusicManager;
import com.tencent.qcloud.ugckit.module.record.ScrollFilterView;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.VideoRecordSDK;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordButton;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordMusicPannel;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordRightLayout;
import com.tencent.qcloud.ugckit.utils.DialogUtil;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.TelephonyUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.dialog.ProgressDialogUtil;
import com.tencent.qcloud.ugckit.component.dialogfragment.ProgressFragmentUtil;
import com.tencent.qcloud.ugckit.module.record.beauty.BeautyPannel;
import com.tencent.qcloud.ugckit.module.record.beauty.BeautyParams;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

public class UGCKitVideoRecord extends AbsVideoRecordUI implements
        IRecordRightLayout.OnItemClickListener,
        IRecordButton.OnRecordButtonListener,
        SoundEffectsPannel.SoundEffectsSettingPannelListener,
        BeautyPannel.IOnBeautyParamsChangeListener,
        IRecordMusicPannel.MusicChangeListener,
        ScrollFilterView.OnRecordFilterListener,
        VideoRecordSDK.OnVideoRecordListener {

    private static final String TAG = "UGCKitVideoRecord";
    private OnRecordListener mOnRecordListener;
    private OnMusicChooseListener mOnMusicListener;
    private FragmentActivity mActivity;
    private ProgressFragmentUtil mProgressFragmentUtil;
    private ProgressDialogUtil mProgressDialogUtil;

    public UGCKitVideoRecord(Context context) {
        super(context);
        initDefault(context);
    }

    public UGCKitVideoRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefault(context);
    }

    public UGCKitVideoRecord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault(context);
    }

    private void initDefault(Context context) {
        mActivity = (FragmentActivity) getContext();
        // 初始化SDK:TXUGCRecord
        VideoRecordSDK.getInstance().initSDK();
        // 初始化视频草稿箱
        VideoRecordSDK.getInstance().initRecordDraft(context);
        VideoRecordSDK.getInstance().setOnRestoreDraftListener(new VideoRecordSDK.OnRestoreDraftListener() {
            @Override
            public void onDraftProgress(long duration) {
                getRecordBottomLayout().updateProgress((int) duration);
                getRecordBottomLayout().getRecordProgressView().clipComplete();
            }

            @Override
            public void onDraftTotal(long duration) {
                getRecordRightLayout().setMusicIconEnable(false);
                getRecordRightLayout().setAspectIconEnable(false);

                float second = duration / 1000f;
                boolean enable = second >= UGCKitRecordConfig.getInstance().mMinDuration / 1000;
                getTitleBar().setVisible(enable, ITitleBarLayout.POSITION.RIGHT);
            }
        });

        VideoRecordSDK.getInstance().setVideoRecordListener(this);
        // 点击"下一步"
        getTitleBar().setVisible(false, ITitleBarLayout.POSITION.RIGHT);
        getTitleBar().setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialogUtil.showProgressDialog();

                VideoRecordSDK.getInstance().stopRecord();
            }
        });

        // 点击"右侧工具栏"（包括"美颜"，"音乐"，"音效"）
        getRecordRightLayout().setOnItemClickListener(this);

        // 点击"录制按钮"（包括"拍照"，"单击拍"，"按住拍"）
        getRecordBottomLayout().setOnRecordButtonListener(this);
        getRecordBottomLayout().setOnDeleteLastPartListener(new RecordBottomLayout.OnDeleteLastPartListener() {
            @Override
            public void onUpdateTitle(boolean enable) {
                getTitleBar().setVisible(enable, ITitleBarLayout.POSITION.RIGHT);
            }

            @Override
            public void onReRecord() {
                getRecordRightLayout().setMusicIconEnable(true);
                getRecordRightLayout().setAspectIconEnable(true);
            }
        });

        // 设置"音乐面板"监听器
        getRecordMusicPannel().setOnMusicChangeListener(this);
        // 设置"美颜面板"监听器
        getBeautyPannel().setBeautyParamsChangeListener(this);
        // 设置"音效面板"监听器
        getSoundEffectPannel().setSoundEffectsSettingPannelListener(this);

        getScrollFilterView().setOnRecordFilterListener(this);

        TelephonyUtil.getInstance().initPhoneListener();
        mProgressDialogUtil = new ProgressDialogUtil(mActivity);
    }

    @Override
    public void setOnRecordListener(OnRecordListener listener) {
        mOnRecordListener = listener;
    }

    @Override
    public void setOnMusicChooseListener(OnMusicChooseListener listener) {
        mOnMusicListener = listener;
    }

    @Override
    public void start() {
        UGCKitRecordConfig config = UGCKitRecordConfig.getInstance();
        config.mBeautyParams = new BeautyParams();

        VideoRecordSDK.getInstance().initConfig(config);
        // 打开录制预览界面
        VideoRecordSDK.getInstance().startCameraPreview(getRecordVideoView());
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop");
        TelephonyUtil.getInstance().uninitPhoneListener();

        getRecordBottomLayout().getRecordButton().pauseRecordAnim();
        getRecordBottomLayout().closeTorch();
        // 停止录制预览界面
        VideoRecordSDK.getInstance().stopCameraPreview();
        // 暂停录制
        VideoRecordSDK.getInstance().pauseRecord();
    }

    @Override
    public void release() {
        Log.d(TAG, "release");
        getRecordBottomLayout().getRecordProgressView().release();
        // 停止录制
        VideoRecordSDK.getInstance().releaseRecord();
    }

    @Override
    public void screenOrientationChange() {
        Log.d(TAG, "screenOrientationChange");
        VideoRecordSDK.getInstance().stopCameraPreview();

        VideoRecordSDK.getInstance().pauseRecord();

        VideoRecordSDK.getInstance().startCameraPreview(getRecordVideoView());
    }

    @Override
    public void setRecordMusicInfo(@NonNull MusicInfo musicInfo) {
        if (musicInfo != null) {
            Log.d(TAG, "music name:" + musicInfo.name + ", path:" + musicInfo.path);
        }
        getRecordBottomLayout().setVisibility(View.INVISIBLE);
        getRecordRightLayout().setVisibility(View.INVISIBLE);

        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            long duration = record.setBGM(musicInfo.path);
            musicInfo.duration = duration;
            Log.d(TAG, "music duration:" + musicInfo.duration);
        }
        // 设置音乐信息
        RecordMusicManager.getInstance().setRecordMusicInfo(musicInfo);
        // 更新音乐Pannel
        getRecordMusicPannel().setMusicInfo(musicInfo);
        getRecordMusicPannel().setVisibility(View.VISIBLE);

        // 音乐试听
        RecordMusicManager.getInstance().startPreviewMusic();
    }

    @Override
    public void backPressed() {
        Log.d(TAG, "backPressed");
        // 录制已停止，则回调"录制被取消"
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_STOP) {
            if (mOnRecordListener != null) {
                mOnRecordListener.onRecordCanceled();
            }
            return;
        }
        // 录制已开始，点击返回键，暂停录制
        if (VideoRecordSDK.getInstance().getRecordState() == VideoRecordSDK.STATE_START) {
            VideoRecordSDK.getInstance().pauseRecord();
        }

        int size = VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size();
        if (size == 0) {
            if (mOnRecordListener != null) {
                mOnRecordListener.onRecordCanceled();
            }
            return;
        }

        showGiveupRecordDialog();
    }

    /**
     * 显示放弃录制对话框
     */
    private void showGiveupRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder.setTitle(getResources().getString(R.string.cancel_record)).setCancelable(false).setMessage(R.string.confirm_cancel_record_content)
                .setPositiveButton(R.string.give_up, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();

                        VideoRecordSDK.getInstance().deleteAllParts();

                        if (mOnRecordListener != null) {
                            mOnRecordListener.onRecordCanceled();
                        }
                        return;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.wrong_click), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 点击录制开始按钮
     */
    @Override
    public void onRecordStart() {
        getRecordRightLayout().setVisibility(View.INVISIBLE);
        getRecordBottomLayout().startRecord();
        // 开始录制后不能再选择音乐
        getRecordRightLayout().setMusicIconEnable(false);
        // 开始录制后不能切换屏比
        getRecordRightLayout().setAspectIconEnable(false);

        // 开始/继续录制
        VideoRecordSDK.getInstance().startRecord();

        AudioFocusManager.getInstance().setAudioFocusListener(new AudioFocusManager.OnAudioFocusListener() {
            @Override
            public void onAudioFocusChange() {
                VideoRecordSDK.getInstance().pauseRecord();
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
        getRecordRightLayout().setVisibility(View.VISIBLE);
        getRecordBottomLayout().pauseRecord();

        VideoRecordSDK.getInstance().pauseRecord();
        RecordMusicManager.getInstance().pauseMusic();

        AudioFocusManager.getInstance().abandonAudioFocus();
    }

    /**
     * 点击照相
     */
    @Override
    public void onTakePhoto() {
        PhotoSoundPlayer.playPhotoSound();

        VideoRecordSDK.getInstance().takePhoto(new RecordModeView.OnSnapListener() {
            @Override
            public void onSnap(Bitmap bitmap) {
                getSnapshotView().showSnapshotAnim(bitmap);
            }
        });
    }

    @Override
    public void onShowBeautyPannel() {
        // 隐藏底部工具栏
        getRecordBottomLayout().setVisibility(View.GONE);
        // 隐藏右侧工具栏
        getRecordRightLayout().setVisibility(View.GONE);
        // 显示美颜Pannel
        getBeautyPannel().setVisibility(View.VISIBLE);
    }

    /**
     * 点击工具栏按钮"音乐"
     */
    @Override
    public void onShowMusicPannel() {
        boolean isChooseMusicFlag = RecordMusicManager.getInstance().isChooseMusic();
        if (isChooseMusicFlag) {
            // 隐藏底部工具栏
            getRecordBottomLayout().setVisibility(View.GONE);
            // 隐藏右侧工具栏
            getRecordRightLayout().setVisibility(View.GONE);
            // 显示音乐Pannel
            getRecordMusicPannel().setVisibility(View.VISIBLE);

            RecordMusicManager.getInstance().startMusic();
        } else {
            if (mOnMusicListener != null) {
                mOnMusicListener.onChooseMusic(UGCKitRecordConfig.getInstance().musicInfo.position);
            }
        }
    }

    @Override
    public void onShowSoundEffectPannel() {
        // 隐藏底部工具栏
        getRecordBottomLayout().setVisibility(View.GONE);
        // 隐藏右侧工具栏
        getRecordRightLayout().setVisibility(View.GONE);
        // 显示音效Pannel
        getSoundEffectPannel().setVisibility(View.VISIBLE);
    }

    @Override
    public void onAspectSelect(int aspectType) {
        UGCKitRecordConfig.getInstance().mAspectRatio = aspectType;
        VideoRecordSDK.getInstance().updateAspectRatio();
    }

    /************************************   音效Pannel回调接口 Begin  ********************************************/
    @Override
    public void onMicVolumeChanged(float volume) {
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setMicVolume(volume);
        }
    }

    @Override
    public void onClickVoiceChanger(int type) {
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setVoiceChangerType(type);
        }
    }

    @Override
    public void onClickReverb(int type) {
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setReverb(type);
        }
    }

    /************************************   音效Pannel回调接口 End    ********************************************/

    /************************************   美颜Pannel回调接口 Begin  ********************************************/
    @Override
    public void onBeautyParamsChange(@NonNull BeautyParams params, int key) {
        switch (key) {
            case BeautyPannel.BEAUTYPARAM_FILTER:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFilterBmp = params.mFilterBmp;

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
                UGCKitRecordConfig.getInstance().mBeautyParams.mBeautyLevel = params.mBeautyLevel;
                UGCKitRecordConfig.getInstance().mBeautyParams.mBeautyStyle = params.mBeautyStyle;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_WHITE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mWhiteLevel = params.mWhiteLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACESLIM:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFaceSlimLevel = params.mFaceSlimLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);

                break;
            case BeautyPannel.BEAUTYPARAM_BIG_EYE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mBigEyeLevel = params.mBigEyeLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_MOTION_TMPL:
                UGCKitRecordConfig.getInstance().mBeautyParams.mMotionTmplPath = params.mMotionTmplPath;

                VideoRecordSDK.getInstance().updateMotionParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_GREEN:
                UGCKitRecordConfig.getInstance().mBeautyParams.mGreenFile = params.mGreenFile;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_RUDDY:
                UGCKitRecordConfig.getInstance().mBeautyParams.mRuddyLevel = params.mRuddyLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACEV:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFaceVLevel = params.mFaceVLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FACESHORT:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFaceShortLevel = params.mFaceShortLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_CHINSLIME:
                UGCKitRecordConfig.getInstance().mBeautyParams.mChinSlimLevel = params.mChinSlimLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_NOSESCALE:
                UGCKitRecordConfig.getInstance().mBeautyParams.mNoseScaleLevel = params.mNoseScaleLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
                break;
            case BeautyPannel.BEAUTYPARAM_FILTER_MIX_LEVEL:
                UGCKitRecordConfig.getInstance().mBeautyParams.mFilterMixLevel = params.mFilterMixLevel;

                VideoRecordSDK.getInstance().updateBeautyParam(UGCKitRecordConfig.getInstance().mBeautyParams);
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

    /************************************   音乐Pannel回调接口 Begin  ********************************************/
    @Override
    public void onMusicVolumChanged(float volume) {
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setBGMVolume(volume);
        }
    }

    /**
     * 背景音乐裁剪
     *
     * @param startTime
     * @param endTime
     */
    @Override
    public void onMusicTimeChanged(long startTime, long endTime) {
        MusicInfo musicInfo = RecordMusicManager.getInstance().getMusicInfo();
        musicInfo.startTime = startTime;
        musicInfo.endTime = endTime;

        RecordMusicManager.getInstance().startPreviewMusic();
    }

    /**
     * 点击"音乐Pannel"的确定</p>
     * 1、关闭音乐Pannel</p>
     * 2、停止音乐试听
     */
    @Override
    public void onMusicSelect() {
        getRecordBottomLayout().setVisibility(View.VISIBLE);
        getRecordRightLayout().setVisibility(View.VISIBLE);
        // 录制添加BGM后是录制不了人声的，而音效是针对人声有效的
        getRecordRightLayout().setSoundEffectsEnabled(false);

        getRecordMusicPannel().setVisibility(View.GONE);

        // 停止音乐试听
        RecordMusicManager.getInstance().stopPreviewMusic();
    }

    /**
     * 点击"音乐Pannel"的切换音乐
     */
    @Override
    public void onMusicReplace() {
        if (mOnMusicListener != null) {
            mOnMusicListener.onChooseMusic(UGCKitRecordConfig.getInstance().musicInfo.position);
        }
    }

    /**
     * 点击"音乐Pannel"删除背景音乐
     */
    @Override
    public void onMusicDelete() {
        showDeleteMusicDialog();
    }

    private void showDeleteMusicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder.setTitle(getResources().getString(R.string.tips)).setCancelable(false).setMessage(R.string.delete_bgm_or_not)
                .setPositiveButton(R.string.confirm_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();

                        RecordMusicManager.getInstance().deleteMusic();
                        // 录制添加BGM后是录制不了人声的，而音效是针对人声有效的
                        getRecordRightLayout().setSoundEffectIconEnable(true);

//                        getRecordMusicPannel().setMusicName("");
                        getRecordMusicPannel().setVisibility(View.GONE);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    /************************************   音乐Pannel回调接口 End    ********************************************/

    @Override
    public void onSingleClick(float x, float y) {
        getBeautyPannel().setVisibility(View.GONE);
        getRecordMusicPannel().setVisibility(View.GONE);
        getSoundEffectPannel().setVisibility(View.GONE);

        getRecordBottomLayout().setVisibility(View.VISIBLE);
        getRecordRightLayout().setVisibility(View.VISIBLE);
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.setFocusPosition(x, y);
        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        getRecordBottomLayout().updateProgress(milliSecond);

        float second = milliSecond / 1000f;
        boolean enable = second >= UGCKitRecordConfig.getInstance().mMinDuration / 1000;
        getTitleBar().setVisible(enable, ITitleBarLayout.POSITION.RIGHT);
    }

    @Override
    public void onRecordEvent() {
        getRecordBottomLayout().getRecordProgressView().clipComplete();
    }

    @Override
    public void onRecordComplete(@NonNull TXRecordCommon.TXRecordResult result) {
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_VIDEO_RECORD, result.retCode, result.descMsg);

        if (result.retCode >= 0) {
            mProgressDialogUtil.dismissProgressDialog();
            boolean editFlag = JumpActivityMgr.getInstance().getEditFlagFromRecord();
            if (editFlag) {
                // 录制后需要进行编辑，预处理产生视频缩略图
                startPreprocess(result.videoPath);
            } else {
                // 录制后不需要进行编辑视频，直接输出录制视频路径
                if (mOnRecordListener != null) {
                    UGCKitResult ugcKitResult = new UGCKitResult();
                    String outputPath = VideoRecordSDK.getInstance().getRecordVideoPath();
                    ugcKitResult.errorCode = result.retCode;
                    ugcKitResult.descMsg = result.descMsg;
                    ugcKitResult.outputPath = outputPath;
                    ugcKitResult.coverPath = result.coverPath;
                    mOnRecordListener.onRecordCompleted(ugcKitResult);
                }
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

    /**
     * 加载视频信息
     *
     * @param videoPath
     */
    private void loadVideoInfo(String videoPath) {
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
            // 产生录制的缩略图
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
                public void onUIComplete(int retCode, String descMsg) {
                    // 更新UI控件
                    mProgressFragmentUtil.dismissLoadingProgress();
                    if (mOnRecordListener != null) {
                        UGCKitResult ugcKitResult = new UGCKitResult();
                        ugcKitResult.outputPath = VideoRecordSDK.getInstance().getRecordVideoPath();
                        ugcKitResult.errorCode = retCode;
                        ugcKitResult.descMsg = descMsg;
                        mOnRecordListener.onRecordCompleted(ugcKitResult);
                    }
                }

                @Override
                public void onUICancel() {
                    // 更新Activity
                    if (mOnRecordListener != null) {
                        mOnRecordListener.onRecordCanceled();
                    }
                }
            });
            // 开始视频预处理
            ProcessKit.getInstance().startProcess();
        }
    }

    @Override
    public void setConfig(UGCKitRecordConfig config) {
        VideoRecordSDK.getInstance().setConfig(config);
        // 初始化最大/最小视频录制时长
        getRecordBottomLayout().initDuration();
        // 设置默认的录制模式
        getRecordBottomLayout().getRecordButton().setCurrentRecordMode(UGCKitRecordConfig.getInstance().mRecordMode);
        // 设置视频比例UI
        getRecordRightLayout().setAspect(config.mAspectRatio);
    }

    @Override
    public void setEditVideoFlag(boolean enable) {
        JumpActivityMgr.getInstance().setEditFlagFromRecord(enable);
    }

}

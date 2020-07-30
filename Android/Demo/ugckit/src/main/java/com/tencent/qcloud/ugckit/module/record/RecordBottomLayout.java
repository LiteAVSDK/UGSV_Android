package com.tencent.qcloud.ugckit.module.record;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXUGCRecord;

import java.util.Locale;

public class RecordBottomLayout extends RelativeLayout implements View.OnClickListener {
    private Activity            mActivity;
    private ImageView           mImageCameraSwitch;         // 切换摄像头
    private TextView            mTextProgressTime;
    private ImageView           mImageDeleteLastPart;       // 删除上一段
    private ImageView           mImageTorch;                // 闪光灯
    private RecordProgressView  mRecordProgressView;        // 录制进度条
    private RecordSpeedLayout   mRecordSpeedLayout;         // 速度面板
    private RecordButton        mButtonRecord;              // 录制按钮
    private RecordModeView      mRecordModeView;            // 录制模式[单击/长按]
    private View                mRecordModeDot;

    private int     mTorchOnImage;
    private int     mTorchOffImage;
    private int     mTorchDisableImage;
    private boolean mFrontCameraFlag = true;                //是否前置摄像头UI判断
    private boolean mIsTorchOpenFlag;                       // 是否打开闪光灯UI判断
    private boolean isSelectDeleteLastPartFlag;             // 是否点击一次过"删除最有一段分段视频"按钮
    private boolean mDisableTakePhoto;
    private boolean mDisableLongPressRecord;

    private OnDeleteLastPartListener mOnDeleteLastPartListener;

    public RecordBottomLayout(Context context) {
        super(context);
        initViews();
    }

    public RecordBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordBottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.ugckit_record_bottom_layout, this);

        mTextProgressTime = (TextView) findViewById(R.id.record_progress_time);
        mTextProgressTime.setText(0.0f + getResources().getString(R.string.ugckit_unit_second));
        mTextProgressTime.setVisibility(View.INVISIBLE);

        mImageDeleteLastPart = (ImageView) findViewById(R.id.iv_delete_last_part);
        mImageDeleteLastPart.setOnClickListener(this);

        mImageTorch = (ImageView) findViewById(R.id.iv_torch);
        mImageTorch.setOnClickListener(this);

        mImageCameraSwitch = (ImageView) findViewById(R.id.iv_switch_camera);
        mImageCameraSwitch.setOnClickListener(this);

        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);

        mRecordSpeedLayout = (RecordSpeedLayout) findViewById(R.id.record_speed_layout);
        mRecordSpeedLayout.setOnRecordSpeedListener(new RecordSpeedLayout.OnRecordSpeedListener() {
            @Override
            public void onSpeedSelect(int speed) {
                UGCKitRecordConfig.getInstance().mRecordSpeed = speed;
                VideoRecordSDK.getInstance().setRecordSpeed(speed);
            }
        });

        mButtonRecord = (RecordButton) findViewById(R.id.record_button);
        mRecordModeView = (RecordModeView) findViewById(R.id.record_mode_view);
        mRecordModeDot = findViewById(R.id.record_mode_dot);

        // 根据不同的拍摄模式，更新拍摄按钮
        mRecordModeView.setOnRecordModeListener(new RecordModeView.OnRecordModeListener() {
            @Override
            public void onRecordModeSelect(int currentMode) {
                mButtonRecord.setCurrentRecordMode(currentMode);
            }
        });

        mTorchDisableImage = UIAttributeUtil.getResResources(mActivity, R.attr.recordTorchDisableIcon, R.drawable.ugckit_torch_disable);
        mTorchOffImage = UIAttributeUtil.getResResources(mActivity, R.attr.recordTorchOffIcon, R.drawable.ugckit_selector_torch_close);
        mTorchOnImage = UIAttributeUtil.getResResources(mActivity, R.attr.recordTorchOnIcon, R.drawable.ugckit_selector_torch_open);

        if (mFrontCameraFlag) {
            mImageTorch.setVisibility(View.GONE);
            mImageTorch.setImageResource(mTorchDisableImage);
        } else {
            mImageTorch.setVisibility(View.VISIBLE);
            mImageTorch.setImageResource(mTorchOffImage);
        }
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.iv_delete_last_part) {
            deleteLastPart();
        } else if (id == R.id.iv_torch) {
            toggleTorch();
        } else if (id == R.id.iv_switch_camera) {
            switchCamera();
        }
    }


    /**
     * 切换前后摄像头
     */
    private void switchCamera() {
        mFrontCameraFlag = !mFrontCameraFlag;
        mIsTorchOpenFlag = false;
        if (mFrontCameraFlag) {
            mImageTorch.setVisibility(View.GONE);
            mImageTorch.setImageResource(mTorchDisableImage);
        } else {
            mImageTorch.setVisibility(View.VISIBLE);
            mImageTorch.setImageResource(mTorchOffImage);
        }
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.switchCamera(mFrontCameraFlag);
        }
    }

    /**
     * 切换闪光灯开/关
     */
    private void toggleTorch() {
        mIsTorchOpenFlag = !mIsTorchOpenFlag;
        if (mIsTorchOpenFlag) {
            mImageTorch.setImageResource(mTorchOnImage);

            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.toggleTorch(true);
            }
        } else {
            mImageTorch.setImageResource(mTorchOffImage);
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.toggleTorch(false);
            }
        }
    }

    /**
     * 设置闪光灯的状态为关闭
     */
    public void closeTorch() {
        if (mIsTorchOpenFlag) {
            mIsTorchOpenFlag = false;
            if (mFrontCameraFlag) {
                mImageTorch.setVisibility(View.GONE);
                mImageTorch.setImageResource(mTorchDisableImage);
            } else {
                mImageTorch.setImageResource(mTorchOffImage);
                mImageTorch.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 删除上一段
     */
    private void deleteLastPart() {
        int size = VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size();
        if (size == 0) {
            // 没有任何分段视频，返回
            return;
        }
        if (!isSelectDeleteLastPartFlag) {
            isSelectDeleteLastPartFlag = true;
            // 选中最后一段视频，更新进度条颜色
            mRecordProgressView.selectLast();
        } else {
            isSelectDeleteLastPartFlag = false;
            // 删除最后一段视频，更新进度条颜色
            mRecordProgressView.deleteLast();

            VideoRecordSDK.getInstance().deleteLastPart();

            long duration = VideoRecordSDK.getInstance().getPartManager().getDuration();
            float timeSecond = duration / 1000;
            mTextProgressTime.setText(String.format(Locale.CHINA, "%.1f", timeSecond) + getResources().getString(R.string.ugckit_unit_second));

            mOnDeleteLastPartListener.onUpdateTitle(timeSecond >= UGCKitRecordConfig.getInstance().mMinDuration / 1000);

            // 删除分段后再次判断size
            size = VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size();
            if (size == 0) { // 重新开始录
                mOnDeleteLastPartListener.onReRecord();
            }
        }
    }

    public void setOnRecordButtonListener(RecordButton.OnRecordButtonListener listener) {
        mButtonRecord.setOnRecordButtonListener(listener);
    }

    public void setOnDeleteLastPartListener(OnDeleteLastPartListener lister) {
        mOnDeleteLastPartListener = lister;
    }

    public void disableRecordSpeed() {
        mRecordSpeedLayout.setVisibility(View.GONE);
    }

    public void disableTakePhoto() {
        mRecordModeView.disableTakePhoto();
        mDisableTakePhoto = true;
        showRecordMode();
    }

    public void disableLongPressRecord() {
        mRecordModeView.disableLongPressRecord();
        mDisableLongPressRecord = true;
        showRecordMode();
    }

    //如果禁用拍照和长按拍摄，则仅剩下单击拍摄
    private void showRecordMode() {
        mRecordModeDot.setVisibility(View.INVISIBLE);
        mRecordModeView.selectOneRecordMode();
    }

    public interface OnDeleteLastPartListener {
        void onUpdateTitle(boolean enable);

        void onReRecord();
    }

    public void startRecord() {
        mImageDeleteLastPart.setVisibility(View.INVISIBLE);
        mImageCameraSwitch.setVisibility(View.INVISIBLE);
        mImageTorch.setVisibility(View.INVISIBLE);
        mRecordModeView.setVisibility(View.INVISIBLE);
        mRecordSpeedLayout.setVisibility(View.INVISIBLE);
        mTextProgressTime.setVisibility(View.VISIBLE);
    }

    public void pauseRecord() {
        mImageDeleteLastPart.setVisibility(View.VISIBLE);
        mImageCameraSwitch.setVisibility(View.VISIBLE);
        mImageTorch.setVisibility(View.VISIBLE);
        mRecordModeView.setVisibility(View.VISIBLE);
        mRecordSpeedLayout.setVisibility(View.VISIBLE);
        mTextProgressTime.setVisibility(View.INVISIBLE);
    }

    /**
     * 初始化最大/最小视频录制时长
     */
    public void initDuration() {
        mRecordProgressView.setMaxDuration(UGCKitRecordConfig.getInstance().mMaxDuration);
        mRecordProgressView.setMinDuration(UGCKitRecordConfig.getInstance().mMinDuration);
    }

    /**
     * 更新录制进度Progress
     *
     * @param milliSecond
     */
    public void updateProgress(long milliSecond) {
        mRecordProgressView.setProgress((int) milliSecond);
        float second = milliSecond / 1000f;
        mTextProgressTime.setText(String.format(Locale.CHINA, "%.1f", second) + getResources().getString(R.string.ugckit_unit_second));
    }

    public RecordButton getRecordButton() {
        return mButtonRecord;
    }

    public RecordModeView getRecordModeView() {
        return mRecordModeView;
    }

    public RecordProgressView getRecordProgressView() {
        return mRecordProgressView;
    }

    public RecordSpeedLayout getRecordSpeedLayout() {
        return mRecordSpeedLayout;
    }
}

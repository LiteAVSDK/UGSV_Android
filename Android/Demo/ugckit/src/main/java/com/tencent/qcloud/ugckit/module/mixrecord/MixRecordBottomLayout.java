package com.tencent.qcloud.ugckit.module.mixrecord;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.module.record.RecordButton;
import com.tencent.qcloud.ugckit.module.record.RecordModeView;
import com.tencent.qcloud.ugckit.module.record.RecordProgressView;
import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.qcloud.ugckit.module.record.VideoRecordSDK;
import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXUGCRecord;

import java.util.Locale;

public class MixRecordBottomLayout extends RelativeLayout implements View.OnClickListener {
    private Activity mActivity;

    private ImageView mIvCameraSwitch;
    private TextView mTvProgressTime;
    private ImageView mIvDeleteLastPart;
    private ImageView mIvTorch;
    private RecordProgressView mRecordProgressView;
    private RecordButton mRecordButton;
    private RecordModeView mRecordModeView;
    private View mRecordModeDot;

    private RecordButton.OnRecordButtonListener mOnRecordButtonListener;
    /**
     * 是否前置摄像头UI判断
     */
    private boolean mFrontCameraFlag = true;
    /**
     * 是否打开闪光灯UI判断
     */
    private boolean mIsTorchOpenFlag;
    /**
     * 是否点击一次过"删除最有一段分段视频"按钮
     */
    private boolean isSelectDeleteLastPartFlag;

    public MixRecordBottomLayout(Context context) {
        super(context);
        initViews();
    }

    public MixRecordBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public MixRecordBottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.chorus_bottom_layout, this);

        mTvProgressTime = (TextView) findViewById(R.id.record_progress_time);
        mTvProgressTime.setText(0.0f + getResources().getString(R.string.unit_second));

        mIvDeleteLastPart = (ImageView) findViewById(R.id.iv_delete_last_part);
        mIvDeleteLastPart.setOnClickListener(this);

        mIvTorch = (ImageView) findViewById(R.id.iv_torch);
        mIvTorch.setOnClickListener(this);

        mIvCameraSwitch = (ImageView) findViewById(R.id.iv_switch_camera);
        mIvCameraSwitch.setOnClickListener(this);

        if (mFrontCameraFlag) {
            mIvTorch.setVisibility(View.GONE);
            mIvTorch.setImageResource(R.drawable.ugc_torch_disable);
        } else {
            mIvTorch.setImageResource(R.drawable.selector_torch_close);
            mIvTorch.setVisibility(View.VISIBLE);
        }

        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);

        mRecordButton = (RecordButton) findViewById(R.id.record_button);
        mRecordModeView = (RecordModeView) findViewById(R.id.record_mode_view);
        mRecordModeDot = findViewById(R.id.record_mode_dot);

        // 合唱模式：禁用拍照，仅支持"单击拍"和"按住拍"
        mRecordModeView.disableTakePhoto();
        // 根据不同的拍摄模式，更新拍摄按钮
        mRecordModeView.setOnRecordModeListener(new RecordModeView.OnRecordModeListener() {
            @Override
            public void onRecordModeSelect(int currentMode) {
                mRecordButton.setCurrentRecordMode(currentMode);
            }
        });
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
            mIvTorch.setVisibility(View.GONE);
            mIvTorch.setImageResource(R.drawable.ugc_torch_disable);
        } else {
            mIvTorch.setImageResource(R.drawable.selector_torch_close);
            mIvTorch.setVisibility(View.VISIBLE);
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
        if (mIsTorchOpenFlag) {
            mIvTorch.setImageResource(R.drawable.selector_torch_close);
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.toggleTorch(false);
            }
        } else {
            mIvTorch.setImageResource(R.drawable.selector_torch_open);
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.toggleTorch(true);
            }
        }
        mIsTorchOpenFlag = !mIsTorchOpenFlag;
    }

    /**
     * 设置闪光灯的状态为关闭
     */
    public void closeTorch() {
        if (mIsTorchOpenFlag) {
            mIsTorchOpenFlag = false;
            if (mFrontCameraFlag) {
                mIvTorch.setVisibility(View.GONE);
                mIvTorch.setImageResource(R.drawable.ugc_torch_disable);
            } else {
                mIvTorch.setImageResource(R.drawable.selector_torch_close);
                mIvTorch.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 删除上一段
     */
    private void deleteLastPart() {
        int size = VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size();

        if (size == 0) {// 没有任何分段视频，返回
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
            mTvProgressTime.setText(String.format(Locale.CHINA, "%.1f", timeSecond) + getResources().getString(R.string.unit_second));
            if (mOnRecordButtonListener != null) {
                mOnRecordButtonListener.onDeleteParts(VideoRecordSDK.getInstance().getPartManager().getPartsPathList().size(), duration);
            }
        }
    }

    public void setOnRecordButtonListener(RecordButton.OnRecordButtonListener listener) {
        mRecordButton.setOnRecordButtonListener(listener);
        mOnRecordButtonListener = listener;
    }

    public void disableFunction() {
        mIvDeleteLastPart.setVisibility(View.INVISIBLE);
        mIvCameraSwitch.setVisibility(View.INVISIBLE);
        mIvTorch.setVisibility(View.INVISIBLE);
        mRecordModeView.setVisibility(View.INVISIBLE);
        mRecordModeDot.setVisibility(View.INVISIBLE);
    }

    public void enableFunction() {
        mIvDeleteLastPart.setVisibility(View.VISIBLE);
        mIvCameraSwitch.setVisibility(View.VISIBLE);
        mIvTorch.setVisibility(View.VISIBLE);
        mRecordModeView.setVisibility(View.VISIBLE);
        mRecordModeDot.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化最大/最小视频录制时长
     */
    public void setDuration(int minDuration, int maxDuration) {
        mRecordProgressView.setMaxDuration(maxDuration);
        mRecordProgressView.setMinDuration(minDuration);
    }

    /**
     * 更新录制进度Progress
     *
     * @param milliSecond
     */
    public void updateProgress(long milliSecond) {
        mRecordProgressView.setProgress((int) milliSecond);
        float second = milliSecond / 1000f;
        mTvProgressTime.setText(String.format(Locale.CHINA, "%.1f", second) + getResources().getString(R.string.unit_second));
    }

    public RecordProgressView getRecordProgressView() {
        return mRecordProgressView;
    }

    public RecordButton getRecordButton() {
        return mRecordButton;
    }

    public RecordModeView getRecordModeView() {
        return mRecordModeView;
    }
}

package com.tencent.qcloud.ugckit.module.cut;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.R;

import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.component.slider.VideoCutView;
import com.tencent.qcloud.ugckit.module.effect.utils.Edit;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXVideoEditConstants;

public class VideoCutLayout extends RelativeLayout implements IVideoCutLayout, View.OnClickListener, Edit.OnCutChangeListener {
    private static final String TAG = "VideoCutLayout";
    private FragmentActivity mActivity;
    private ImageView mIvRotate;
    private TextView mTvDuration;
    private VideoCutView mVideoCutView;

    private int mRotation;
    private OnRotateVideoListener mOnRotateVideoListener;

    public VideoCutLayout(Context context) {
        super(context);
        initViews();
    }

    public VideoCutLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public VideoCutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (FragmentActivity) getContext();
        inflate(mActivity, R.layout.video_cut_kit, this);

        mTvDuration = (TextView) findViewById(R.id.tv_choose_duration);
        mIvRotate = (ImageView) findViewById(R.id.iv_rotate);
        mVideoCutView = (VideoCutView) findViewById(R.id.video_edit_view);

        mIvRotate.setOnClickListener(this);
        mVideoCutView.setCutChangeListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        if (view.getId() == R.id.iv_rotate) {
            mRotation += 90;

            if (mOnRotateVideoListener != null) {
                mOnRotateVideoListener.onRotate(mRotation);
            }
        }
    }

    @Override
    public void onCutClick() {

    }

    @Override
    public void onCutChangeKeyDown() {
        PlayerManagerKit.getInstance().stopPlay();
    }

    @Override
    public void onCutChangeKeyUp(long startTime, long endTime, int type) {
        long duration = (endTime - startTime) / 1000;

        String str = getResources().getString(R.string.tc_video_cutter_activity_load_video_success_already_picked) + duration + "s";
        mTvDuration.setText(str);

        PlayerManagerKit.getInstance().startPlay();
        VideoEditerSDK.getInstance().setCutterStartTime(startTime, endTime);

        TXLog.d(TAG, "startTime:" + startTime + ",endTime:" + endTime + ",duration:" + duration);
    }

    @Override
    public void setVideoInfo(@NonNull TXVideoEditConstants.TXVideoInfo videoInfo) {
        mRotation = 0;

        int durationS = (int) (videoInfo.duration / 1000);
        int thumbCount = durationS / 3;

        if (durationS >= MAX_DURATION) {
            durationS = MAX_DURATION;
        }
        mTvDuration.setText(getResources().getString(R.string.tc_video_cutter_activity_load_video_success_already_picked) + durationS + "s");

        TXCLog.i(TAG, "[UGCKit][VideoCut]init cut time, start:" + 0 + ", end:" + durationS * 1000);
        VideoEditerSDK.getInstance().setCutterStartTime(0, durationS * 1000);

        mVideoCutView.setMediaFileInfo(videoInfo);
        mVideoCutView.setCount(thumbCount);
    }

    @Override
    public void addThumbnail(int index, Bitmap bitmap) {
        mVideoCutView.addBitmap(index, bitmap);
    }

    public void clearThumbnail() {
        mVideoCutView.clearAllBitmap();
    }

    public VideoCutView getVideoCutView() {
        return mVideoCutView;
    }

    @Override
    public void setOnRotateVideoListener(OnRotateVideoListener listener) {
        mOnRotateVideoListener = listener;
    }

}

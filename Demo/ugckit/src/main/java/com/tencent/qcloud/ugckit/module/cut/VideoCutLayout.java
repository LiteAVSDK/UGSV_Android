package com.tencent.qcloud.ugckit.module.cut;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.R;

import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.component.slider.VideoCutView;
import com.tencent.qcloud.ugckit.module.effect.utils.Edit;
import com.tencent.ugc.TXVideoEditConstants;

public class VideoCutLayout extends RelativeLayout implements IVideoCutLayout, View.OnClickListener, Edit.OnCutChangeListener {
    private static final String TAG = "VideoCutLayout";

    private FragmentActivity mActivity;
    private ImageView        mImageRotate;
    private TextView         mTextDuration;
    private VideoCutView     mVideoCutView;
    private int              mRotation;

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
        inflate(mActivity, R.layout.ugckit_video_cut_kit, this);

        mTextDuration = (TextView) findViewById(R.id.tv_choose_duration);
        mImageRotate = (ImageView) findViewById(R.id.iv_rotate);
        mVideoCutView = (VideoCutView) findViewById(R.id.video_edit_view);

        mImageRotate.setOnClickListener(this);
        mVideoCutView.setCutChangeListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        if (view.getId() == R.id.iv_rotate) {
            // 当旋转角度大于等于270度的时候，下一次就是0度；
            mRotation = mRotation < 270 ? mRotation + 90 : 0;

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

        String str = getResources().getString(R.string.ugckit_video_cutter_activity_load_video_success_already_picked) + duration + "s";
        mTextDuration.setText(str);

        VideoEditerSDK.getInstance().setCutterStartTime(startTime, endTime);
        PlayerManagerKit.getInstance().startPlay();

        Log.d(TAG, "startTime:" + startTime + ",endTime:" + endTime + ",duration:" + duration);
    }

    @Override
    public void setVideoInfo(@NonNull TXVideoEditConstants.TXVideoInfo videoInfo) {
        mRotation = 0;

        int durationS = (int) (videoInfo.duration / 1000);
        int thumbCount = durationS / 3;

        int selectDuration = durationS;
        if (selectDuration >= MAX_DURATION) {
            selectDuration = MAX_DURATION;
        }
        mTextDuration.setText(getResources().getString(R.string.ugckit_video_cutter_activity_load_video_success_already_picked) + selectDuration + "s");

        long cutTimeMs = videoInfo.duration;
        if (cutTimeMs > MAX_DURATION * 1000) {
            cutTimeMs = MAX_DURATION * 1000;
        }

        Log.i(TAG, "[UGCKit][VideoCut]init cut time, start:" + 0 + ", end:" + cutTimeMs);
        VideoEditerSDK.getInstance().setCutterStartTime(0, cutTimeMs);
        VideoEditerSDK.getInstance().setVideoDuration(videoInfo.duration);

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

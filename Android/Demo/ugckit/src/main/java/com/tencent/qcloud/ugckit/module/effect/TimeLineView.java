package com.tencent.qcloud.ugckit.module.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressController;
import com.tencent.qcloud.ugckit.component.timeline.VideoProgressView;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;

import java.util.List;

/**
 * 编辑控件：图片时间轴
 */
public class TimeLineView extends RelativeLayout implements ITimeLineView, VideoProgressController.VideoProgressSeekListener, PlayerManagerKit.OnPreviewListener {
    private static final String TAG = "TimeLineView";
    private FragmentActivity mActivity;
    private ImageView mIvSlider;
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;

    public TimeLineView(Context context) {
        super(context);
        initViews();
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (FragmentActivity) getContext();
        inflate(getContext(), R.layout.video_timeline, this);
        mIvSlider = (ImageView) findViewById(R.id.iv_player_slider);
        mVideoProgressView = (VideoProgressView) findViewById(R.id.video_progress_view);

        PlayerManagerKit.getInstance().addOnPreviewLitener(this);
    }

    /**
     * 初始化进度布局
     */
    @Override
    public void initVideoProgressLayout() {
        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;

        List<Bitmap> thumbnailList = VideoEditerSDK.getInstance().getAllThumbnails();
        mVideoProgressView.setViewWidth(screenWidth);
        mVideoProgressView.setThumbnailData(thumbnailList);

        // TODO:设置裁剪时长
        long duration = VideoEditerSDK.getInstance().getVideoDuration();
        mVideoProgressController = new VideoProgressController(duration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        mVideoProgressController.setVideoProgressSeekListener(this);
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);
    }

    @Override
    public void onVideoProgressSeek(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    @Override
    public void onVideoProgressSeekFinish(long currentTimeMs) {
        PlayerManagerKit.getInstance().previewAtTime(currentTimeMs);
    }

    public VideoProgressController getVideoProgressController() {
        return mVideoProgressController;
    }

    public VideoProgressView getVideoProgressView() {
        return mVideoProgressView;
    }

    @Override
    public void onPreviewProgress(int timeMs) {
        int currentState = PlayerManagerKit.getInstance().getCurrentState();
        if (currentState == PlayState.STATE_PLAY || currentState == PlayState.STATE_RESUME) {
            mVideoProgressController.setCurrentTimeMs(timeMs);
        }
    }

    @Override
    public void onPreviewFinish() {

    }

    @Override
    public void updateUIByFragment(int type) {
        if (type == UGCKitConstants.TYPE_EDITER_BGM) {
            mVideoProgressView.setVisibility(View.GONE);
        } else {
            mVideoProgressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setCurrentProgessIconResource(int resid) {
        mIvSlider.setImageResource(resid);
    }

}

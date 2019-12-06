package com.tencent.qcloud.ugckit.module.followRecord;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.qcloud.ugckit.utils.ScreenUtils;
import com.tencent.qcloud.ugckit.R;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

/**
 * 合唱录制和播放界面</p>
 * 左侧：录制界面</p>
 * 右侧：跟拍播放界面</p>
 */
public class FollowRecordPlayView extends LinearLayout {
    private TXCloudVideoView mChorusRecordView;
    private FrameLayout mChorusPlayView;
    private TXVideoEditer mVideoEditer;
    private int mCurrentState;

    public FollowRecordPlayView(Context context) {
        super(context);
        initViews();
    }

    public FollowRecordPlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FollowRecordPlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.chorus_play_layout, this);

        mChorusRecordView = (TXCloudVideoView) findViewById(R.id.chorus_record_view);
        mChorusPlayView = (FrameLayout) findViewById(R.id.chorus_play_view);

        int widthPixels = ScreenUtils.getScreenWidth(getContext());
        int heightPixels = ScreenUtils.getScreenHeight(getContext());

        int viewWidth = widthPixels / 2;
        int viewHeight = heightPixels;
        int paddingTop = (heightPixels - widthPixels * 8 / 9) / 2;
        int paddingBottom = paddingTop;

        mChorusPlayView.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
        mChorusPlayView.setPadding(0, paddingTop, 0, paddingBottom);

        mChorusRecordView.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
        mChorusRecordView.setPadding(0, paddingTop, 0, paddingBottom);

        mCurrentState = PlayState.STATE_STOP;
    }

    /**
     * 初始化播放器，必须先设置合唱信息 {@link IVideoFollowRecordKit#setFollowRecordInfo(FollowRecordInfo)}
     */
    public void initPlayerView() {
        mVideoEditer = new TXVideoEditer(getContext());
        mVideoEditer.setVideoPath(FollowRecordConfig.getInstance().videoInfo.playPath);

        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mChorusPlayView;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_SCREEN;
        mVideoEditer.initWithPreview(param);
    }

    public void startPlayChorusVideo() {
        // FIXBUG: 合唱界面一进入，即播放跟拍视频，点击开始录制后，需要重头播放跟拍视频
        if (mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PLAY) {
            mVideoEditer.stopPlay();
            mVideoEditer.startPlayFromTime(0, FollowRecordConfig.getInstance().videoInfo.duration);
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            mVideoEditer.resumePlay();
        }
        mCurrentState = PlayState.STATE_PLAY;
    }

    public void pausePlayChorusVideo() {
        mVideoEditer.pausePlay();
        mCurrentState = PlayState.STATE_PAUSE;
    }

    public void stopPlayChorusVideo() {
        mVideoEditer.stopPlay();
        mCurrentState = PlayState.STATE_STOP;
    }

    public void release() {
        mVideoEditer.release();
    }

    public TXCloudVideoView getChorusRecordView() {
        return mChorusRecordView;
    }

    public FrameLayout getChorusPlayView() {
        return mChorusPlayView;
    }
}

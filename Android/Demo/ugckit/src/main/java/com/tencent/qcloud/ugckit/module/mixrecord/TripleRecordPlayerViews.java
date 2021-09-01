package com.tencent.qcloud.ugckit.module.mixrecord;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditConstants;

import java.util.ArrayList;
import java.util.List;

public class TripleRecordPlayerViews extends LinearLayout implements IPlayerView {
    private MixRecordPlayerView mTopView;
    private MixRecordPlayerView mMiddleView;
    private MixRecordPlayerView mBottomView;
    private float               mContinuePosition = -1;

    public TripleRecordPlayerViews(Context context) {
        super(context);
        initViews();
    }

    public TripleRecordPlayerViews(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TripleRecordPlayerViews(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_triple_record_player_view_inner, this);
        mTopView = (MixRecordPlayerView) findViewById(R.id.triple_first);
        mMiddleView = (MixRecordPlayerView) findViewById(R.id.triple_second);
        mBottomView = (MixRecordPlayerView) findViewById(R.id.triple_third);
    }

    @Override
    public boolean init(int index, String videoPath) {
        if (index == 0) {
            mTopView.init(0, videoPath);
        } else if (index == 1) {
            mMiddleView.init(1, null);
        } else {
            mBottomView.init(2, videoPath);
        }
        return true;
    }

    @Override
    public boolean startVideo() {
        mTopView.startVideo();
        mBottomView.startVideo();
        return false;
    }

    @Override
    public void stopVideo() {
        mTopView.stopVideo();
        mBottomView.stopVideo();
        //停止播放后，使用顶部视频播放保存的播放位置做为合唱视频记录的基准，保证下次播放顶部和底部的视频同步播放
        mContinuePosition = mTopView.getContinuePosition();
        mTopView.setContinuePosition(mContinuePosition);
        mBottomView.setContinuePosition(mContinuePosition);
    }

    @Override
    public void pauseVideo() {
        mTopView.pauseVideo();
        mBottomView.pauseVideo();
    }

    @Override
    public void seekVideo(long position) {
        mTopView.seekVideo(position);
        mBottomView.seekVideo(position);
    }

    @Override
    public void releaseVideo() {
        mTopView.releaseVideo();
        mBottomView.releaseVideo();
    }

    @Override
    public TXCloudVideoView getVideoView() {
        return mMiddleView.getVideoView();
    }

    @Override
    public void updateFile(int index, String videoPath) {
        mTopView.updateFile(index, videoPath);
        mBottomView.updateFile(index, videoPath);
    }

    @Override
    public List<TXVideoEditConstants.TXAbsoluteRect> getCombineRects(MixRecordConfig config) {
        List<TXVideoEditConstants.TXAbsoluteRect> rects = new ArrayList<>();
        TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
        int h = config.getHeight() / 3;
        rect1.x = 0;
        rect1.y = 0;
        rect1.width = config.getWidth();
        rect1.height = h;

        TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
        rect2.x = 0;
        rect2.y = h;
        rect2.width = config.getWidth();
        rect2.height = h;

        TXVideoEditConstants.TXAbsoluteRect rect3 = new TXVideoEditConstants.TXAbsoluteRect();
        rect3.x = 0;
        rect3.y = h * 2;
        rect3.width = config.getWidth();
        rect3.height = h;

        rects.add(rect1);
        rects.add(rect2);
        rects.add(rect3);

        config.setVolume(0, 0.0f);
        config.setVolume(1, 0.0f);
        return rects;
    }

    @Override
    public float getContinuePosition() {
        return mContinuePosition;
    }

    @Override
    public void setContinuePosition(float position) {
        mContinuePosition = position;
    }
}

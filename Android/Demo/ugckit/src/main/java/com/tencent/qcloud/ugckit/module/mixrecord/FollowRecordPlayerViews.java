package com.tencent.qcloud.ugckit.module.mixrecord;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.utils.ScreenUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditConstants;

import java.util.ArrayList;
import java.util.List;

public class FollowRecordPlayerViews extends LinearLayout implements IPlayerView {
    private MixRecordPlayerView mLeftView;
    private MixRecordPlayerView mRightView;

    public FollowRecordPlayerViews(Context context) {
        super(context);
        initViews();
    }

    public FollowRecordPlayerViews(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FollowRecordPlayerViews(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.follow_record_player_view_inner, this);
        mLeftView = (MixRecordPlayerView) findViewById(R.id.left);
        mRightView = (MixRecordPlayerView) findViewById(R.id.right);
        int widthPixels = ScreenUtils.getScreenWidth(getContext());
        int heightPixels = ScreenUtils.getScreenHeight(getContext());

        int viewWidth = widthPixels / 2;
        int viewHeight = heightPixels;
        int paddingTop = (heightPixels - widthPixels * 8 / 9) / 2;
        int paddingBottom = paddingTop;
        mLeftView.setPadding(0, paddingTop, 0, paddingBottom);
        mRightView.setPadding(0, paddingTop, 0, paddingBottom);
    }

    @Override
    public boolean init(int index, String videoPath) {
        if (index == 0) {
            mLeftView.init(0, null);
        } else {
            mRightView.init(1, videoPath);
            mRightView.setMute(false);
        }
        return true;
    }

    @Override
    public boolean startVideo() {
        boolean rst = mRightView.startVideo();
        mRightView.setMute(false);
        return rst;
    }

    @Override
    public void stopVideo() {
        mRightView.stopVideo();
    }

    @Override
    public void pauseVideo() {
        mRightView.pauseVideo();
    }

    @Override
    public void seekVideo(long position) {
        mRightView.seekVideo(position);
    }

    @Override
    public void releaseVideo() {
        mRightView.releaseVideo();
    }

    @Override
    public TXCloudVideoView getVideoView() {
        return mLeftView.getVideoView();
    }

    @Override
    public void updateFile(int index, String videoPath) {
        mRightView.updateFile(index, videoPath);
        mRightView.setMute(false);
    }

    @Override
    public List<TXVideoEditConstants.TXAbsoluteRect> getCombineRects(MixRecordConfig config) {
        List<TXVideoEditConstants.TXAbsoluteRect> rects = new ArrayList<>();
        TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
        int w = config.getWidth() / 2;
        rect1.x = 0;
        rect1.y = 0;
        rect1.width = w;
        rect1.height = config.getHeight();

        TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
        rect2.x = rect1.width;
        rect2.y = 0;
        rect2.width = w;
        rect2.height = config.getHeight();

        rects.add(rect1);
        rects.add(rect2);
        return rects;
    }
}

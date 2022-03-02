package com.tencent.qcloud.ugckit.module.cut;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;


public abstract class AbsVideoCutUI extends RelativeLayout implements IVideoCutKit {

    private TitleBarLayout  mTitleBar;
    private VideoPlayLayout mVideoPlayLayout;
    private VideoCutLayout  mVideoCutLayout;

    public AbsVideoCutUI(Context context) {
        super(context);
        initViews();
    }

    public AbsVideoCutUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsVideoCutUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_video_cut_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mVideoPlayLayout = (VideoPlayLayout) findViewById(R.id.video_play_layout);
        mVideoCutLayout = (VideoCutLayout) findViewById(R.id.video_cut_layout);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    public VideoPlayLayout getVideoPlayLayout() {
        return mVideoPlayLayout;
    }

    /**
     * 获取裁剪工具栏
     */
    public VideoCutLayout getVideoCutLayout() {
        return mVideoCutLayout;
    }
}

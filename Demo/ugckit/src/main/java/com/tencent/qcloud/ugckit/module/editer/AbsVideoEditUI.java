package com.tencent.qcloud.ugckit.module.editer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.module.cut.VideoPlayLayout;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;

public abstract class AbsVideoEditUI extends RelativeLayout implements IVideoEditKit {

    private TitleBarLayout  mLayoutTitleBar;
    private VideoPlayLayout mLayoutVideoPlay;

    public AbsVideoEditUI(Context context) {
        super(context);
        initViews();
    }

    public AbsVideoEditUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsVideoEditUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_video_edit_layout, this);

        mLayoutTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mLayoutVideoPlay = (VideoPlayLayout) findViewById(R.id.video_play_layout);
    }

    public TitleBarLayout getTitleBar() {
        return mLayoutTitleBar;
    }

    /**
     * 获取视频播放界面
     */
    public VideoPlayLayout getVideoPlayLayout() {
        return mLayoutVideoPlay;
    }

}

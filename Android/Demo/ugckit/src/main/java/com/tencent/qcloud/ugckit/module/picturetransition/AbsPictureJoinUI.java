package com.tencent.qcloud.ugckit.module.picturetransition;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.module.cut.VideoPlayLayout;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;

public abstract class AbsPictureJoinUI extends RelativeLayout implements IPictureJoinKit {
    private TitleBarLayout          mTitleBar;
    private VideoPlayLayout         mVideoPlayLayout;
    private PictureTransitionLayout mPictureTransitionLayout;

    public AbsPictureJoinUI(Context context) {
        super(context);
        initViews();
    }

    public AbsPictureJoinUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsPictureJoinUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_pic_join_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mVideoPlayLayout = (VideoPlayLayout) findViewById(R.id.picture_play_layout);
        mPictureTransitionLayout = (PictureTransitionLayout) findViewById(R.id.picture_transition_layout);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    /**
     * 获取图片播放界面
     */
    public VideoPlayLayout getVideoPlayLayout() {
        return mVideoPlayLayout;
    }

    /**
     * 获取转场设置工具栏
     */
    @Override
    public PictureTransitionLayout getPictureTransitionLayout() {
        return mPictureTransitionLayout;
    }

}

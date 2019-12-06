package com.tencent.qcloud.ugckit.module.followRecord;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.module.record.ScrollFilterView;
import com.tencent.qcloud.ugckit.module.record.beauty.BeautyPannel;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;

public abstract class AbsVideoFollowRecordUI extends RelativeLayout implements IVideoFollowRecordKit {

    private TitleBarLayout mTitleBar;
    private FollowRecordPlayView mFollowRecordPlayView;
    private ScrollFilterView mScrollFilterView;

    private FollowRecordRightLayout mFollowRecordRightLayout;
    private FollowRecordBottomLayout mFollowRecordBottomLayout;

    private BeautyPannel mBeautyPannel;
    private CountDownTimerView mCountDownTimerView;

    public AbsVideoFollowRecordUI(Context context) {
        super(context);
        initViews();
    }

    public AbsVideoFollowRecordUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsVideoFollowRecordUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.video_chor_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mFollowRecordPlayView = (FollowRecordPlayView) findViewById(R.id.chorus_view);
        mCountDownTimerView = (CountDownTimerView) findViewById(R.id.countdown_timer_view);

        mFollowRecordRightLayout = (FollowRecordRightLayout) findViewById(R.id.record_right_layout);
        mFollowRecordBottomLayout = (FollowRecordBottomLayout) findViewById(R.id.record_bottom_layout);

        mBeautyPannel = (BeautyPannel) findViewById(R.id.beauty_pannel);
        mScrollFilterView = (ScrollFilterView) findViewById(R.id.scrollFilterView);
        mScrollFilterView.setBeautyPannel(mBeautyPannel);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    /**
     * 获取合唱预览界面【左侧为录制的视频，右侧为跟拍的视频】
     */
    public FollowRecordPlayView getChorusPlayView() {
        return mFollowRecordPlayView;
    }

    public ScrollFilterView getScrollFilterView() {
        return mScrollFilterView;
    }

    /**
     * 获取录制底部工具栏
     */
    public FollowRecordBottomLayout getFollowRecordBottomLayout() {
        return mFollowRecordBottomLayout;
    }

    /**
     * 获取合唱右侧工具栏
     */
    public FollowRecordRightLayout getFollowRecordRightLayout() {
        return mFollowRecordRightLayout;
    }

    /**
     * 获取"美颜"面板
     */
    public BeautyPannel getBeautyPannel() {
        return mBeautyPannel;
    }

    public CountDownTimerView getCountDownTimerView() {
        return mCountDownTimerView;
    }

    @Override
    public void disableCountDownTimer() {
        mFollowRecordRightLayout.disableCountDownTimer();
        mCountDownTimerView.setVisibility(View.GONE);
    }

    @Override
    public void disableBeauty() {
        mFollowRecordRightLayout.disableBeauty();
    }

}

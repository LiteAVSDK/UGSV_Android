package com.tencent.qcloud.ugckit.module.mixrecord;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.basic.ITitleBarLayout;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;
import com.tencent.qcloud.ugckit.module.record.ScrollFilterView;
import com.tencent.liteav.demo.beauty.BeautyPanel;

public abstract class AbsVideoTripleMixRecordUI extends RelativeLayout implements IVideoMixRecordKit {
    private TitleBarLayout mTitleBar;
    private IPlayerView mPlayerViews;
    private ScrollFilterView mScrollFilterView;

    private MixRecordRightLayout mFollowRecordRightLayout;
    private MixRecordBottomLayout mMixRecordBottomLayout;

    private BeautyPanel mBeautyPannel;
    private CountDownTimerView mCountDownTimerView;

    public AbsVideoTripleMixRecordUI(Context context) {
        this(context,null);
    }

    public AbsVideoTripleMixRecordUI(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AbsVideoTripleMixRecordUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(attrs,defStyleAttr);
    }

    private void initViews(AttributeSet attrs, int defStyleAttr) {
        inflate(getContext(), R.layout.mix_record_view, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mCountDownTimerView = (CountDownTimerView) findViewById(R.id.countdown_timer_view);
        mFollowRecordRightLayout = (MixRecordRightLayout) findViewById(R.id.record_right_layout);
        mMixRecordBottomLayout = (MixRecordBottomLayout) findViewById(R.id.record_bottom_layout);
        mBeautyPannel = (BeautyPanel) findViewById(R.id.beauty_pannel);
        mScrollFilterView = (ScrollFilterView) findViewById(R.id.scrollFilterView);
        mScrollFilterView.setBeautyPannel(mBeautyPannel);

        mTitleBar.setVisible(true, ITitleBarLayout.POSITION.RIGHT);
        mTitleBar.setTitle(getResources().getString(R.string.triple_replace_file), ITitleBarLayout.POSITION.RIGHT);//

        View playerviewHolder = (View) findViewById(R.id.mixrecord_playerview_placeholder);
        int player_layout_id = R.layout.mix_record_view;
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MixRecord, 0, 0);
            try {
                player_layout_id = a.getResourceId(R.styleable.MixRecord_layout_id, player_layout_id);
            }
            finally {
                a.recycle();
            }
        }
        View playerViews = LayoutInflater.from(getContext()).inflate(player_layout_id, this,false);
        playerViews.setId(R.id.mixrecord_playerviews);
        playerViews.setLayoutParams(playerviewHolder.getLayoutParams());
        ViewGroup parent = ((ViewGroup) playerviewHolder.getParent());
        int controllerIndex = parent.indexOfChild(playerviewHolder);
        parent.removeView(playerviewHolder);
        parent.addView(playerViews, controllerIndex);
        mPlayerViews = (IPlayerView) playerViews;
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    public IPlayerView getPlayViews() {
        return mPlayerViews;
    }

    public ScrollFilterView getScrollFilterView() {
        return mScrollFilterView;
    }

    /**
     * 获取录制底部工具栏
     */
    public MixRecordBottomLayout getFollowRecordBottomLayout() {
        return mMixRecordBottomLayout;
    }

    /**
     * 获取合唱右侧工具栏
     */
    public MixRecordRightLayout getFollowRecordRightLayout() {
        return mFollowRecordRightLayout;
    }

    /**
     * 获取"美颜"面板
     */
    public BeautyPanel getBeautyPanel() {
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

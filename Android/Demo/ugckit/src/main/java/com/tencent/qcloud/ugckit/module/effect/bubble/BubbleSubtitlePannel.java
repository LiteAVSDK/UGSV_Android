package com.tencent.qcloud.ugckit.module.effect.bubble;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.circlebmp.TCCircleView;
import com.tencent.qcloud.ugckit.component.seekbar.TCColorView;


import java.util.List;

/**
 * 配置气泡字幕样式、以及字体颜色的控件
 */
public class BubbleSubtitlePannel extends FrameLayout implements IBubbleSubtitlePannel, BubbleAdapter.OnItemClickListener, View.OnClickListener, TCColorView.OnSelectColorListener {
    private View mContentView;
    private RecyclerView mRvBubbles;
    private BubbleAdapter mBubbleAdapter;
    private List<TCBubbleInfo> mBubbles;
    private ImageView mIvClose;
    /**
     * 气泡样式
     */
    private TextView mTvBubbleStyle;
    /**
     * 文字颜色
     */
    private TextView mTvTextColor;

    private TCColorView mColorView;
    private TCCircleView mCvColor;
    private LinearLayout mLlColor;

    @Nullable
    private TCSubtitleInfo mSubtitleInfo;
    private OnBubbleSubtitleCallback mCallback;

    public BubbleSubtitlePannel(@NonNull Context context) {
        super(context);
        init();
    }

    public BubbleSubtitlePannel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleSubtitlePannel(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mSubtitleInfo = new TCSubtitleInfo();

        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bubble_window, this, true);
        initViews(mContentView);
    }

    private void initViews(@NonNull View contentView) {
        mIvClose = (ImageView)contentView.findViewById(R.id.iv_close);
        mIvClose.setOnClickListener(this);
        mRvBubbles = (RecyclerView) contentView.findViewById(R.id.bubble_rv_style);
        mTvBubbleStyle = (TextView) contentView.findViewById(R.id.bubble_iv_bubble);
        mTvBubbleStyle.setOnClickListener(this);
        mTvTextColor = (TextView) contentView.findViewById(R.id.bubble_iv_color);
        mTvTextColor.setOnClickListener(this);
        mLlColor = (LinearLayout) contentView.findViewById(R.id.bubble_ll_color);
        mCvColor = (TCCircleView) contentView.findViewById(R.id.bubble_cv_color);
        mColorView = (TCColorView) contentView.findViewById(R.id.bubble_color_view);
        mColorView.setOnSelectColorListener(this);
        mTvBubbleStyle.setSelected(true);
        mRvBubbles.setVisibility(View.VISIBLE);
    }

    private void enterAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mContentView, "translationY", mContentView.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                BubbleSubtitlePannel.this.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void resetInfo() {
        mSubtitleInfo.setBubblePos(0);
        // 创建一个默认的
        TCBubbleInfo info = new TCBubbleInfo();
        info.setHeight(0);
        info.setWidth(0);
        info.setDefaultSize(40);
        info.setBubblePath(null);
        info.setIconPath(null);
        info.setRect(0, 0, 0, 0);
        mSubtitleInfo.setBubbleInfo(info);
    }

    @Override
    public void loadAllBubble(List<TCBubbleInfo> list) {
        mBubbles = list;
        mRvBubbles.setVisibility(View.VISIBLE);
        mBubbleAdapter = new BubbleAdapter(list);
        mBubbleAdapter.setOnItemClickListener(this);
        GridLayoutManager manager = new GridLayoutManager(mContentView.getContext(), 4, GridLayoutManager.VERTICAL, false);
        mRvBubbles.setLayoutManager(manager);
        mRvBubbles.setAdapter(mBubbleAdapter);
    }

    @Override
    public void show(@Nullable TCSubtitleInfo info) {
        if (info == null) {
            resetInfo();
        } else {
            mSubtitleInfo = info;
        }
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                enterAnimator();
            }
        });
    }

    @Override
    public void dismiss() {
        exitAnimator();
    }

    private void exitAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mContentView, "translationY", 0,
                mContentView.getHeight());
        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                BubbleSubtitlePannel.super.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.start();
    }

    @Override
    public void onItemClick(View view, int position) {
        mSubtitleInfo.setBubblePos(position);
        mSubtitleInfo.setBubbleInfo(mBubbles.get(position));
        callback();
    }

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.bubble_iv_bubble) {
            mLlColor.setVisibility(View.GONE);
            mTvTextColor.setSelected(false);

            mTvBubbleStyle.setSelected(true);
            mRvBubbles.setVisibility(View.VISIBLE);

        } else if (i == R.id.bubble_iv_color) {
            mLlColor.setVisibility(View.VISIBLE);
            mTvTextColor.setSelected(true);

            mTvBubbleStyle.setSelected(false);
            mRvBubbles.setVisibility(View.GONE);

        } else if (i == R.id.iv_close) {
            dismiss();


        }
    }

    @Override
    public void onFinishColor(@ColorInt int color) {
        mSubtitleInfo.setTextColor(color);
        callback();
    }

    @Override
    public void onProgressColor(@ColorInt int color) {
        mCvColor.setColor(color);
    }

    private void callback() {
        if (mCallback != null) {
            TCSubtitleInfo info = new TCSubtitleInfo();
            info.setBubblePos(mSubtitleInfo.getBubblePos());
            info.setBubbleInfo(mSubtitleInfo.getBubbleInfo());
            info.setTextColor(mSubtitleInfo.getTextColor());
            mCallback.onBubbleSubtitleCallback(info);
        }
    }

    @Override
    public void setOnBubbleSubtitleCallback(OnBubbleSubtitleCallback callback) {
        mCallback = callback;
    }

    public void setTabTextColor(int selectedColor, int normalColor) {
        int[] colors = new int[]{selectedColor, normalColor};
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled};
        ColorStateList colorList = new ColorStateList(states, colors);

        mTvBubbleStyle.setTextColor(colorList);
        mTvTextColor.setTextColor(colorList);
    }

    public void setTabTextSize(int size) {
        mTvBubbleStyle.setTextSize(size);
        mTvTextColor.setTextSize(size);
    }

    public void setCancelIconResource(int resid) {
        mIvClose.setImageResource(resid);
    }

}

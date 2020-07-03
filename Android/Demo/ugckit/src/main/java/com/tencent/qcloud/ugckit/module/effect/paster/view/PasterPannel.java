package com.tencent.qcloud.ugckit.module.effect.paster.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.module.effect.paster.IPasterPannel;
import com.tencent.qcloud.ugckit.module.effect.paster.TCPasterInfo;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;


import java.util.List;

/**
 * 贴纸面板
 */
public class PasterPannel extends LinearLayout implements IPasterPannel, View.OnClickListener {

    private Context mContext;
    private int mSelectColor;

    @Override
    public int getCurrentTab() {
        return currentTab;
    }

    private int currentTab;

    private TextView mTvPaster;
    private TextView mTvAnimatedPaster;

    private RecyclerView mRecyclerView;
    private PasterAdapter mPasterAdapter;
    private ImageView mBtnSure;

    private IPasterPannel.OnTabChangedListener mOnTabChangedListener;
    private IPasterPannel.OnAddClickListener mOnAddClickListener;
    private IPasterPannel.OnItemClickListener mOnItemClickListener;

    public PasterPannel(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PasterPannel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PasterPannel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_paster_select, this);
        mTvPaster = (TextView) findViewById(R.id.tv_paster);
        mTvPaster.setOnClickListener(this);

        mTvAnimatedPaster = (TextView) findViewById(R.id.tv_animated_paster);
        mTvAnimatedPaster.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.paster_recycler_view);
        mBtnSure = (ImageView) findViewById(R.id.paster_btn_done);
        mBtnSure.setOnClickListener(this);

        currentTab = TAB_ANIMATED_PASTER;

        mSelectColor = UIAttributeUtil.getColorRes(context, R.attr.ugckitColorPrimary, R.color.colorRed2);
        mTvAnimatedPaster.setTextColor(mSelectColor);
        mTvPaster.setTextColor(context.getResources().getColor(R.color.white));
    }

    public void setPasterInfoList(List<TCPasterInfo> pasterInfoList) {
        mPasterAdapter = new PasterAdapter(pasterInfoList);
        mPasterAdapter.setOnItemClickListener(mOnItemClickListener);

        GridLayoutManager manager = new GridLayoutManager(mContext, 1, GridLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mPasterAdapter);
    }

    @Override
    public void onClick(@NonNull View view) {
        int i = view.getId();
        if (i == R.id.tv_animated_paster) {
            if (currentTab == TAB_ANIMATED_PASTER) {
                return;
            }
            currentTab = TAB_ANIMATED_PASTER;
            mTvAnimatedPaster.setTextColor(mSelectColor);
            mTvPaster.setTextColor(mContext.getResources().getColor(R.color.white));
            if (mOnTabChangedListener != null) {
                mOnTabChangedListener.onTabChanged(currentTab);
            }

        } else if (i == R.id.tv_paster) {
            if (currentTab == TAB_PASTER) {
                return;
            }
            currentTab = TAB_PASTER;
            mTvAnimatedPaster.setTextColor(mContext.getResources().getColor(R.color.white));
            mTvPaster.setTextColor(mSelectColor);
            if (mOnTabChangedListener != null) {
                mOnTabChangedListener.onTabChanged(currentTab);
            }

        } else if (i == R.id.paster_btn_done) {
            exitAnimator();
        }
    }

    @Override
    public void show() {
        this.post(new Runnable() {
            @Override
            public void run() {
                enterAnimator();
            }
        });
    }

    @Override
    public void dismiss() {
        this.post(new Runnable() {
            @Override
            public void run() {
                exitAnimator();
            }
        });
    }

    private void enterAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", this.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        set.play(translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                PasterPannel.this.setVisibility(VISIBLE);
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

    private void exitAnimator() {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", 0,
                this.getHeight());
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
                PasterPannel.this.setVisibility(GONE);
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
    public void setOnTabChangedListener(IPasterPannel.OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    @Override
    public void setOnItemClickListener(IPasterPannel.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void setOnAddClickListener(IPasterPannel.OnAddClickListener listener) {
        mOnAddClickListener = listener;
    }

    public void setTabTextColor(int selectedColor, int normalColor) {
        int[] colors = new int[]{selectedColor, normalColor};
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled};
        ColorStateList colorList = new ColorStateList(states, colors);

        mTvPaster.setTextColor(colorList);
        mTvAnimatedPaster.setTextColor(colorList);
    }

    public void setTabTextSize(int size) {
        mTvPaster.setTextSize(size);
        mTvAnimatedPaster.setTextSize(size);
    }

    public void setCancelIconResource(int resid) {
        mBtnSure.setImageResource(resid);
    }
}

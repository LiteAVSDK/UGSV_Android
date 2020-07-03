package com.tencent.qcloud.ugckit.module.record;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXRecordCommon;

/**
 * 屏比，目前有三种（1:1；3:4；4:3; 9:16; 16:9）
 */
public class AspectView extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "AspectView";
    private Activity mActivity;

    private TextView mTvAspect;
    private ImageView mIvAspectCurr;
    private ImageView mIvAspectFirst;
    private ImageView mIvAspectSecond;
    private ImageView mIvAspectThird;
    private ImageView mIvAspectFourth;
    private ImageView mIvAspecteMask;

    private RelativeLayout mLayoutAspectSelect;
    private boolean mToggleAspect;
    // UI上三个位置的屏比分别对应哪个Icon
    private int mFirstAspect;
    private int mSecondAspect;
    private int mThirdAspect;
    private int mFourthAspect;
    private int mCurrentAspect;
    private OnAspectListener mOnAspectListener;

    public AspectView(Context context) {
        super(context);
        initViews();
    }

    public AspectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AspectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.aspect_view, this);

        mTvAspect = (TextView) findViewById(R.id.tv_aspect);
        mIvAspectCurr = (ImageView) findViewById(R.id.iv_aspect);
        mIvAspecteMask = (ImageView) findViewById(R.id.iv_aspect_mask);
        mIvAspectFirst = (ImageView) findViewById(R.id.iv_aspect_first);
        mIvAspectSecond = (ImageView) findViewById(R.id.iv_aspect_second);
        mIvAspectThird = (ImageView) findViewById(R.id.iv_aspect_third);
        mIvAspectFourth = (ImageView) findViewById(R.id.iv_aspect_fourth);
        mLayoutAspectSelect = (RelativeLayout) findViewById(R.id.layout_aspect_select);

        mIvAspectCurr.setOnClickListener(this);
        mIvAspectFirst.setOnClickListener(this);
        mIvAspectSecond.setOnClickListener(this);
        mIvAspectThird.setOnClickListener(this);
        mIvAspectFourth.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.iv_aspect) { // 点击当前屏比
            toggleAspectAnim();
        } else if (id == R.id.iv_aspect_first) { // 点击第一个位置的屏比
            toggleAspectAnim();
            selectAnotherAspect(mFirstAspect);
        } else if (id == R.id.iv_aspect_second) { // 点击第2个位置的屏比
            toggleAspectAnim();
            selectAnotherAspect(mSecondAspect);
        } else if (id == R.id.iv_aspect_third) { // 点击第3个位置的屏比
            toggleAspectAnim();
            selectAnotherAspect(mThirdAspect);
        } else if (id == R.id.iv_aspect_fourth) { // 点击第4个位置的屏比
            toggleAspectAnim();
            selectAnotherAspect(mFourthAspect);
        }
    }

    private void toggleAspectAnim() {
        if (!mToggleAspect) {
            showAspectSelectAnim();
        } else {
            hideAspectSelectAnim();
        }
        mToggleAspect = !mToggleAspect;
    }

    private void selectAnotherAspect(int targetScale) {
        mCurrentAspect = targetScale;

        switch (mCurrentAspect) {
            case TXRecordCommon.VIDEO_ASPECT_RATIO_9_16:
                if (mOnAspectListener != null) {
                    mOnAspectListener.onAspectSelect(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
                }
                mIvAspectCurr.setImageResource(R.drawable.ic_aspect_916);
                mIvAspectFirst.setImageResource(R.drawable.ic_aspect_11);
                mIvAspectSecond.setImageResource(R.drawable.ic_aspect_34);
                mIvAspectThird.setImageResource(R.drawable.ic_aspect_43);
                mIvAspectFourth.setImageResource(R.drawable.ic_aspect_169);

                mFirstAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                mSecondAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                mThirdAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_4_3;
                mFourthAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_16_9;
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_3_4:
                if (mOnAspectListener != null) {
                    mOnAspectListener.onAspectSelect(TXRecordCommon.VIDEO_ASPECT_RATIO_3_4);
                }
                mIvAspectCurr.setImageResource(R.drawable.ic_aspect_34);
                mIvAspectFirst.setImageResource(R.drawable.ic_aspect_11);
                mIvAspectSecond.setImageResource(R.drawable.ic_aspect_916);
                mIvAspectThird.setImageResource(R.drawable.ic_aspect_43);
                mIvAspectFourth.setImageResource(R.drawable.ic_aspect_169);

                mFirstAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                mSecondAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                mThirdAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_4_3;
                mFourthAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_16_9;
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_1_1:
                if (mOnAspectListener != null) {
                    mOnAspectListener.onAspectSelect(TXRecordCommon.VIDEO_ASPECT_RATIO_1_1);
                }
                mIvAspectCurr.setImageResource(R.drawable.ic_aspect_11);
                mIvAspectFirst.setImageResource(R.drawable.ic_aspect_34);
                mIvAspectSecond.setImageResource(R.drawable.ic_aspect_916);
                mIvAspectThird.setImageResource(R.drawable.ic_aspect_43);
                mIvAspectFourth.setImageResource(R.drawable.ic_aspect_169);

                mFirstAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                mSecondAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                mThirdAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_4_3;
                mFourthAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_16_9;
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_4_3:
                if (mOnAspectListener != null) {
                    mOnAspectListener.onAspectSelect(TXRecordCommon.VIDEO_ASPECT_RATIO_4_3);
                }
                mIvAspectCurr.setImageResource(R.drawable.ic_aspect_43);
                mIvAspectFirst.setImageResource(R.drawable.ic_aspect_34);
                mIvAspectSecond.setImageResource(R.drawable.ic_aspect_916);
                mIvAspectThird.setImageResource(R.drawable.ic_aspect_11);
                mIvAspectFourth.setImageResource(R.drawable.ic_aspect_169);

                mFirstAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                mSecondAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                mThirdAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                mFourthAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_16_9;
                break;
            case TXRecordCommon.VIDEO_ASPECT_RATIO_16_9:
                if (mOnAspectListener != null) {
                    mOnAspectListener.onAspectSelect(TXRecordCommon.VIDEO_ASPECT_RATIO_16_9);
                }
                mIvAspectCurr.setImageResource(R.drawable.ic_aspect_169);
                mIvAspectFirst.setImageResource(R.drawable.ic_aspect_34);
                mIvAspectSecond.setImageResource(R.drawable.ic_aspect_916);
                mIvAspectThird.setImageResource(R.drawable.ic_aspect_11);
                mIvAspectFourth.setImageResource(R.drawable.ic_aspect_43);

                mFirstAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                mSecondAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                mThirdAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                mFourthAspect = TXRecordCommon.VIDEO_ASPECT_RATIO_4_3;
                break;
        }
    }

    /**
     * 显示切换屏比动画
     */
    private void showAspectSelectAnim() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(mLayoutAspectSelect, "translationX",
                2 * (getResources().getDimension(R.dimen.ugc_aspect_divider) + getResources().getDimension(R.dimen.ugc_aspect_width)), 0f);
        showAnimator.setDuration(80);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mLayoutAspectSelect.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        showAnimator.start();
    }

    /**
     * 隐藏切换屏比动画
     */
    public void hideAspectSelectAnim() {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(mLayoutAspectSelect, "translationX", 0f,
                2 * (getResources().getDimension(R.dimen.ugc_aspect_divider) + getResources().getDimension(R.dimen.ugc_aspect_width)));
        showAnimator.setDuration(80);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mLayoutAspectSelect.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        showAnimator.start();
    }

    /**
     * 设置切换"屏比"监听器
     *
     * @param listener
     */
    public void setOnAspectListener(OnAspectListener listener) {
        mOnAspectListener = listener;
    }

    public void enableMask() {
        mIvAspecteMask.setVisibility(View.VISIBLE);
        mIvAspectCurr.setEnabled(false);
    }

    public void disableMask() {
        mIvAspecteMask.setVisibility(View.GONE);
        mIvAspectCurr.setEnabled(true);
    }

    public void setTextSize(int size) {
        mTvAspect.setTextSize(size);
    }

    public void setTextColor(int color) {
        mTvAspect.setTextColor(getResources().getColor(color));
    }

    public void setIconList(int[] iconList) {
        Log.d(TAG, "iconList size:" + iconList.length);

        mIvAspectCurr.setImageResource(iconList[0]);
        mIvAspectFirst.setImageResource(iconList[1]);
        mIvAspectSecond.setImageResource(iconList[2]);
    }

    public void setAspect(int aspectRatio) {
        selectAnotherAspect(aspectRatio);
    }

    public interface OnAspectListener {
        /**
         * 选择一种屏比
         *
         * @param currentAspect 当前屏比
         */
        void onAspectSelect(int currentAspect);
    }
}

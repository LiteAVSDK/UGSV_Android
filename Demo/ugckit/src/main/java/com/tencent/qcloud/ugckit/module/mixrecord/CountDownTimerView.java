package com.tencent.qcloud.ugckit.module.mixrecord;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.R;


/**
 * 倒计时动画View
 */
public class CountDownTimerView extends RelativeLayout implements ICountDownTimerView {
    public static int DEFAULT_COUNTDOWN_NUMBER = 3;

    private TextView           mTextNumber;
    private ICountDownListener mListener;
    private boolean            mAnimationStart;

    public CountDownTimerView(Context context) {
        super(context);
        initViews();
    }

    public CountDownTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public CountDownTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_countdown_view, this);

        mTextNumber = (TextView) findViewById(R.id.tv_number);
    }

    @Override
    public void setOnCountDownListener(ICountDownListener listener) {
        mListener = listener;
    }

    /**
     * 播放倒计时动画
     *
     * @param num
     */
    public void countDownAnimation(final int num) {
        if (num <= 0) {
            mAnimationStart = false;
            mTextNumber.setVisibility(GONE);

            if (mListener != null) {
                mListener.onCountDownComplete();
            }
            return;
        }
        mAnimationStart = true;
        mTextNumber.setVisibility(View.VISIBLE);
        mTextNumber.setText(Integer.toString(num));

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mTextNumber, "scaleX", 0, 1.1f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mTextNumber, "scaleY", 0, 1.1f);

        ObjectAnimator animatorX2 = ObjectAnimator.ofFloat(mTextNumber, "scaleX", 1.1f, 1);
        ObjectAnimator animatorY2 = ObjectAnimator.ofFloat(mTextNumber, "scaleY", 1.1f, 1);

        ObjectAnimator animatorX3 = ObjectAnimator.ofFloat(mTextNumber, "scaleX", 1, 1);
        ObjectAnimator animatorY3 = ObjectAnimator.ofFloat(mTextNumber, "scaleY", 1, 1);

        ObjectAnimator animatorX4 = ObjectAnimator.ofFloat(mTextNumber, "scaleX", 1, 0);
        ObjectAnimator animatorY4 = ObjectAnimator.ofFloat(mTextNumber, "scaleY", 1, 0);


        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.play(animatorX).with(animatorY);
        animatorSet1.setDuration(100);
        animatorSet1.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.play(animatorX2).with(animatorY2);
        animatorSet2.setDuration(100);
        animatorSet2.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.play(animatorX3).with(animatorY3);
        animatorSet3.setDuration(500);
        animatorSet3.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet4 = new AnimatorSet();
        animatorSet4.play(animatorX4).with(animatorY4);
        animatorSet4.setDuration(300);
        animatorSet4.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(animatorSet1);
        animatorSet.play(animatorSet2).after(animatorSet1);
        animatorSet.play(animatorSet3).after(animatorSet2);
        animatorSet.play(animatorSet4).after(animatorSet3);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimationStart) {
                    countDownAnimation(num - 1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    @Override
    public void setCountDownTextColor(int color) {
        mTextNumber.setTextColor(getResources().getColor(color));
    }

    @Override
    public void setCountDownNumber(int number) {
        DEFAULT_COUNTDOWN_NUMBER = number;
    }

    @Override
    public void cancelDownAnimation() {
        DEFAULT_COUNTDOWN_NUMBER = 3;
        mAnimationStart = false;
    }

}

package com.tencent.qcloud.ugckit.module.mixrecord;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.R;


public class MixRecordRightLayout extends RelativeLayout implements View.OnClickListener, IMixRecordRightLayout {
    private Activity mActivity;
    // 美颜
    private ImageView mIvBeauty;
    private TextView mTvBeauty;
    private RelativeLayout mLayoutBeauty;
    // 倒计时
    private ImageView mIvCountDown;
    private TextView mTvCountDown;
    private RelativeLayout mLayoutCountdown;
    private OnItemClickListener mOnItemClickListener;

    public MixRecordRightLayout(Context context) {
        super(context);
        initViews();
    }

    public MixRecordRightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public MixRecordRightLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.chorus_right_layout, this);

        mLayoutBeauty = (RelativeLayout) findViewById(R.id.layout_beauty);
        mIvBeauty = (ImageView) findViewById(R.id.iv_beauty);
        mTvBeauty = (TextView) findViewById(R.id.tv_beauty);
        mIvBeauty.setOnClickListener(this);

        mLayoutCountdown = (RelativeLayout) findViewById(R.id.layout_countdown);
        mIvCountDown = (ImageView) findViewById(R.id.iv_countdown);
        mTvCountDown = (TextView) findViewById(R.id.tv_countdown);
        mIvCountDown.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.iv_beauty) {
            mOnItemClickListener.onShowBeautyPanel();
        } else if (id == R.id.iv_countdown) {
            mOnItemClickListener.countDownTimer();
        }
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void disableCountDownTimer() {
        mLayoutCountdown.setVisibility(View.GONE);
    }

    public void disableBeauty() {
        mLayoutBeauty.setVisibility(View.GONE);
    }

    @Override
    public void setBeautyIconResource(int resid) {
        mIvBeauty.setImageResource(resid);
    }

    @Override
    public void setBeautyTextSize(int size) {
        mTvBeauty.setTextSize(size);
    }

    @Override
    public void setBeautyTextColor(int color) {
        mTvBeauty.setTextColor(getResources().getColor(color));
    }

    @Override
    public void setCountDownIconResource(int resid) {
        mIvCountDown.setImageResource(resid);
    }

    @Override
    public void setCountDownTextSize(int size) {
        mTvCountDown.setTextSize(size);
    }

    @Override
    public void setCountDownTextColor(int color) {
        mTvCountDown.setTextColor(getResources().getColor(color));
    }

}

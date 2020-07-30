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

    private ImageView       mImageBeauty;       // 美颜
    private TextView        mTextBeauty;
    private RelativeLayout  mLayoutBeauty;
    private ImageView       mImageCountDown;    // 倒计时
    private TextView        mTextCountDown;
    private RelativeLayout  mLayoutCountdown;

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
        inflate(mActivity, R.layout.ugckit_chorus_right_layout, this);

        mLayoutBeauty = (RelativeLayout) findViewById(R.id.layout_beauty);
        mImageBeauty = (ImageView) findViewById(R.id.iv_beauty);
        mTextBeauty = (TextView) findViewById(R.id.tv_beauty);
        mImageBeauty.setOnClickListener(this);

        mLayoutCountdown = (RelativeLayout) findViewById(R.id.layout_countdown);
        mImageCountDown = (ImageView) findViewById(R.id.iv_countdown);
        mTextCountDown = (TextView) findViewById(R.id.tv_countdown);
        mImageCountDown.setOnClickListener(this);
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
        mImageBeauty.setImageResource(resid);
    }

    @Override
    public void setBeautyTextSize(int size) {
        mTextBeauty.setTextSize(size);
    }

    @Override
    public void setBeautyTextColor(int color) {
        mTextBeauty.setTextColor(getResources().getColor(color));
    }

    @Override
    public void setCountDownIconResource(int resid) {
        mImageCountDown.setImageResource(resid);
    }

    @Override
    public void setCountDownTextSize(int size) {
        mTextCountDown.setTextSize(size);
    }

    @Override
    public void setCountDownTextColor(int color) {
        mTextCountDown.setTextColor(getResources().getColor(color));
    }

}

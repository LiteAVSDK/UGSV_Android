package com.tencent.qcloud.ugckit.module.picturetransition;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXVideoEditConstants;

public class PictureTransitionLayout extends RelativeLayout implements View.OnClickListener, IPictureTransitionLayout {

    private FragmentActivity     mActivity;
    private ImageButton          mIbTransition1; //左右
    private ImageButton          mIbTransition2; //上下
    private ImageButton          mIbTransition3; //放大
    private ImageButton          mIbTransition4; //缩小
    private ImageButton          mIbTransition5; //旋转
    private ImageButton          mIbTransition6; //淡入淡出
    private OnTransitionListener mOnTransitionListener;

    public PictureTransitionLayout(Context context) {
        super(context);
        initViews();
    }

    public PictureTransitionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PictureTransitionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (FragmentActivity) getContext();
        inflate(mActivity, R.layout.ugckit_pic_transition_layout, this);
        mIbTransition1 = (ImageButton) findViewById(R.id.transition1);
        mIbTransition2 = (ImageButton) findViewById(R.id.transition2);
        mIbTransition3 = (ImageButton) findViewById(R.id.transition3);
        mIbTransition4 = (ImageButton) findViewById(R.id.transition4);
        mIbTransition5 = (ImageButton) findViewById(R.id.transition5);
        mIbTransition6 = (ImageButton) findViewById(R.id.transition6);

        mIbTransition1.setOnClickListener(this);
        mIbTransition2.setOnClickListener(this);
        mIbTransition3.setOnClickListener(this);
        mIbTransition4.setOnClickListener(this);
        mIbTransition5.setOnClickListener(this);
        mIbTransition6.setOnClickListener(this);

        // 设置默认左右切换
        mIbTransition1.setSelected(true);
    }

    @Override
    public void onClick(@NonNull View v) {
        mIbTransition1.setSelected(false);
        mIbTransition2.setSelected(false);
        mIbTransition3.setSelected(false);
        mIbTransition4.setSelected(false);
        mIbTransition5.setSelected(false);
        mIbTransition6.setSelected(false);
        int id = v.getId();
        if (id == R.id.transition1) {
            mIbTransition1.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
        } else if (id == R.id.transition2) {
            mIbTransition2.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_UP_DOWN_SLIPPING);
        } else if (id == R.id.transition3) {
            mIbTransition3.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_ENLARGE);
        } else if (id == R.id.transition4) {
            mIbTransition4.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_NARROW);
        } else if (id == R.id.transition5) {
            mIbTransition5.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_ROTATIONAL_SCALING);
        } else if (id == R.id.transition6) {
            mIbTransition6.setSelected(true);
            mOnTransitionListener.transition(TXVideoEditConstants.TX_TRANSITION_TYPE_FADEIN_FADEOUT);
        }
    }

    @Override
    public void setTransitionListener(OnTransitionListener transitionListener) {
        mOnTransitionListener = transitionListener;
    }

    @Override
    public void disableLeftrightTransition() {
        mIbTransition1.setVisibility(View.GONE);
    }

    @Override
    public void disableUpdownTransition() {
        mIbTransition2.setVisibility(View.GONE);
    }

    @Override
    public void disableEnlargeTransition() {
        mIbTransition3.setVisibility(View.GONE);
    }

    @Override
    public void disableNarrowTransition() {
        mIbTransition4.setVisibility(View.GONE);
    }

    @Override
    public void disableRotateTransition() {
        mIbTransition5.setVisibility(View.GONE);
    }

    @Override
    public void disableFadeinoutTransition() {
        mIbTransition6.setVisibility(View.GONE);
    }

    @Override
    public void setLeftrightIconResource(int resid) {
        mIbTransition1.setImageResource(resid);
    }

    @Override
    public void setUpdownIconResource(int resid) {
        mIbTransition2.setImageResource(resid);
    }

    @Override
    public void setEnlargeIconResource(int resid) {
        mIbTransition3.setImageResource(resid);
    }

    @Override
    public void setNarrowIconResource(int resid) {
        mIbTransition4.setImageResource(resid);
    }

    @Override
    public void setRotateIconResource(int resid) {
        mIbTransition5.setImageResource(resid);
    }

    @Override
    public void setFadeinoutIconResource(int resid) {
        mIbTransition6.setImageResource(resid);
    }

}

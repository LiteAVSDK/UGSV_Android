package com.tencent.qcloud.ugckit.module.mixrecord;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.PermissionIntroductionDialog;
import com.tencent.qcloud.ugckit.R;


public class MixRecordRightLayout extends RelativeLayout implements View.OnClickListener, IMixRecordRightLayout {
    private Activity            mActivity;
    private ImageView           mImageBeauty;       // 基础美颜
    private ImageView           mImageTeBeauty;       // 高级美颜
    private TextView            mTextBeauty;
    private RelativeLayout      mLayoutBeauty;
    private ImageView           mImageCountDown;    // 倒计时
    private TextView            mTextCountDown;
    private RelativeLayout      mLayoutCountdown;
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
        mImageTeBeauty = findViewById(R.id.iv_te_beauty);
        mTextBeauty = (TextView) findViewById(R.id.tv_beauty);
        mImageBeauty.setOnClickListener(this);
        mImageTeBeauty.setOnClickListener(this);

        mLayoutCountdown = (RelativeLayout) findViewById(R.id.layout_countdown);
        mImageCountDown = (ImageView) findViewById(R.id.iv_countdown);
        mTextCountDown = (TextView) findViewById(R.id.tv_countdown);
        mImageCountDown.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.iv_beauty) {
            if (!PermissionIntroductionDialog.isGrantPermission()) {
                showIntroductionDialog(((FragmentActivity) getContext()).getSupportFragmentManager(),
                        new PermissionIntroductionDialog.PositiveClickListener() {
                            @Override
                            public void onClickPositive() {
                                mOnItemClickListener.onShowBeautyPanel();
                            }
                        });
            } else {
                mOnItemClickListener.onShowBeautyPanel();
            }
        } else if (id == R.id.iv_te_beauty) {
            if (!PermissionIntroductionDialog.isGrantPermission()) {
                showIntroductionDialog(((FragmentActivity) getContext()).getSupportFragmentManager(),
                        new PermissionIntroductionDialog.PositiveClickListener() {
                            @Override
                            public void onClickPositive() {
                                mOnItemClickListener.onShowTEBeautyPanel();
                            }
                        });
            } else {
                mOnItemClickListener.onShowTEBeautyPanel();
            }
        } else if (id == R.id.iv_countdown) {
            mOnItemClickListener.countDownTimer();
        }
    }


    private void showIntroductionDialog(FragmentManager fragmentManager,
                                        PermissionIntroductionDialog.PositiveClickListener listener) {
        PermissionIntroductionDialog
                mPermissionIntroductionDialog = new PermissionIntroductionDialog(getContext()
                .getString(R.string.app_personal_information_collection),
                getContext().getString(R.string.beauty_cam_introduction),
                PermissionIntroductionDialog.DialogPosition.BOTTOM);
        mPermissionIntroductionDialog.setPositiveClickListener(listener);
        mPermissionIntroductionDialog.show(fragmentManager, PermissionIntroductionDialog.DIALOG_NAME);
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

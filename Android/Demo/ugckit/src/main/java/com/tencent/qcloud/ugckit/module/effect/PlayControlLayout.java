package com.tencent.qcloud.ugckit.module.effect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.module.PlayerManagerKit;
import com.tencent.qcloud.ugckit.utils.DateTimeUtil;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;

public class PlayControlLayout extends RelativeLayout implements IPlayControlLayout, View.OnClickListener, PlayerManagerKit.OnPreviewListener, PlayerManagerKit.OnPlayStateListener {
    private static final String TAG = "PlayControlLayout";

    private ImageView mImagePlay;
    private TextView  mTextCurrent;
    private int       mPauseIcon           = R.drawable.ugckit_ic_pause_normal;
    private int       mPlayIcon            = R.drawable.ugckit_ic_play_normal;
    private int       mCurrentTimeColor    = R.color.ugckit_white;
    private int       mCurrentTimeTextSize = 15;

    public PlayControlLayout(Context context) {
        super(context);
        initViews();
    }

    public PlayControlLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PlayControlLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_play_control_view, this);

        mImagePlay = (ImageView) findViewById(R.id.iv_play);
        mImagePlay.setOnClickListener(this);

        mTextCurrent = (TextView) findViewById(R.id.tv_current);
        mTextCurrent.setTextColor(getResources().getColor(mCurrentTimeColor));
        mTextCurrent.setTextSize(mCurrentTimeTextSize);

        PlayerManagerKit.getInstance().addOnPreviewLitener(this);
        PlayerManagerKit.getInstance().addOnPlayStateLitener(this);

        mPlayIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerPlayIcon, R.drawable.ugckit_ic_play_normal);
        mPauseIcon = UIAttributeUtil.getResResources(getContext(), R.attr.editerPauseIcon, R.drawable.ugckit_ic_pause_normal);
    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.iv_play) {
            PlayerManagerKit.getInstance().playVideo(false);
        }
    }

    @Override
    public void onPreviewProgress(int timeMs) {
        mTextCurrent.setText(DateTimeUtil.duration(timeMs));
    }

    @Override
    public void onPreviewFinish() {

    }

    public void updateUIByFragment(int type) {
        if (type == UGCKitConstants.TYPE_EDITER_BGM) {
            mImagePlay.setVisibility(View.GONE);
        } else {
            mImagePlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlayStateStart() {
        mImagePlay.setImageResource(mPauseIcon);
    }

    @Override
    public void onPlayStateResume() {
        mImagePlay.setImageResource(mPauseIcon);
    }

    @Override
    public void onPlayStatePause() {
        mImagePlay.setImageResource(mPlayIcon);
    }

    @Override
    public void onPlayStateStop() {
        mImagePlay.setImageResource(mPlayIcon);
    }

    @Override
    public void setCurrentTimeTextSize(int size) {
        mCurrentTimeTextSize = size;
    }

    @Override
    public void setCurrentTimeTextColor(int color) {
        mCurrentTimeColor = color;
    }

    @Override
    public void setPlayIconResource(int resid) {
        mPlayIcon = resid;
    }

    @Override
    public void setPauseIconResource(int resid) {
        mPauseIcon = resid;
    }

}

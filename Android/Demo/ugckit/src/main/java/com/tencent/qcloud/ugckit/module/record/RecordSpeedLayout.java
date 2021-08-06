package com.tencent.qcloud.ugckit.module.record;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;


import com.tencent.qcloud.ugckit.utils.UIAttributeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordSpeedLayout;
import com.tencent.ugc.TXRecordCommon;

/**
 * 录制视频的播放速度，目前支持五种（极慢，慢速，标准，快速，极快）
 */
public class RecordSpeedLayout extends RelativeLayout implements RadioGroup.OnCheckedChangeListener, IRecordSpeedLayout {
    private static final String                TAG          = "RecordSpeedLayout";
    private              Activity              mActivity;
    private              RadioGroup            mRadioGroup;
    private              RadioButton           mRadioSloweset;
    private              RadioButton           mRadioSlow;
    private              RadioButton           mRadioNormal;
    private              RadioButton           mRadioFast;
    private              RadioButton           mRadioFastest;
    private              int                   mSlowestBg   = R.drawable.ugckit_record_left_bg;
    private              int                   mSlowBg      = R.drawable.ugckit_record_mid_bg;
    private              int                   mNormalBg    = R.drawable.ugckit_record_mid_bg;
    private              int                   mFastBg      = R.drawable.ugckit_record_mid_bg;
    private              int                   mFastestBg   = R.drawable.ugckit_record_right_bg;
    private              int                   mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;  // 录制速度
    private              OnRecordSpeedListener mOnRecordSpeedListener;

    public RecordSpeedLayout(Context context) {
        super(context);
        initViews();
    }

    public RecordSpeedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordSpeedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (Activity) getContext();
        inflate(mActivity, R.layout.ugckit_record_speed_layout, this);

        mRadioGroup = (RadioGroup) findViewById(R.id.rg_record_speed);
        mRadioFast = (RadioButton) findViewById(R.id.rb_fast);
        mRadioFastest = (RadioButton) findViewById(R.id.rb_fastest);
        mRadioNormal = (RadioButton) findViewById(R.id.rb_normal);
        mRadioSlow = (RadioButton) findViewById(R.id.rb_slow);
        mRadioSloweset = (RadioButton) findViewById(R.id.rb_slowest);

        mSlowestBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedSlowestBackgroud, R.drawable.ugckit_record_left_bg);
        mSlowBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedSlowBackground, R.drawable.ugckit_record_mid_bg);
        mNormalBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedNormalBackground, R.drawable.ugckit_record_mid_bg);
        mFastBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedFastBackground, R.drawable.ugckit_record_mid_bg);
        mFastestBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedFastestBackground, R.drawable.ugckit_record_right_bg);

        ((RadioButton) findViewById(R.id.rb_normal)).setChecked(true);
        mRadioNormal.setBackgroundResource(mNormalBg);
        mRadioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.rb_fast) {
            mRadioFast.setBackgroundResource(mFastBg);
            mRadioFastest.setBackgroundColor(Color.TRANSPARENT);
            mRadioNormal.setBackgroundColor(Color.TRANSPARENT);
            mRadioSlow.setBackgroundColor(Color.TRANSPARENT);
            mRadioSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_FAST;
        } else if (checkedId == R.id.rb_fastest) {
            mRadioFastest.setBackgroundResource(mFastestBg);
            mRadioFast.setBackgroundColor(Color.TRANSPARENT);
            mRadioNormal.setBackgroundColor(Color.TRANSPARENT);
            mRadioSlow.setBackgroundColor(Color.TRANSPARENT);
            mRadioSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_FASTEST;

        } else if (checkedId == R.id.rb_normal) {
            mRadioNormal.setBackgroundResource(mNormalBg);
            mRadioFastest.setBackgroundColor(Color.TRANSPARENT);
            mRadioFast.setBackgroundColor(Color.TRANSPARENT);
            mRadioSlow.setBackgroundColor(Color.TRANSPARENT);
            mRadioSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;

        } else if (checkedId == R.id.rb_slow) {
            mRadioSlow.setBackgroundResource(mSlowBg);
            mRadioFastest.setBackgroundColor(Color.TRANSPARENT);
            mRadioFast.setBackgroundColor(Color.TRANSPARENT);
            mRadioNormal.setBackgroundColor(Color.TRANSPARENT);
            mRadioSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_SLOW;

        } else if (checkedId == R.id.rb_slowest) {
            mRadioSloweset.setBackgroundResource(mSlowestBg);
            mRadioFastest.setBackgroundColor(Color.TRANSPARENT);
            mRadioFast.setBackgroundColor(Color.TRANSPARENT);
            mRadioNormal.setBackgroundColor(Color.TRANSPARENT);
            mRadioSlow.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_SLOWEST;
        }

        if (mOnRecordSpeedListener != null) {
            mOnRecordSpeedListener.onSpeedSelect(mRecordSpeed);
        }
    }

    /**
     * 设置切换"速度"监听器
     *
     * @param listener
     */
    public void setOnSpeedListener(RecordSpeedLayout.OnRecordSpeedListener listener) {
        mOnRecordSpeedListener = listener;
    }

    @Override
    public void setSpeedTextSize(int size) {
        mRadioFast.setTextSize(size);
        mRadioFastest.setTextSize(size);
        mRadioNormal.setTextSize(size);
        mRadioSlow.setTextSize(size);
        mRadioSloweset.setTextSize(size);
    }

    @Override
    public void setSpeedTextColor(ColorStateList color) {
        mRadioFast.setTextColor(color);
        mRadioFastest.setTextColor(color);
        mRadioNormal.setTextColor(color);
        mRadioSlow.setTextColor(color);
        mRadioSloweset.setTextColor(color);
    }

    @Override
    public void setOnRecordSpeedListener(OnRecordSpeedListener listener) {
        mOnRecordSpeedListener = listener;
    }

    public void settIconList(int[] iconList) {
        Log.d(TAG, "iconList size:" + iconList.length);

        mRadioSloweset.setBackgroundResource(iconList[0]);
        mRadioSlow.setBackgroundResource(iconList[1]);
        mRadioNormal.setBackgroundResource(iconList[2]);
        mRadioFast.setBackgroundResource(iconList[3]);
        mRadioFastest.setBackgroundResource(iconList[4]);
    }
}

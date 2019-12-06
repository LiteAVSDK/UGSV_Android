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
    private static final String TAG = "RecordSpeedLayout";
    private Activity mActivity;

    private RadioGroup mRadioGroup;
    private RadioButton mRbSloweset;
    private RadioButton mRbSlow;
    private RadioButton mRbNormal;
    private RadioButton mRbFast;
    private RadioButton mRbFastest;

    private int slowestBg = R.drawable.record_left_bg;
    private int slowBg = R.drawable.record_mid_bg;
    private int normalBg = R.drawable.record_mid_bg;
    private int fastBg = R.drawable.record_mid_bg;
    private int fastestBg = R.drawable.record_right_bg;

    /**
     * 录制速度
     */
    private int mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;
    private OnRecordSpeedListener mOnRecordSpeedListener;

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
        inflate(mActivity, R.layout.record_speed_layout, this);

        mRadioGroup = (RadioGroup) findViewById(R.id.rg_record_speed);
        mRbFast = (RadioButton) findViewById(R.id.rb_fast);
        mRbFastest = (RadioButton) findViewById(R.id.rb_fastest);
        mRbNormal = (RadioButton) findViewById(R.id.rb_normal);
        mRbSlow = (RadioButton) findViewById(R.id.rb_slow);
        mRbSloweset = (RadioButton) findViewById(R.id.rb_slowest);

        slowestBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedSlowestBackgroud, R.drawable.record_left_bg);
        slowBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedSlowBackground, R.drawable.record_mid_bg);
        normalBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedNormalBackground, R.drawable.record_mid_bg);
        fastBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedFastBackground, R.drawable.record_mid_bg);
        fastestBg = UIAttributeUtil.getResResources(mActivity, R.attr.recordSpeedFastestBackground, R.drawable.record_right_bg);

        ((RadioButton) findViewById(R.id.rb_normal)).setChecked(true);
        mRbNormal.setBackgroundResource(normalBg);
        mRadioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.rb_fast) {
            mRbFast.setBackgroundResource(fastBg);
            mRbFastest.setBackgroundColor(Color.TRANSPARENT);
            mRbNormal.setBackgroundColor(Color.TRANSPARENT);
            mRbSlow.setBackgroundColor(Color.TRANSPARENT);
            mRbSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_FAST;
        } else if (checkedId == R.id.rb_fastest) {
            mRbFastest.setBackgroundResource(fastestBg);
            mRbFast.setBackgroundColor(Color.TRANSPARENT);
            mRbNormal.setBackgroundColor(Color.TRANSPARENT);
            mRbSlow.setBackgroundColor(Color.TRANSPARENT);
            mRbSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_FASTEST;

        } else if (checkedId == R.id.rb_normal) {
            mRbNormal.setBackgroundResource(normalBg);
            mRbFastest.setBackgroundColor(Color.TRANSPARENT);
            mRbFast.setBackgroundColor(Color.TRANSPARENT);
            mRbSlow.setBackgroundColor(Color.TRANSPARENT);
            mRbSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_NORMAL;

        } else if (checkedId == R.id.rb_slow) {
            mRbSlow.setBackgroundResource(slowBg);
            mRbFastest.setBackgroundColor(Color.TRANSPARENT);
            mRbFast.setBackgroundColor(Color.TRANSPARENT);
            mRbNormal.setBackgroundColor(Color.TRANSPARENT);
            mRbSloweset.setBackgroundColor(Color.TRANSPARENT);
            mRecordSpeed = TXRecordCommon.RECORD_SPEED_SLOW;

        } else if (checkedId == R.id.rb_slowest) {
            mRbSloweset.setBackgroundResource(slowestBg);
            mRbFastest.setBackgroundColor(Color.TRANSPARENT);
            mRbFast.setBackgroundColor(Color.TRANSPARENT);
            mRbNormal.setBackgroundColor(Color.TRANSPARENT);
            mRbSlow.setBackgroundColor(Color.TRANSPARENT);
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
        mRbFast.setTextSize(size);
        mRbFastest.setTextSize(size);
        mRbNormal.setTextSize(size);
        mRbSlow.setTextSize(size);
        mRbSloweset.setTextSize(size);
    }

    @Override
    public void setSpeedTextColor(ColorStateList color) {
        mRbFast.setTextColor(color);
        mRbFastest.setTextColor(color);
        mRbNormal.setTextColor(color);
        mRbSlow.setTextColor(color);
        mRbSloweset.setTextColor(color);
    }

    @Override
    public void setOnRecordSpeedListener(OnRecordSpeedListener listener) {
        mOnRecordSpeedListener = listener;
    }

    public void settIconList(int[] iconList) {
        Log.d(TAG, "iconList size:" + iconList.length);

        mRbSloweset.setBackgroundResource(iconList[0]);
        mRbSlow.setBackgroundResource(iconList[1]);
        mRbNormal.setBackgroundResource(iconList[2]);
        mRbFast.setBackgroundResource(iconList[3]);
        mRbFastest.setBackgroundResource(iconList[4]);
    }
}

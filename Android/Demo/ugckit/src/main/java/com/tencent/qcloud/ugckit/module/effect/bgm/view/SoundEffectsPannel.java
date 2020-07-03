package com.tencent.qcloud.ugckit.module.effect.bgm.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.record.interfaces.ISoundEffectsPannel;
import com.tencent.ugc.TXRecordCommon;

/**
 * 音效Pannel
 */
public class SoundEffectsPannel extends RelativeLayout implements ISoundEffectsPannel, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private Context mContext;
    private SeekBar mMicVolumeSeekBar;
    private int mMicVolume = 100;

    private SoundEffectsSettingPannelListener mSoundEffectsSettingPannelListener;

    private int mLastReverbIndex;
    private int mLastVoiceChangerIndex;
    private Button mBtnVolume;
    private Button mBtnVoiceChange;
    private Button mBtnReverb;
    private LinearLayout mLayoutVolume;
    private HorizontalScrollView mLayoutVoiceChange;
    private HorizontalScrollView mLayoutReverb;

    public SoundEffectsPannel(Context context) {
        super(context);
        init(context);
    }

    public SoundEffectsPannel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SoundEffectsPannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_sound_effects, this);

        mBtnVolume = (Button) findViewById(R.id.btn_volume);
        mBtnVolume.setOnClickListener(this);
        mBtnVoiceChange = (Button) findViewById(R.id.btn_voicechange);
        mBtnVoiceChange.setOnClickListener(this);
        mBtnReverb = (Button) findViewById(R.id.btn_reverb);
        mBtnReverb.setOnClickListener(this);

        mLayoutVolume = (LinearLayout) findViewById(R.id.layout_volume);
        mLayoutVoiceChange = (HorizontalScrollView) findViewById(R.id.layout_voicechanger);
        mLayoutReverb = (HorizontalScrollView) findViewById(R.id.layout_reverb_type);

        mMicVolumeSeekBar = (SeekBar) findViewById(R.id.seekbar_mic_volume);
        mMicVolumeSeekBar.setOnSeekBarChangeListener(this);

        findViewById(R.id.btn_reverb_default).setOnClickListener(this);
        findViewById(R.id.btn_reverb_1).setOnClickListener(this);
        findViewById(R.id.btn_reverb_2).setOnClickListener(this);
        findViewById(R.id.btn_reverb_3).setOnClickListener(this);
        findViewById(R.id.btn_reverb_4).setOnClickListener(this);
        findViewById(R.id.btn_reverb_5).setOnClickListener(this);
        findViewById(R.id.btn_reverb_6).setOnClickListener(this);

        findViewById(R.id.btn_voicechanger_default).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_1).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_2).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_3).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_4).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_6).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_7).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_8).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_9).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_10).setOnClickListener(this);
        findViewById(R.id.btn_voicechanger_11).setOnClickListener(this);

        setDefaultRevertAndVoiceChange();
    }

    @Override
    public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seekbar_mic_volume) {
            mMicVolume = progress;
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onMicVolumeChanged(mMicVolume / (float) 100);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void setSoundEffectsSettingPannelListener(SoundEffectsSettingPannelListener soundEffectsSettingPannelListener) {
        mSoundEffectsSettingPannelListener = soundEffectsSettingPannelListener;
    }

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.btn_volume) {
            //音量
            mBtnVolume.setBackgroundResource(R.drawable.ic_ugc_soundeffect);
            mBtnVoiceChange.setBackground(null);
            mBtnReverb.setBackground(null);

            mLayoutVolume.setVisibility(View.VISIBLE);
            mLayoutReverb.setVisibility(View.GONE);
            mLayoutVoiceChange.setVisibility(View.GONE);
        } else if (i == R.id.btn_voicechange) {
            //变声
            mBtnVoiceChange.setBackgroundResource(R.drawable.ic_ugc_soundeffect);
            mBtnVolume.setBackground(null);
            mBtnReverb.setBackground(null);

            mLayoutVolume.setVisibility(View.GONE);
            mLayoutReverb.setVisibility(View.GONE);
            mLayoutVoiceChange.setVisibility(View.VISIBLE);
        } else if (i == R.id.btn_reverb) {
            //混响
            mBtnReverb.setBackgroundResource(R.drawable.ic_ugc_soundeffect);
            mBtnVoiceChange.setBackground(null);
            mBtnVolume.setBackground(null);

            mLayoutVolume.setVisibility(View.GONE);
            mLayoutReverb.setVisibility(View.VISIBLE);
            mLayoutVoiceChange.setVisibility(View.GONE);
        } else if (i == R.id.btn_reverb_default) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_0);
            }

        } else if (i == R.id.btn_reverb_1) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_1);
            }

        } else if (i == R.id.btn_reverb_2) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_2);
            }

        } else if (i == R.id.btn_reverb_3) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_3);
            }

        } else if (i == R.id.btn_reverb_4) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_4);
            }

        } else if (i == R.id.btn_reverb_5) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_5);
            }

        } else if (i == R.id.btn_reverb_6) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickReverb(TXRecordCommon.VIDOE_REVERB_TYPE_6);
            }

        } else if (i == R.id.btn_voicechanger_default) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_0);
            }

        } else if (i == R.id.btn_voicechanger_1) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_1);
            }

        } else if (i == R.id.btn_voicechanger_2) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_2);
            }

        } else if (i == R.id.btn_voicechanger_3) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_3);
            }

        } else if (i == R.id.btn_voicechanger_4) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_4);
            }

        } else if (i == R.id.btn_voicechanger_6) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_6);
            }

        } else if (i == R.id.btn_voicechanger_7) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_7);
            }

        } else if (i == R.id.btn_voicechanger_8) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_8);
            }

        } else if (i == R.id.btn_voicechanger_9) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_9);
            }

        } else if (i == R.id.btn_voicechanger_10) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_10);
            }

        } else if (i == R.id.btn_voicechanger_11) {
            if (mSoundEffectsSettingPannelListener != null) {
                mSoundEffectsSettingPannelListener.onClickVoiceChanger(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_11);
            }

        }

        if (v.getId() != mLastReverbIndex &&
                (v.getId() == R.id.btn_reverb_default || v.getId() == R.id.btn_reverb_1 ||
                        v.getId() == R.id.btn_reverb_2 || v.getId() == R.id.btn_reverb_3 ||
                        v.getId() == R.id.btn_reverb_4 || v.getId() == R.id.btn_reverb_5 ||
                        v.getId() == R.id.btn_reverb_6)) {   // 混响
            v.setSelected(true);

            View lastV = findViewById(mLastReverbIndex);
            if (null != lastV) {
                lastV.setSelected(false);
            }

            mLastReverbIndex = v.getId();

        } else if (v.getId() != mLastVoiceChangerIndex &&
                (v.getId() == R.id.btn_voicechanger_default || v.getId() == R.id.btn_voicechanger_1 || v.getId() == R.id.btn_voicechanger_2
                        || v.getId() == R.id.btn_voicechanger_3 || v.getId() == R.id.btn_voicechanger_4
                        || v.getId() == R.id.btn_voicechanger_6 || v.getId() == R.id.btn_voicechanger_7
                        || v.getId() == R.id.btn_voicechanger_8 || v.getId() == R.id.btn_voicechanger_9
                        || v.getId() == R.id.btn_voicechanger_10 || v.getId() == R.id.btn_voicechanger_11)) {  // 变声

            v.setSelected(true);

            View lastV = findViewById(mLastVoiceChangerIndex);
            if (null != lastV) {
                lastV.setSelected(false);
            }

            mLastVoiceChangerIndex = v.getId();
        }
    }

    private void setDefaultRevertAndVoiceChange() {
        TextView btnReverbDefalult = (TextView) findViewById(R.id.btn_reverb_default);
        btnReverbDefalult.setSelected(true);
        mLastReverbIndex = R.id.btn_reverb_default;

        TextView btnVoiceChangerDefault = (TextView) findViewById(R.id.btn_voicechanger_default);
        btnVoiceChangerDefault.setSelected(true);
        mLastVoiceChangerIndex = R.id.btn_voicechanger_default;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public void setSeekbarColor(int color) {

    }

    @Override
    public void setCheckedTextColor(int color) {

    }

    @Override
    public void setNormalTextColor(int color) {

    }

    @Override
    public void setConfirmButtonBackgroundColor(int color) {

    }

    @Override
    public void setConfirmButtonTextColor(int color) {

    }

    @Override
    public void setConfirmButtonTextSize(int size) {

    }

}

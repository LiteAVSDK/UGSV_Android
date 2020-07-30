package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.tencent.qcloud.ugckit.utils.DateTimeUtil;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordMusicPannel;
import com.tencent.qcloud.ugckit.component.slider.RangeSlider;


public class RecordMusicPannel extends RelativeLayout implements IRecordMusicPannel, SeekBar.OnSeekBarChangeListener, RangeSlider.OnRangeChangeListener, View.OnClickListener {
    private Context     mContext;
    private SeekBar     mSeekBarVolume;
    private RangeSlider mRangeSlider;
    private Button      mButtonConfirm;
    private TextView    mTextStartTime;
    private TextView    mTextMusicName;
    private ImageView   mImageLogo;
    private ImageView   mImageVoiceWave;
    private ImageView   mImageReplace;
    private ImageView   mImageDelete;

    private int  mMusicVolume = 100;
    private long mMusicDuration;

    private IRecordMusicPannel.MusicChangeListener mMusicChangeListener;

    public RecordMusicPannel(Context context) {
        super(context);
        init(context);
    }

    public RecordMusicPannel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecordMusicPannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.ugckit_layout_reocrd_music, this);
        mImageLogo = (ImageView) findViewById(R.id.iv_music_logo);
        mImageVoiceWave = (ImageView) findViewById(R.id.iv_voice_wave);
        mSeekBarVolume = (SeekBar) findViewById(R.id.seekbar_bgm_volume);
        mSeekBarVolume.setOnSeekBarChangeListener(this);

        mRangeSlider = (RangeSlider) findViewById(R.id.bgm_range_slider);
        mRangeSlider.setRangeChangeListener(this);

        mButtonConfirm = (Button) findViewById(R.id.btn_bgm_confirm);
        mButtonConfirm.setOnClickListener(this);
        mImageReplace = (ImageView) findViewById(R.id.btn_bgm_replace);
        mImageReplace.setOnClickListener(this);
        mImageDelete = (ImageView) findViewById(R.id.btn_bgm_delete);
        mImageDelete.setOnClickListener(this);

        mTextStartTime = (TextView) findViewById(R.id.tv_bgm_start_time);
        mTextStartTime.setText(String.format(getResources().getString(R.string.ugckit_bgm_start_position), "00:00"));

        mTextMusicName = (TextView) findViewById(R.id.tx_music_name);
        mTextMusicName.setText("");
        mTextMusicName.setSelected(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    private void setMusicName(String musicName) {
        mTextMusicName.setText(musicName);
    }

    @Override
    public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seekbar_bgm_volume) {
            mMusicVolume = progress;
            if (mMusicChangeListener != null) {
                mMusicChangeListener.onMusicVolumChanged(mMusicVolume / (float) 100);
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
    public void onKeyDown(int type) {

    }

    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        long leftTime = mMusicDuration * leftPinIndex / 100; //ms
        long rightTime = mMusicDuration * rightPinIndex / 100;

        if (mMusicChangeListener != null) {
            mMusicChangeListener.onMusicTimeChanged(leftTime, rightTime);
        }

        mTextStartTime.setText(String.format(getResources().getString(R.string.ugckit_bgm_start_position), DateTimeUtil.millsecondToMinuteSecond((int) leftTime)));
    }

    private void setCutRange(long startTime, long endTime) {
        if (mRangeSlider != null && mMusicDuration != 0) {
            int left = (int) (startTime * 100 / mMusicDuration);
            int right = (int) (endTime * 100 / mMusicDuration);
            mRangeSlider.setCutRange(left, right);
        }
        if (mTextStartTime != null) {
            mTextStartTime.setText(String.format(getResources().getString(R.string.ugckit_bgm_start_position), DateTimeUtil.millsecondToMinuteSecond((int) startTime)));
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        int i = v.getId();
        if (i == R.id.btn_bgm_confirm) {
            if (mMusicChangeListener != null) {
                mMusicChangeListener.onMusicSelect();
            }

        } else if (i == R.id.btn_bgm_replace) {
            if (mMusicChangeListener != null) {
                mMusicChangeListener.onMusicReplace();
            }

        } else if (i == R.id.btn_bgm_delete) {
            if (mMusicChangeListener != null) {
                mMusicChangeListener.onMusicDelete();
            }
        }
    }

    @Override
    public void setOnMusicChangeListener(IRecordMusicPannel.MusicChangeListener volumeChangeListener) {
        mMusicChangeListener = volumeChangeListener;
    }

    @Override
    public void setMusicInfo(@NonNull MusicInfo musicInfo) {
        setMusicName(musicInfo.name);
        mMusicDuration = musicInfo.duration;
        setCutRange(0, musicInfo.duration);
        mRangeSlider.resetRangePos();
    }

    @Override
    public void setMusicIconResource(int resid) {
        mImageLogo.setImageResource(resid);
    }

    @Override
    public void setMusicDeleteIconResource(int resid) {
        mImageDelete.setImageResource(resid);
    }

    @Override
    public void setMusicReplaceIconResource(int resid) {
        mImageReplace.setImageResource(resid);
    }

    @Override
    public void setVolumeSeekbarColor(int color) {
        mSeekBarVolume.setBackgroundColor(getResources().getColor(color));
    }

    @Override
    public void setMusicRangeColor(Drawable color) {
    }

    @Override
    public void setMusicRangeBackgroundResource(int resid) {
        mImageVoiceWave.setImageResource(resid);
    }

    @Override
    public void setConfirmButtonBackgroundColor(int color) {
        mButtonConfirm.setBackgroundColor(getResources().getColor(color));
    }

    @Override
    public void setConfirmButtonTextColor(int color) {
        mButtonConfirm.setTextColor(getResources().getColor(color));
    }

    @Override
    public void setConfirmButtonTextSize(int size) {
        mButtonConfirm.setTextSize(size);
    }
}

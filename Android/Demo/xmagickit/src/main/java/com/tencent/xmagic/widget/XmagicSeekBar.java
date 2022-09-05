package com.tencent.xmagic.widget;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * 系统 SeekBar 从 API 26 (Android 8.0) 才开始支持 setMin(int) 方法, 见 https://developer.android.com/reference/android/widget/ProgressBar#setMin(int)
 * 此类专门为此做了兼容处理, 请使用 {@link #setMyMin(int)}, {@link #setMyMax(int)}, {@link #setMyProgress(int, boolean)} 方法代替.
 */
public class XmagicSeekBar extends AppCompatSeekBar {

    private int mMin;
    private int mMax;

    public XmagicSeekBar(Context context) {
        super(context);
    }

    public XmagicSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XmagicSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public synchronized void setMyMin(int min) {
        mMin = min;
        setInnerMinMax();
    }

    public void setMyMax(int max) {
        mMax = max;
        setInnerMinMax();
    }

    public void setMyProgress(int progress, boolean animate) {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            super.setProgress(convertUIProgressToInnerProgress(progress), animate);
        } else {
            super.setProgress(convertUIProgressToInnerProgress(progress));
        }
    }

    @Override
    public void setOnSeekBarChangeListener(final OnSeekBarChangeListener changeListener) {
        if (changeListener == null) {
            super.setOnSeekBarChangeListener(null);
            return;
        }

        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeListener.onProgressChanged(seekBar, convertInnerProgressToUiProgress(progress), fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeListener.onStopTrackingTouch(seekBar);
            }
        });
    }

    private void setInnerMinMax() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            super.setMin(0);
        }
        super.setMax(mMax - mMin);
    }

    public int getMyProgress() {
        int innerProgress = super.getProgress();
        return convertInnerProgressToUiProgress(innerProgress);
    }

    private int convertInnerProgressToUiProgress(int innerProgress) {
        int innerMax = super.getMax();
        float progressPercent = innerProgress * 1f / innerMax;
        return (int) (mMin + progressPercent * (mMax - mMin));
    }

    private int convertUIProgressToInnerProgress(int uiProgress) {
        float progressPercent = (uiProgress - mMin) * 1f / (mMax - mMin);
        int innerMax = super.getMax();
        return (int) (progressPercent * innerMax);
    }
}

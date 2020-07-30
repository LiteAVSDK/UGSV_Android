package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.record.interfaces.IRecordProgressView;

import java.util.ArrayList;

/**
 * 录制进度条
 */
public class RecordProgressView extends View implements IRecordProgressView {
    private final String TAG = "RecordProgressView";

    private Paint               mRecordPaint;
    private Paint               mPendingPaint;
    private Paint               mSpacePaint;
    @Nullable
    private Handler             mHandler;
    private ArrayList<ClipInfo> mClipInfoList;
    private ClipInfo            mCurClipInfo;

    private int     mMaxDuration;
    private int     mMinDuration;
    private int     mLastTotalDuration;
    public int      mNormalColor;           // 已经录制的视频进度条颜色
    public int      mDeleteColor;           // 删除上一段选中的进度条颜色
    public int      mBackgroundColor;       // 进度条背景颜色
    public int      mSpaceColor;            // 多段录制间隔颜色

    private boolean isPending;
    private boolean isCursorShow = false;
    private boolean isInProgress = false;

    public RecordProgressView(Context context) {
        super(context);
        init();
    }

    public RecordProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRecordPaint = new Paint();
        mPendingPaint = new Paint();
        mSpacePaint = new Paint();

        mRecordPaint.setAntiAlias(true);
        mPendingPaint.setAntiAlias(true);
        mSpacePaint.setAntiAlias(true);

        mBackgroundColor = getResources().getColor(R.color.ugckit_record_progress_bg);
        mNormalColor = getResources().getColor(R.color.ugckit_record_progress);
        mDeleteColor = getResources().getColor(R.color.ugckit_record_progress_pending);
        mSpaceColor = getResources().getColor(R.color.ugckit_white);

        mRecordPaint.setColor(mNormalColor);
        mPendingPaint.setColor(mDeleteColor);
        mSpacePaint.setColor(mSpaceColor);

        mClipInfoList = new ArrayList<ClipInfo>();
        mCurClipInfo = new ClipInfo();
        isPending = false;

        mHandler = new Handler();
        startCursorBling();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBackgroundColor);

        int lastTotalProgress = 0;
        float totalWidth = 0;
        for (ClipInfo clipInfo : mClipInfoList) {
            float newWidth = (lastTotalProgress + clipInfo.progress) / (float) mMaxDuration * getWidth();
            switch (clipInfo.clipType) {
                case ClipInfo.CLIP_TYPE_SPACE:
                    canvas.drawRect(totalWidth - getResources().getDimension(R.dimen.ugckit_progress_divider), 0f, newWidth, getHeight(), mSpacePaint);
                    break;
                case ClipInfo.CLIP_TYPE_PROGRESS:
                    canvas.drawRect(totalWidth, 0f, newWidth, getHeight(), mRecordPaint);
                    break;
                case ClipInfo.CLIP_TYPE_PENDING:
                    canvas.drawRect(totalWidth, 0f, newWidth, getHeight(), mPendingPaint);
                    break;
            }
            lastTotalProgress += clipInfo.progress;
            totalWidth = newWidth;
        }
        if (mCurClipInfo != null && mCurClipInfo.progress != 0) {
            canvas.drawRect(totalWidth, 0f, totalWidth + mCurClipInfo.progress / (float) mMaxDuration * getWidth(), getHeight(), mRecordPaint);
            totalWidth = totalWidth + mCurClipInfo.progress / (float) mMaxDuration * getWidth();
        }
        if (lastTotalProgress + mCurClipInfo.progress < mMinDuration) {
            canvas.drawRect(mMinDuration / (float) mMaxDuration * getWidth(), 0f,
                    mMinDuration / (float) mMaxDuration * getWidth() + getResources().getDimension(R.dimen.ugckit_progress_min_pos), getHeight(), mSpacePaint);
        }
        if (isCursorShow || isInProgress) {
            canvas.drawRect(totalWidth, 0f, totalWidth + getResources().getDimension(R.dimen.ugckit_progress_cursor), getHeight(), mSpacePaint);
        }
    }

    @Override
    public void setNormalColor(@ColorInt int color) {
        mNormalColor = color;
        mRecordPaint.setColor(mNormalColor);
    }

    @Override
    public void setDeleteColor(@ColorInt int color) {
        mDeleteColor = color;
        mPendingPaint.setColor(mDeleteColor);
    }

    @Override
    public void setSpaceColor(@ColorInt int color) {
        mSpaceColor = color;
        mSpacePaint.setColor(mSpaceColor);
    }

    private class ClipInfo {
        public static final int CLIP_TYPE_PROGRESS = 1;
        public static final int CLIP_TYPE_PENDING = 2;
        public static final int CLIP_TYPE_SPACE = 3;

        public int progress;
        public int clipType;
    }

    @Override
    public void setMaxDuration(int maxDuration) {
        this.mMaxDuration = maxDuration;
    }

    @Override
    public void setMinDuration(int minDuration) {
        this.mMinDuration = minDuration;
    }

    @Override
    public void setProgress(int progress) {
        isInProgress = true;
        stopCursorBling();
        if (isPending) {
            for (ClipInfo clipInfo : mClipInfoList) {
                if (clipInfo.clipType == ClipInfo.CLIP_TYPE_PENDING) {
                    clipInfo.clipType = ClipInfo.CLIP_TYPE_PROGRESS;
                    isPending = false;
                    break;
                }
            }
        }
        this.mCurClipInfo.clipType = ClipInfo.CLIP_TYPE_PROGRESS;
        this.mCurClipInfo.progress = progress - mLastTotalDuration;
        invalidate();
    }

    public void clipComplete() {
        isInProgress = false;

        mLastTotalDuration = mLastTotalDuration + mCurClipInfo.progress;

        mClipInfoList.add(mCurClipInfo);
        ClipInfo clipInfo = new ClipInfo();
        clipInfo.clipType = ClipInfo.CLIP_TYPE_SPACE;
        clipInfo.progress = 0;
        mClipInfoList.add(clipInfo);
        mCurClipInfo = new ClipInfo();

        startCursorBling();
        invalidate();
    }

    @Override
    public void selectLast() {
        if (mClipInfoList.size() >= 2) {
            ClipInfo clipInfo = mClipInfoList.get(mClipInfoList.size() - 2);
            clipInfo.clipType = ClipInfo.CLIP_TYPE_PENDING;
            isPending = true;
            invalidate();
        }
    }

    @Override
    public void deleteLast() {
        if (mClipInfoList.size() >= 2) {
            mClipInfoList.remove(mClipInfoList.size() - 1);
            ClipInfo clipInfo = mClipInfoList.remove(mClipInfoList.size() - 1);
            mLastTotalDuration = mLastTotalDuration - clipInfo.progress;
        }
        invalidate();
    }

    @NonNull
    private Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            isCursorShow = !isCursorShow;
            mHandler.postDelayed(cursorRunnable, 500);
            invalidate();
        }
    };

    private void startCursorBling() {
        if (mHandler != null) {
            mHandler.postDelayed(cursorRunnable, 500);
        }
    }

    private void stopCursorBling() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

}

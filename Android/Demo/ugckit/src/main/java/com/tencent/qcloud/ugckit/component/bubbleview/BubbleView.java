package com.tencent.qcloud.ugckit.component.bubbleview;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.tencent.qcloud.ugckit.component.floatlayer.FloatLayerView;

/**
 * 气泡字幕的View
 * <p>
 * 根绝参数 初始化气泡字幕
 */
public class BubbleView extends FloatLayerView {
    private static final String TAG = "BubbleView";

    private long mStartTime;
    private long mEndTime;

    @Nullable
    private BubbleViewParams mBubbleViewParams;

    public BubbleView(Context context) {
        super(context);
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBubbleParams(@Nullable BubbleViewParams params) {
        mBubbleViewParams = params;
        if (params == null) {
            return;
        }
        if (params.text == null) {
            params.text = "";
            Log.w(TAG, "setBubbleParams: bubble text is null");
        }
        BubbleViewHelper helper = new BubbleViewHelper();
        helper.setBubbleTextParams(params);
        Bitmap bitmap = helper.createBubbleTextBitmap();
        setImageBitamp(bitmap);
        mBubbleViewParams.bubbleBitmap = null;
        invalidate();
    }

    @Nullable
    public BubbleViewParams getBubbleParams() {
        return mBubbleViewParams;
    }

    public void setStartToEndTime(long startTime, long endTime) {
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }


}
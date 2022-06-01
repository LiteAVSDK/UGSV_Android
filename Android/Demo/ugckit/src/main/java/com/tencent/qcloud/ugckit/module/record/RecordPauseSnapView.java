package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.Nullable;

import android.util.AttributeSet;

import com.tencent.qcloud.ugckit.utils.BackgroundTasks;
import com.tencent.ugc.TXRecordCommon;

/**
 * 短视频录制暂留画面
 */
public class RecordPauseSnapView extends androidx.appcompat.widget.AppCompatImageView {

    //暂留图片
    private Bitmap mSnapImageBitmap;
    //透明度
    private float mOpacityRate = 0.7f;

    private final Paint mDefaultPaint = new Paint();
    //图片宽高比
    private float mImageSizeRate = 1f;

    private final RectF mImageDrawRect = new RectF();


    public RecordPauseSnapView(Context context) {
        this(context, null);
    }

    public RecordPauseSnapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordPauseSnapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDefaultPaint.setStyle(Paint.Style.FILL);
        setSnapViewAlpha(mOpacityRate);
        setClickable(false);
        setFocusable(false);
    }

    public void setSnapViewAlpha(float alpha) {
        this.mOpacityRate = alpha;
        mDefaultPaint.setAlpha((int) (alpha * 255));
    }

    public float getSnapViewAlpha() {
        return mOpacityRate;
    }

    /**
     * 暂停状态下的暂留画面：抓取当前视频截图，并以mPauseSnapOpacity透明度显示在播放器上
     */
    public void catchPauseImage() {
        setSnapViewAlpha(UGCKitRecordConfig.getInstance().mPauseSnapOpacity);
        //不使用takePhoto方法，takePhoto方法会把当前照片存储到本地，该功能暂不需要存储
        if (VideoRecordSDK.getInstance().getRecorder() != null) {
            VideoRecordSDK.getInstance().getRecorder().snapshot(new TXRecordCommon.ITXSnapshotListener() {
                @Override
                public void onSnapshot(final Bitmap bitmap) {
                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSnapImageBitmap(bitmap);
                            calcSnapImageSize();
                        }
                    });
                }
            });
        }
    }

    private void calcSnapImageSize() {
        int bitmapWidth = mSnapImageBitmap.getWidth();
        int bitmapHeight = mSnapImageBitmap.getHeight();

        mImageSizeRate = bitmapHeight / (float) bitmapWidth;
    }

    public void setSnapImageBitmap(Bitmap bitmap) {
        this.mSnapImageBitmap = bitmap;
        if (null != bitmap) {
            setVisibility(VISIBLE);
        }
        invalidate();
    }

    public Bitmap getSnapImageBitmap() {
        return mSnapImageBitmap;
    }

    public void clearBitmap() {
        if (null != getSnapImageBitmap()) {
            setSnapImageBitmap(null);
        }
        if (getVisibility() == VISIBLE) {
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        if (null != mSnapImageBitmap) {

            float drawHeight = width * mImageSizeRate;

            final float top = (height / 2f) - (drawHeight / 2);
            final float bottom = (height / 2f) + (drawHeight / 2);

            mImageDrawRect.left = 0;
            mImageDrawRect.top = top;
            mImageDrawRect.right = width;
            mImageDrawRect.bottom = bottom;

            canvas.drawBitmap(mSnapImageBitmap, null,
                    mImageDrawRect, mDefaultPaint);

        }
        canvas.restore();
    }
}

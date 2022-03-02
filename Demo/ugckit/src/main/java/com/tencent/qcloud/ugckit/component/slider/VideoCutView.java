package com.tencent.qcloud.ugckit.component.slider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.qcloud.ugckit.module.effect.time.TCVideoEditerAdapter;
import com.tencent.qcloud.ugckit.module.effect.utils.Edit;
import com.tencent.qcloud.ugckit.R;
import com.tencent.ugc.TXVideoEditConstants;

/**
 * 裁剪View
 */
public class VideoCutView extends RelativeLayout implements RangeSlider.OnRangeChangeListener {

    @NonNull
    private String                   TAG        = "VideoCutView";
    private Context                  mContext;
    private RecyclerView             mRecyclerView;
    private RangeSlider              mRangeSlider;
    private float                    mCurrentScroll;
    /**
     * 单个缩略图的宽度
     */
    private int                      mSingleWidth;
    /**
     * 所有缩略图的宽度
     */
    private int                      mAllWidth;
    /**
     * 整个视频的时长
     */
    private long                     mVideoDuration;
    /**
     * 控件最大时长16s
     */
    private long                     mViewMaxDuration;
    /**
     * 如果视频时长超过了控件的最大时长，底部在滑动时最左边的起始位置时间
     */
    private long                     mStartTime = 0;
    /**
     * 裁剪的起始时间，最左边是0
     */
    private int                      mViewLeftTime;
    /**
     * 裁剪的结束时间，最右边最大是16000ms
     */
    private int                      mViewRightTime;
    /**
     * 最终视频的起始时间
     */
    private long                     mVideoStartPos;
    /**
     * 最终视频的结束时间
     */
    private long                     mVideoEndPos;
    private TCVideoEditerAdapter     mAdapter;
    private Edit.OnCutChangeListener mRangeChangeListener;

    public VideoCutView(Context context) {
        super(context);

        init(context);
    }

    public VideoCutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public VideoCutView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        inflate(getContext(), R.layout.ugckit_item_edit_view, this);

        mRangeSlider = (RangeSlider) findViewById(R.id.range_slider);
        mRangeSlider.setRangeChangeListener(this);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mAdapter = new TCVideoEditerAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);

        mSingleWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.ugckit_item_thumb_height);
    }

    /**
     * 设置缩略图个数
     *
     * @param count
     */
    public void setCount(int count) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = count * mSingleWidth;
        mAllWidth = width;
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        if (width > screenWidth) {
            width = screenWidth;
        }
        layoutParams.width = width + 2 * resources.getDimensionPixelOffset(R.dimen.ugckit_cut_margin);
        setLayoutParams(layoutParams);
    }

    /**
     * 设置裁剪Listener
     *
     * @param listener
     */
    public void setCutChangeListener(Edit.OnCutChangeListener listener) {
        mRangeChangeListener = listener;
    }

    public void setMediaFileInfo(@Nullable TXVideoEditConstants.TXVideoInfo videoInfo) {
        if (videoInfo == null) {
            return;
        }
        mVideoDuration = videoInfo.duration;

        if (mVideoDuration >= 16000) {
            mViewMaxDuration = 16000;
        } else {
            mViewMaxDuration = mVideoDuration;
        }

        mViewLeftTime = 0;
        mViewRightTime = (int) mViewMaxDuration;

        mVideoStartPos = 0;
        mVideoEndPos = mViewMaxDuration;
    }

    public void addBitmap(int index, Bitmap bitmap) {
        mAdapter.add(index, bitmap);
    }

    public void clearAllBitmap() {
        mAdapter.clearAllBitmap();
    }

    @Override
    public void onKeyDown(int type) {
        if (mRangeChangeListener != null) {
            mRangeChangeListener.onCutChangeKeyDown();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null) {
            Log.i(TAG, "onDetachedFromWindow");
            mAdapter.clearAllBitmap();
        }
    }

    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        mViewLeftTime = (int) (mViewMaxDuration * leftPinIndex / 100); //ms
        mViewRightTime = (int) (mViewMaxDuration * rightPinIndex / 100);

        onTimeChanged();
    }

    private void onTimeChanged() {
        mVideoStartPos = mStartTime + mViewLeftTime;
        mVideoEndPos = mStartTime + mViewRightTime;

        if (mRangeChangeListener != null) {
            mRangeChangeListener.onCutChangeKeyUp((int) mVideoStartPos, (int) mVideoEndPos, 0);
        }
    }

    @NonNull
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.i(TAG, "onScrollStateChanged, new state = " + newState);

            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    onTimeChanged();
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    if (mRangeChangeListener != null) {
                        mRangeChangeListener.onCutChangeKeyDown();
                    }
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:

                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            mCurrentScroll = mCurrentScroll + dx;
            float rate = mCurrentScroll / mAllWidth;
            if (mCurrentScroll + mRecyclerView.getWidth() >= mAllWidth) {
                mStartTime = mVideoDuration - mViewMaxDuration;
            } else {
                mStartTime = (int) (rate * mVideoDuration);
            }
        }
    };

    public RangeSlider getRangeSlider() {
        return mRangeSlider;
    }

}

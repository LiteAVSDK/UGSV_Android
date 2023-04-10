package com.tencent.qcloud.ugckit.component.timeline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.ugc.TXVideoEditer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoProgressController {
    private final String TAG = "VideoProgressController";

    private VideoProgressView mVideoProgressView;
    private RecyclerView      mRecyclerView;
    @Nullable
    private ColorfulProgress  mColorfulProgress;

    private boolean                        mIsTouching;
    private boolean                        mIsRangeSliderChanged;
    private int                            mThumbnailNum;
    private int                            mScrollState;
    private long                           mCurrentTimeMs;
    private float                          mCurrentScroll;
    private float                          mThumbnailPicListDisplayWidth; // 视频缩略图列表的宽度
    private float                          mVideoProgressDisplayWidth; // 视频进度条可显示宽度
    private VideoProgressSeekListener      mVideoProgressSeekListener;
    private HashMap<Integer, List>         mRangeSliderViewContainerHashmap; // 分类的范围块view
    private List<RangeSliderViewContainer> mRangeSliderViewContainerList; // 所有的范围块view
    private List<SliderViewContainer>      mSliderViewContainerList;

    public VideoProgressController() {
    }

    public void setVideoProgressDisplayWidth(int width) {
        mVideoProgressDisplayWidth = width;
    }

    public void addRangeSliderView(@Nullable final RangeSliderViewContainer rangeSliderView) {
        if (rangeSliderView == null) {
            Log.e(TAG, "addRangeSliderView, rangeSliderView is null !");
            return;
        }
        if (mRangeSliderViewContainerList == null) {
            mRangeSliderViewContainerList = new ArrayList<>();
        }

        mRangeSliderViewContainerList.add(rangeSliderView);
        mVideoProgressView.getParentView().addView(rangeSliderView);
        rangeSliderView.post(new Runnable() {
            @Override
            public void run() {
                rangeSliderView.changeStartViewLayoutParams();
            }
        });
    }

    public void addRangeSliderView(int type, @Nullable final RangeSliderViewContainer rangeSliderView) {
        if (rangeSliderView == null) {
            Log.e(TAG, "addRangeSliderView, rangeSliderView is null !");
            return;
        }
        if (mRangeSliderViewContainerList == null) {
            mRangeSliderViewContainerList = new ArrayList<>();
        }
        if (mRangeSliderViewContainerHashmap == null) {
            mRangeSliderViewContainerHashmap = new HashMap<>();
        }
        List<RangeSliderViewContainer> rangeSliderViewContainerList = mRangeSliderViewContainerHashmap.get(type);
        if (rangeSliderViewContainerList == null) {
            rangeSliderViewContainerList = new ArrayList<>();
        }
        rangeSliderViewContainerList.add(rangeSliderView);
        mRangeSliderViewContainerHashmap.put(type, rangeSliderViewContainerList);

        mRangeSliderViewContainerList.add(rangeSliderView);
        mVideoProgressView.getParentView().addView(rangeSliderView);
        rangeSliderView.post(new Runnable() {
            @Override
            public void run() {
                rangeSliderView.changeStartViewLayoutParams();
            }
        });
    }

    public boolean removeRangeSliderView(RangeSliderViewContainer rangeSliderView) {
        if (mVideoProgressView == null) {
            Log.e(TAG, "removeRangeSliderView, mVideoProgressView is null");
            return false;
        }
        mVideoProgressView.getParentView().removeView(rangeSliderView);
        if (mRangeSliderViewContainerList == null || mRangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeRangeSliderView, mRangeSliderViewContainerList is empty");
            return false;
        }
        return mRangeSliderViewContainerList.remove(rangeSliderView);
    }

    @Nullable
    public View removeRangeSliderView(int index) {
        if (mVideoProgressView == null) {
            Log.e(TAG, "removeRangeSliderView(index), mVideoProgressView is null");
            return null;
        }
        if (mRangeSliderViewContainerList == null || mRangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeRangeSliderView(index), mRangeSliderViewContainerList is empty");
            return null;
        }
        if (index > mRangeSliderViewContainerList.size() - 1) {
            Log.e(TAG, "removeRangeSliderView(index), index out of bounds");
            return null;
        }
        RangeSliderViewContainer view = mRangeSliderViewContainerList.remove(index);
        mVideoProgressView.getParentView().removeView(view);
        return view;
    }

    @Nullable
    public View removeRangeSliderView(int type, int index) {
        if (mVideoProgressView == null) {
            Log.e(TAG, "removeRangeSliderView(type, index), mVideoProgressView is null");
            return null;
        }
        if (mRangeSliderViewContainerList == null || mRangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeRangeSliderView(type, index), mRangeSliderViewContainerList is empty");
            return null;
        }
        List<RangeSliderViewContainer> rangeSliderViewContainerList = mRangeSliderViewContainerHashmap.get(type);
        if (rangeSliderViewContainerList == null || rangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeRangeSliderView(type, index), rangeSliderViewContainerList is empty");
            return null;
        }
        RangeSliderViewContainer view = rangeSliderViewContainerList.remove(index);
        mRangeSliderViewContainerList.remove(view);
        mVideoProgressView.getParentView().removeView(view);
        return view;
    }

    @Nullable
    public RangeSliderViewContainer getRangeSliderView(int index) {
        if (index < 0) {
            return null;
        }
        if (mRangeSliderViewContainerList != null && index < mRangeSliderViewContainerList.size() && index >= 0) {
            return mRangeSliderViewContainerList.get(index);
        }
        return null;
    }

    @Nullable
    public RangeSliderViewContainer getRangeSliderView(int type, int index) {
        if (index < 0) {
            return null;
        }
        if (mRangeSliderViewContainerHashmap == null) {
            Log.e(TAG, "getRangeSliderView(type, index), mRangeSliderViewContainerHashmap is null");
            return null;
        }
        List<RangeSliderViewContainer> rangeSliderViewContainerList = mRangeSliderViewContainerHashmap.get(type);
        if (rangeSliderViewContainerList == null || rangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "getRangeSliderView(type, index), rangeSliderViewContainer is empty");
            return null;
        }
        RangeSliderViewContainer view = rangeSliderViewContainerList.get(index);
        return view;
    }

    public void showAllRangeSliderView(int type, boolean isShow) {
        if (mRangeSliderViewContainerHashmap == null) {
            Log.e(TAG, "showAllRangeSliderView(type), mRangeSliderViewContainerHashmap is null");
            return;
        }
        List<RangeSliderViewContainer> rangeSliderViewContainerList = mRangeSliderViewContainerHashmap.get(type);
        if (rangeSliderViewContainerList == null || rangeSliderViewContainerList.size() == 0) {
            Log.e(TAG, "showAllRangeSliderView(type), rangeSliderViewContainer is empty");
            return;
        }

        for (RangeSliderViewContainer rangeSliderViewContainer : rangeSliderViewContainerList) {
            if (isShow) {
                rangeSliderViewContainer.setVisibility(View.VISIBLE);
            } else {
                rangeSliderViewContainer.setVisibility(View.GONE);
            }
        }
    }

    public void addColorfulProgress(@Nullable ColorfulProgress colorfulProgress) {
        if (colorfulProgress == null) {
            Log.e(TAG, "addColorfulProgress, colorfulProgress is null !");
            return;
        }
        colorfulProgress.setVideoProgressController(this);
        mColorfulProgress = colorfulProgress;
        mVideoProgressView.getParentView().addView(colorfulProgress);
        mColorfulProgress.post(new Runnable() {
            @Override
            public void run() {
                changeColorfulProgressOffset();
            }
        });
    }

    private void changeColorfulProgressOffset() {
        if (mColorfulProgress == null) {
            return;
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mColorfulProgress.getLayoutParams();
        layoutParams.leftMargin = calculateColorfulProgressOffset();
        mColorfulProgress.requestLayout();
    }

    public void removeColorfulProgress() {
        if (mColorfulProgress != null) {
            mVideoProgressView.getParentView().removeView(mColorfulProgress);
        }
    }

    public void addSliderView(@Nullable final SliderViewContainer sliderViewContainer) {
        if (sliderViewContainer == null) {
            return;
        }
        if (mSliderViewContainerList == null) {
            mSliderViewContainerList = new ArrayList<>();
        }
        mSliderViewContainerList.add(sliderViewContainer);
        sliderViewContainer.setVideoProgressControlloer(this);
        mVideoProgressView.getParentView().addView(sliderViewContainer);
        sliderViewContainer.post(new Runnable() {
            @Override
            public void run() {
                sliderViewContainer.changeLayoutParams();
            }
        });
    }

    public boolean removeSliderView(SliderViewContainer sliderViewContainer) {
        if (mVideoProgressView == null) {
            Log.e(TAG, "removeSliderView, mVideoProgressView is null");
            return false;
        }
        mVideoProgressView.getParentView().removeView(sliderViewContainer);
        if (mSliderViewContainerList == null || mSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeSliderView, mSliderViewContainerList is empty");
            return false;
        }
        return mSliderViewContainerList.remove(sliderViewContainer);
    }

    @Nullable
    public View removeSliderView(int index) {
        if (mVideoProgressView == null) {
            Log.e(TAG, "removeSliderView(index), mVideoProgressView is null");
            return null;
        }
        if (mSliderViewContainerList == null || mSliderViewContainerList.size() == 0) {
            Log.e(TAG, "removeSliderView(index), mSliderViewContainerList is empty");
            return null;
        }
        if (index > mSliderViewContainerList.size() - 1) {
            Log.e(TAG, "removeSliderView(int index), index out of bounds");
            return null;
        }
        SliderViewContainer sliderViewContainer = mSliderViewContainerList.get(index);
        mVideoProgressView.getParentView().removeView(sliderViewContainer);
        return sliderViewContainer;
    }

    int calculateStartViewPosition(@NonNull RangeSliderViewContainer rangeSliderView) {
        return (int) (mVideoProgressDisplayWidth / 2 - rangeSliderView.getStartView().getMeasuredWidth()
                + duration2Distance(rangeSliderView.getStartTimeUs()) - mCurrentScroll);
    }

    int calculateSliderViewPosition(@NonNull SliderViewContainer sliderViewContainer) {
        return (int) (mVideoProgressDisplayWidth / 2 + duration2Distance(sliderViewContainer.getStartTimeMs()) - mCurrentScroll);
    }

    int calculateColorfulProgressOffset() {
        return (int) (mVideoProgressDisplayWidth / 2 - mCurrentScroll);
    }

    public int duration2Distance(long durationMs) {
        float rate = durationMs * 1.0f / VideoEditerSDK.getInstance().getVideoPlayDuration();
        return (int) (getThumbnailPicListDisplayWidth() * rate);
    }

    public int calculateSliderWidth(long durationMs) {
        int resultWidth;
        float thumbnailPicDisplayWidth = getThumbnailPicListDisplayWidth();
        //最小宽度不能低于0.1秒宽度
        float durationSec = durationMs < 100 ? 0.1F : durationMs / 1000F;
        long totalDuration = VideoEditerSDK.getInstance().getVideoPlayDuration();
        int durationMinWidth;
        if (totalDuration > 1) {
            durationMinWidth = (int) (thumbnailPicDisplayWidth / (totalDuration / 1000));
        } else {
            durationMinWidth = (int) (mVideoProgressDisplayWidth / 16);
        }
        float scale = durationMs / (totalDuration * 1f);
        int resultWidth2 = (int) (scale * thumbnailPicDisplayWidth);

        resultWidth = (int) (durationMinWidth * durationSec);
        //如果最终计算出来的宽度大于整个视频时间轴上的宽度，那么将宽度置为视频轴的宽度
        if (resultWidth > thumbnailPicDisplayWidth) {
            resultWidth = (int) thumbnailPicDisplayWidth;
        }
        resultWidth2 = (int) Math.min(resultWidth2, thumbnailPicDisplayWidth);
        return resultWidth2;
    }

    long distance2Duration(float distance) {
        long totalDuration = VideoEditerSDK.getInstance().getVideoPlayDuration();
        float rate = distance / getThumbnailPicListDisplayWidth();
        return (long) (totalDuration * rate);
    }

    public void setVideoProgressSeekListener(VideoProgressSeekListener videoProgressSeekListener) {
        mVideoProgressSeekListener = videoProgressSeekListener;
    }

    public void setVideoProgressView(VideoProgressView videoProgressView) {
        mVideoProgressView = videoProgressView;
        mRecyclerView = mVideoProgressView.getRecyclerView();
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, @NonNull MotionEvent motionEvent) {
                int eventId = motionEvent.getAction();
                switch (eventId) {
                    case MotionEvent.ACTION_DOWN:
                        mIsTouching = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mIsTouching = false;
                        break;
                }
                return false;
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.i(TAG, "onScrollStateChanged, new state = " + newState);

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Log.i(TAG, "onScrollStateChanged, state idle, mCurrentTimeMs = " + mCurrentTimeMs);

                        if (mVideoProgressSeekListener != null) {
                            mVideoProgressSeekListener.onVideoProgressSeekFinish(mCurrentTimeMs);
                        }
                        if (mRangeSliderViewContainerList != null && mRangeSliderViewContainerList.size() > 0) {
                            for (RangeSliderViewContainer rangeSliderView : mRangeSliderViewContainerList) {
                                rangeSliderView.changeStartViewLayoutParams();
                            }
                        }

                        if (mColorfulProgress != null) {
                            mColorfulProgress.setCurPosition(mCurrentScroll);
                            changeColorfulProgressOffset();
                        }

                        if (mSliderViewContainerList != null && mSliderViewContainerList.size() > 0) {
                            for (SliderViewContainer sliderViewContainer : mSliderViewContainerList) {
                                sliderViewContainer.changeLayoutParams();
                            }
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
                mScrollState = newState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mCurrentScroll = mCurrentScroll + dx;
                float rate = mCurrentScroll / getThumbnailPicListDisplayWidth();
                long totalDuration = VideoEditerSDK.getInstance().getVideoPlayDuration();
                long currentTimeUs = (long) (rate * totalDuration);

                if (mIsTouching || mIsRangeSliderChanged || mScrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                    mIsRangeSliderChanged = false; // 由于范围改变引起的，回调给界面后保证能单帧预览，之后马上重置
                    if (mVideoProgressSeekListener != null) {
                        mVideoProgressSeekListener.onVideoProgressSeek(currentTimeUs);
                    }
                }
                mCurrentTimeMs = currentTimeUs;
                if (mRangeSliderViewContainerList != null && mRangeSliderViewContainerList.size() > 0) {
                    for (RangeSliderViewContainer rangeSliderView : mRangeSliderViewContainerList) {
                        rangeSliderView.changeStartViewLayoutParams();
                    }
                }

                if (mColorfulProgress != null) {
                    mColorfulProgress.setCurPosition(mCurrentScroll);
                    changeColorfulProgressOffset();
                }
                if (mSliderViewContainerList != null && mSliderViewContainerList.size() > 0) {
                    for (SliderViewContainer sliderViewContainer : mSliderViewContainerList) {
                        sliderViewContainer.changeLayoutParams();
                    }
                }
            }
        });
    }

    /**
     * 当前时间ms
     *
     * @param currentTimeMs
     */
    public void setCurrentTimeMs(long currentTimeMs) {
        mCurrentTimeMs = currentTimeMs;
        long totalDuration = VideoEditerSDK.getInstance().getVideoPlayDuration();
        float rate = (float) mCurrentTimeMs / totalDuration;
        float scrollBy = rate * getThumbnailPicListDisplayWidth() - mCurrentScroll;
        mRecyclerView.scrollBy((int) scrollBy, 0);
    }

    public long getCurrentTimeMs() {
        return mCurrentTimeMs;
    }

    public void setIsRangeSliderChanged(boolean isRangeSliderChanged) {
        mIsRangeSliderChanged = isRangeSliderChanged;
    }

    /**
     * 获取缩略图列表的长度，需要在设置完数据之后调用，否则返回0
     *
     * @return
     */
    public float getThumbnailPicListDisplayWidth() {
        mThumbnailNum = mVideoProgressView.getThumbnailCount();
        float thumbnailWidth = mThumbnailNum * mVideoProgressView.getSingleThumbnailWidth();
//        if (mSliderViewContainerList != null && mSliderViewContainerList.size() > 0) {
//            for (SliderViewContainer sliderViewContainer : mSliderViewContainerList) {
//                sliderViewContainer.changeLayoutParams();
//            }
//        }
        mThumbnailPicListDisplayWidth = thumbnailWidth;
        return mThumbnailPicListDisplayWidth;
    }

    public interface VideoProgressSeekListener {
        void onVideoProgressSeek(long currentTimeMs);

        void onVideoProgressSeekFinish(long currentTimeMs);
    }

}

package com.tencent.qcloud.ugckit.module.effect;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tencent.qcloud.ugckit.UGCKitImpl;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutLayout;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 由于SDK提供的TXVideoEditer为非单例模式
 * 当您需要在多个Activity\Fragment 之间对同一个Video进行编辑的时候，可以在上层将其包装为一个单例
 * <p>
 * 需要注意：
 * 完成一次视频编辑后，请务必调用{@link VideoEditerSDK#clear()}晴空相关的一些配置
 */
public class VideoEditerSDK {
    private static final String TAG = "VideoEditerKit";
    private static VideoEditerSDK INSTANCE;
    @Nullable
    private TXVideoEditer mTXVideoEditer;

    /**
     * 缩略图相关
     */
    private List<ThumbnailBitmapInfo> mThumbnailList;               // 将已经加在好的Bitmap缓存起来

    /**
     * 预览相关
     * <p>
     * 由于SDK没有提供多个Listener的预览进度的回调，所以在上层包装一下
     */
    private final List<TXVideoPreviewListenerWrapper> mPreviewWrapperList;
    private boolean mIsReverse;

    private long mCutterDuration;                                   // 裁剪的总时长
    private long mCutterStartTime;                                  // 裁剪开始的时间
    private long mCutterEndTime;                                    // 裁剪结束的时间
    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;
    private String mVideoPath;
    private boolean mPublishFlag;

    public static VideoEditerSDK getInstance() {
        if (INSTANCE == null) {
            synchronized (VideoEditerSDK.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VideoEditerSDK();
                }
            }
        }
        return INSTANCE;
    }

    private VideoEditerSDK() {
        mThumbnailList = new ArrayList<>();
        mPreviewWrapperList = new ArrayList<>();
        mIsReverse = false;
    }

    public void setTXVideoInfo(TXVideoEditConstants.TXVideoInfo info) {
        Log.d(TAG, "setTXVideoInfo info:" + info);
        mTXVideoInfo = info;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        mTXVideoEditer.setVideoPath(mVideoPath);
    }

    /**
     * 获取视频的信息
     *
     * @return
     */
    public TXVideoEditConstants.TXVideoInfo getTXVideoInfo() {
        if (mTXVideoInfo == null) {
            mTXVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoPath);
            Log.d(TAG, "setTXVideoInfo info:" + mTXVideoInfo);
        }
        return mTXVideoInfo;
    }

    public void setEditer(TXVideoEditer editer) {
        mTXVideoEditer = editer;
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setTXVideoPreviewListener(mPreviewListener);
        }
    }

    @Nullable
    public TXVideoEditer getEditer() {
        return mTXVideoEditer;
    }

    public void clear() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setTXVideoPreviewListener(null);
            mTXVideoEditer = null;
        }

        mCutterDuration = 0;
        mCutterStartTime = 0;
        mCutterEndTime = 0;

        mThumbnailList.clear();

        synchronized (mPreviewWrapperList) {
            mPreviewWrapperList.clear();
        }
        mIsReverse = false;
    }

    public void releaseSDK() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.release();
        }
    }

    /**
     * 裁剪后的时间
     *
     * @param newVideoDuration
     */
    public void setCutterDuration(long newVideoDuration) {
        mCutterDuration = newVideoDuration;
    }

    /**
     * 获取裁剪后的时间
     *
     * @return
     */
    public long geCutterDuration() {
        return mCutterDuration;
    }


    public void setCutterStartTime(long startTime, long endTime) {
        mCutterStartTime = startTime;
        mCutterEndTime = endTime;
        mCutterDuration = endTime - startTime;
    }

    public long getCutterStartTime() {
        return mCutterStartTime;
    }

    public long getCutterEndTime() {
        return mCutterEndTime;
    }


    /**
     * ======================================================预览相关======================================================
     */
    public void setReverse(boolean isReverse) {
        mIsReverse = isReverse;
    }

    public boolean isReverse() {
        return mIsReverse;
    }

    @NonNull
    private TXVideoEditer.TXVideoPreviewListener mPreviewListener = new TXVideoEditer.TXVideoPreviewListener() {
        @Override
        public void onPreviewProgress(int time) {
            int currentTimeMs = (int) (time / 1000);//转为ms值
            synchronized (mPreviewWrapperList) {
                for (TXVideoPreviewListenerWrapper wrapper : mPreviewWrapperList) {
                    wrapper.onPreviewProgressWrapper(currentTimeMs);
                }
            }
        }

        @Override
        public void onPreviewFinished() {
            synchronized (mPreviewWrapperList) {
                for (TXVideoPreviewListenerWrapper wrapper : mPreviewWrapperList) {
                    wrapper.onPreviewFinishedWrapper();
                }
            }
        }
    };

    public void addTXVideoPreviewListenerWrapper(TXVideoPreviewListenerWrapper listener) {
        synchronized (mPreviewWrapperList) {
            if (mPreviewWrapperList.contains(listener)) {
                return;
            }
            mPreviewWrapperList.add(listener);
        }
    }

    public void removeTXVideoPreviewListenerWrapper(TXVideoPreviewListenerWrapper listener) {
        synchronized (mPreviewWrapperList) {
            mPreviewWrapperList.remove(listener);
        }
    }

    public void initSDK() {
        if (mTXVideoEditer == null) {
            mTXVideoEditer = new TXVideoEditer(UGCKitImpl.getAppContext());
        }
    }

    public void constructVideoInfo(@NonNull TXVideoEditConstants.TXVideoInfo videoInfo, long duration) {
        videoInfo.width = 100;
        videoInfo.height = 100;
        videoInfo.duration = duration;
        mTXVideoInfo = videoInfo;
    }

    public void resetDuration() {
        if (mCutterEndTime - mCutterStartTime != 0) {
            mCutterDuration = mCutterEndTime - mCutterStartTime;
            mCutterStartTime = 0;
            mCutterEndTime = mCutterDuration;
        } else {
            mCutterDuration = getTXVideoInfo().duration;
        }
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setCutFromTime(0, mCutterDuration);
        }
    }

    public long getVideoDuration() {
        return mCutterDuration;
    }

    public void initThumbnailList(TXVideoEditer.TXThumbnailListener listener) {
        int durationS = (int) (getTXVideoInfo().duration / 1000);
        // 每一秒/一张缩略图
        int thumbCount = durationS;
        Log.d(TAG, "thumbCount:" + thumbCount);
        setCutterStartTime(0, mTXVideoInfo.duration);
        mTXVideoEditer.setRenderRotation(0);
        mTXVideoEditer.getThumbnail(thumbCount, IVideoCutLayout.DEFAULT_THUMBNAIL_WIDTH, IVideoCutLayout.DEFAULT_THUMBNAIL_HEIGHT, false, listener);
    }

    public void setPublishFlag(boolean flag) {
        mPublishFlag = flag;
    }

    public boolean isPublish() {
        return mPublishFlag;
    }

    /**
     * 由于SDK没有提供多个界面的预览进度的回调，所以在上层包装一下
     */
    public interface TXVideoPreviewListenerWrapper {
        /**
         * @param time
         */
        void onPreviewProgressWrapper(int time);

        void onPreviewFinishedWrapper();
    }

    /**
     * ======================================================缩略图相关======================================================
     */

    /**
     * 获取已经加载的缩略图
     *
     * @return
     */
    @NonNull
    public List<Bitmap> getThumbnailList(long startPts, long endPts) {
        List<Bitmap> list = new ArrayList<>();
        for (ThumbnailBitmapInfo info : mThumbnailList) {
            if (info.ptsMs >= startPts && info.ptsMs <= endPts) {
                list.add(info.bitmap);
            }
        }
        return list;
    }

    @NonNull
    public List<Bitmap> getAllThumbnails() {
        return getThumbnailList(0, mTXVideoInfo.duration);
    }

    public void addThumbnailBitmap(long timeMs, Bitmap bitmap) {
        mThumbnailList.add(new ThumbnailBitmapInfo(timeMs, bitmap));
    }

    public void cleaThumbnails() {
        mThumbnailList.clear();
    }

    private class ThumbnailBitmapInfo {
        public long ptsMs;
        public Bitmap bitmap;

        public ThumbnailBitmapInfo(long ptsMs, Bitmap bitmap) {
            this.ptsMs = ptsMs;
            this.bitmap = bitmap;
        }
    }

}

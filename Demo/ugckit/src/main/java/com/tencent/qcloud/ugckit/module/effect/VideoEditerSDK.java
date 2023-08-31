package com.tencent.qcloud.ugckit.module.effect;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.module.cut.IVideoCutLayout;
import com.tencent.qcloud.ugckit.module.effect.utils.DraftEditer;
import com.tencent.qcloud.ugckit.module.effect.utils.EffectEditer;
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
 *
 *
 * The TXVideoEditer of the SDK uses the singleton mode.
 * To edit the same video across multiple activities/fragments, you can package it as a singleton in the upper layer.
 * <p>
 * Notes:
 * After each editing, make sure you call {@link VideoEditerSDK#clear()} to clear the configurations.
 */


public class VideoEditerSDK {
    private static final String                              TAG = "VideoEditerKit";
    private static       VideoEditerSDK                      sInstance;
    @Nullable
    private              TXVideoEditer                       mTXVideoEditer;
    private              List<ThumbnailBitmapInfo>           mThumbnailList;               // 缩略图相关, 将已经加在好的Bitmap缓存起来
    private              boolean                             mIsReverse;
    private              long                                mCutterDuration;                                   // 裁剪的总时长
    private              long                                mCutterStartTime;                                  // 裁剪开始的时间
    private              long                                mCutterEndTime;                                    // 裁剪结束的时间
    private              long                                mVideoDuration;                                    // 视频原时长s
    private              String                              mVideoPath;
    private              boolean                             mPublishFlag;
    private              TXVideoEditConstants.TXVideoInfo    mTXVideoInfo;
    private final        List<TXVideoPreviewListenerWrapper> mPreviewWrapperList;
    private long mEffectDuration = 0;

    public static VideoEditerSDK getInstance() {
        if (sInstance == null) {
            synchronized (VideoEditerSDK.class) {
                if (sInstance == null) {
                    sInstance = new VideoEditerSDK();
                }
            }
        }
        return sInstance;
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
     * FIXBUG：不能判断是否为空，如果更换频路径，
     *
     * @return
     */
    public TXVideoEditConstants.TXVideoInfo getTXVideoInfo() {
        if (mVideoPath == null) {
            return mTXVideoInfo;
        }
        mTXVideoInfo = TXVideoInfoReader.getInstance(UGCKit.getAppContext()).getVideoFileInfo(mVideoPath);
        if (mTXVideoInfo != null) {
            Log.d(TAG, "setTXVideoInfo duration:" + mTXVideoInfo.duration);
        }
        return mTXVideoInfo;
    }

    public void clear() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setTXVideoPreviewListener(null);
            mTXVideoEditer = null;
        }

        mCutterDuration = 0;
        mCutterStartTime = 0;
        mCutterEndTime = 0;
        mEffectDuration = 0;

        mThumbnailList.clear();
        DraftEditer.getInstance().clear();
        EffectEditer.getInstance().clear();

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

    public void setVideoDuration(long duration) {
        this.mVideoDuration = duration;
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

    /**
     * 初始化新的TXVideoEditer
     */
    public void initSDK() {
        mTXVideoEditer = new TXVideoEditer(UGCKit.getAppContext());
    }

    /**
     * 获取以前创建的TXVideoEditer
     *
     * @return
     */
    public TXVideoEditer getEditer() {
        return mTXVideoEditer;
    }

    public String getVideoSourcePath() {
        if (mTXVideoEditer == null) {
            return null;
        }
        return mTXVideoEditer.getVideoSourcePath();
    }

    public String getVideoProcessPath() {
        if (mTXVideoEditer == null) {
            return null;
        }
        return mTXVideoEditer.getVideoProcessPath();
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
            TXVideoEditConstants.TXVideoInfo videoInfo = getTXVideoInfo();
            if (videoInfo != null) {
                mCutterDuration = videoInfo.duration;
                mVideoDuration = videoInfo.duration;
            }
        }
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setCutFromTime(0, mCutterDuration);
        }
    }

    public long getVideoDuration() {
        return mCutterDuration;
    }

    public long getVideoPlayDuration() {
        return mCutterDuration + mEffectDuration;
    }

    public long getEffectDuration() {
        return mEffectDuration;
    }

    private long mEffectDrawWidth = 0;

    public void setEffectDrawWidth(long width) {
        mEffectDrawWidth = width;
    }

    public long getEffectDrawWidth() {
        return mEffectDrawWidth;
    }

    public void addEffectDuration(long plus) {
        mEffectDuration = plus;
    }

    /**
     * 初始化缩略图
     *
     * @param listener
     * @param interval 缩略图的时间间隔
     */
    public void initThumbnailList(TXVideoEditer.TXThumbnailListener listener, int interval) {
        if (interval == 0) {
            Log.e(TAG, "interval error:0");
            return;
        }
        // 每一秒/一张缩略图
        int thumbCount = (int) (mVideoDuration / interval);
        Log.d(TAG, "thumbCount:" + thumbCount);

        if (mTXVideoEditer != null) {
            mTXVideoEditer.setRenderRotation(0);
            // FIXBUG：获取缩略图之前需要设置缩略图的开始和结束时间点，SDK内部会根据开始时间和结束时间出缩略图
            mTXVideoEditer.setCutFromTime(mCutterStartTime, mCutterEndTime);
            mTXVideoEditer.getThumbnail(thumbCount, IVideoCutLayout.DEFAULT_THUMBNAIL_WIDTH, IVideoCutLayout.DEFAULT_THUMBNAIL_HEIGHT, false, listener);
        }
    }

    public void setPublishFlag(boolean flag) {
        mPublishFlag = flag;
    }

    public boolean isPublish() {
        return mPublishFlag;
    }

    /**
     * 在特效页面设置特效，会调用SDK，特效点击"取消"后，还原设置进入SDK的特效
     */
    public void restore() {
        EffectEditer effectEditer = EffectEditer.getInstance();
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setBGM(effectEditer.getBgmPath());
            mTXVideoEditer.setBGMVolume(effectEditer.getBgmVolume());
            mTXVideoEditer.setVideoVolume(effectEditer.getVideoVolume());
        }
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

    public interface ThumbnailsListener {
        void onThumbnailGot(long time, Bitmap bitmap);
    }

    private ThumbnailsListener mThumbnailsListener;

    public void setThumbnailListener(ThumbnailsListener listener) {
        mThumbnailsListener = listener;
    }

    public void addThumbnailBitmap(long timeMs, Bitmap bitmap) {
        mThumbnailList.add(new ThumbnailBitmapInfo(timeMs, bitmap));
        if (mThumbnailsListener != null) {
            mThumbnailsListener.onThumbnailGot(timeMs, bitmap);
        }
    }

    /**
     * 清空缩略图列表
     */
    public void clearThumbnails() {
        mThumbnailList.clear();
    }

    public int getThumbnailSize() {
        return mThumbnailList == null ? 0 : mThumbnailList.size();
    }

    private class ThumbnailBitmapInfo {
        public long   ptsMs;
        public Bitmap bitmap;

        public ThumbnailBitmapInfo(long ptsMs, Bitmap bitmap) {
            this.ptsMs = ptsMs;
            this.bitmap = bitmap;
        }
    }

}

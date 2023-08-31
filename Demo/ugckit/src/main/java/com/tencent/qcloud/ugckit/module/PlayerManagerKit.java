package com.tencent.qcloud.ugckit.module;

import androidx.annotation.NonNull;
import android.util.Log;

import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.qcloud.ugckit.module.effect.utils.PlayState;
import com.tencent.ugc.TXVideoEditer;

import java.util.ArrayList;
import java.util.List;

public class PlayerManagerKit implements TXVideoEditer.TXVideoPreviewListener {

    private static final String           TAG       = "PlayerKit";
    @NonNull
    private static       PlayerManagerKit sInstance = new PlayerManagerKit();

    private int                       mCurrentState;
    private long                      mPreviewAtTime;
    public  boolean                   isPreviewFinish;
    private Object                    mProgressObject = new Object();
    private Object                    mStateObject    = new Object();
    private List<OnPreviewListener>   mProgressListenerList;
    private List<OnPlayStateListener> mStateListenerList;

    private PlayerManagerKit() {
        mProgressListenerList = new ArrayList<OnPreviewListener>();
        mStateListenerList = new ArrayList<OnPlayStateListener>();
        mCurrentState = PlayState.STATE_NONE;
    }

    @NonNull
    public static PlayerManagerKit getInstance() {
        return sInstance;
    }

    /**
     * 开始视频预览
     */
    public void startPlay() {
        Log.i(TAG, "startPlay mCurrentState:" + mCurrentState);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
            if (editer != null) {
                addPreviewListener();

                long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
                long endTime = VideoEditerSDK.getInstance().getCutterEndTime();
                Log.d(TAG, "[UGCKit][PlayerKit]startPlay startTime:" + startTime + ",endTime:" + endTime);
                editer.startPlayFromTime(startTime, endTime);

                mCurrentState = PlayState.STATE_PLAY;
                notifyStart();
            }
            isPreviewFinish = false;
        }
    }

    public void startPlayCutTime() {
        Log.i(TAG, "startPlayCutTime");
        long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
        long endTime = VideoEditerSDK.getInstance().getCutterEndTime();

        addPreviewListener();
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            Log.d(TAG, "[UGCKit][PlayerKit]startPlayCutTime startTime:" + startTime + ",endTime:" + endTime);
            editer.startPlayFromTime(startTime, endTime);

            notifyStart();
        }
        mCurrentState = PlayState.STATE_PLAY;
    }

    /**
     * 停止视频预览
     */
    public void stopPlay() {
        Log.i(TAG, "stopPlay " + mCurrentState);
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_PREVIEW_AT_TIME || mCurrentState == PlayState.STATE_PAUSE) {
            TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
            if (mEditer != null) {
                mEditer.stopPlay();
            }
            removePreviewListener();

            notifyStop();
        }
        mCurrentState = PlayState.STATE_STOP;
    }

    public void resumePlay() {
        Log.i(TAG, "startPlay " + mCurrentState);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay();
        } else {
            TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
            if (mEditer != null) {
                mEditer.resumePlay();
            }

            notifyResume();
        }
        mCurrentState = PlayState.STATE_RESUME;
    }

    public void pausePlay() {
        Log.i(TAG, "stopPlay " + mCurrentState);
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
            if (mEditer != null) {
                mEditer.pausePlay();
            }
            notifyPause();
        }
        mCurrentState = PlayState.STATE_PAUSE;
    }

    public void restartPlay() {
        stopPlay();
        startPlay();
    }

    /**
     * 调用mTXVideoEditer.previewAtTime后，需要记录当前时间，下次播放时从当前时间开始
     *
     * @param timeMs
     */
    public void previewAtTime(long timeMs) {
        pausePlay();
        isPreviewFinish = false;
        TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
        if (mEditer != null) {
            mEditer.previewAtTime(timeMs);
        }
        mPreviewAtTime = timeMs;
        mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
    }

    public void addPreviewListener() {
        Log.d(TAG, "addPreviewListener");
        TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
        if (mEditer != null) {
            mEditer.setTXVideoPreviewListener(this);
        }
    }

    public void removePreviewListener() {
        Log.d(TAG, "removePreviewListener");
        TXVideoEditer mEditer = VideoEditerSDK.getInstance().getEditer();
        if (mEditer != null) {
            mEditer.setTXVideoPreviewListener(null);
        }
    }

    @Override
    public void onPreviewProgress(int time) {
        // 转化为ms
        notifyPreviewProgress(time / 1000);
    }

    @Override
    public void onPreviewFinished() {
        isPreviewFinish = true;
        Log.d(TAG, "=====onPreviewFinished=====");
        mCurrentState = PlayState.STATE_NONE;
        notifyPreviewFinish();

        if (!isOnAddingMotion) {
            restartPlay();
        }
    }
    
    private boolean isOnAddingMotion = false;

    public void setOnAddingMotion(boolean isAddingMotion) {
        isOnAddingMotion = isAddingMotion;
    }

    public void playVideo(boolean isMotionFilter) {
        Log.i(TAG, "playVideo mCurrentState = " + mCurrentState);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay();
        } else if ((mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) && !isMotionFilter) {
            pausePlay();
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            resumePlay();
        } else if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            long startTime = VideoEditerSDK.getInstance().getCutterStartTime();
            long endTime = VideoEditerSDK.getInstance().getCutterEndTime();

            if ((mPreviewAtTime >= endTime || mPreviewAtTime <= startTime) && !isMotionFilter) {
                startPlay(startTime, endTime);
            } else if (!VideoEditerSDK.getInstance().isReverse()) {
                startPlay(mPreviewAtTime, endTime);
            } else {
                startPlay(startTime, mPreviewAtTime);
            }
        }
    }

    public void startPlay(long startTime, long endTime) {
        TXVideoEditer editer = VideoEditerSDK.getInstance().getEditer();
        if (editer != null) {
            addPreviewListener();
            Log.d(TAG, "[UGCKit][PlayerKit]startPlay startTime:" + startTime + ",endTime:" + endTime);
            editer.startPlayFromTime(startTime, endTime);
            mCurrentState = PlayState.STATE_PLAY;
            notifyStart();
        }
        isPreviewFinish = false;
    }

    public void addOnPreviewLitener(OnPreviewListener listener) {
        synchronized (mProgressObject) {
            mProgressListenerList.add(listener);
        }
    }

    public void removeOnPreviewListener(OnPreviewListener listener) {
        synchronized (mProgressObject) {
            mProgressListenerList.remove(listener);
        }
    }

    public void removeAllPreviewListener() {
        synchronized (mProgressObject) {
            mProgressListenerList.clear();
        }
    }

    public void notifyPreviewProgress(int time) {
        synchronized (mProgressObject) {
            for (int i = 0; i < mProgressListenerList.size(); i++) {
                OnPreviewListener listener = mProgressListenerList.get(i);
                if (listener != null) {
                    listener.onPreviewProgress(time);
                }
            }
        }
    }

    public void notifyPreviewFinish() {
        synchronized (mProgressObject) {
            for (int i = 0; i < mProgressListenerList.size(); i++) {
                OnPreviewListener listener = mProgressListenerList.get(i);
                if (listener != null) {
                    listener.onPreviewFinish();
                }
            }
        }
    }

    public void addOnPlayStateLitener(OnPlayStateListener listener) {
        synchronized (mStateObject) {
            mStateListenerList.add(listener);
        }
    }

    public void removeOnPlayStateListener(OnPlayStateListener listener) {
        synchronized (mStateObject) {
            mStateListenerList.remove(listener);
        }
    }

    public void removeAllPlayStateListener() {
        synchronized (mStateObject) {
            mStateListenerList.clear();
        }
    }

    public void notifyStart() {
        synchronized (mStateObject) {
            for (int i = 0; i < mStateListenerList.size(); i++) {
                OnPlayStateListener listener = mStateListenerList.get(i);
                if (listener != null) {
                    listener.onPlayStateStart();
                }
            }
        }
    }

    public void notifyStop() {
        synchronized (mStateObject) {
            for (int i = 0; i < mStateListenerList.size(); i++) {
                OnPlayStateListener listener = mStateListenerList.get(i);
                if (listener != null) {
                    listener.onPlayStateStop();
                }
            }
        }
    }

    public void notifyResume() {
        synchronized (mStateObject) {
            for (int i = 0; i < mStateListenerList.size(); i++) {
                OnPlayStateListener listener = mStateListenerList.get(i);
                if (listener != null) {
                    listener.onPlayStateResume();
                }
            }
        }
    }

    public void notifyPause() {
        synchronized (mStateObject) {
            for (int i = 0; i < mStateListenerList.size(); i++) {
                OnPlayStateListener listener = mStateListenerList.get(i);
                if (listener != null) {
                    listener.onPlayStatePause();
                }
            }
        }
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public interface OnPlayStateListener {
        void onPlayStateStart();

        void onPlayStateResume();

        void onPlayStatePause();

        void onPlayStateStop();
    }

    public interface OnPreviewListener {
        void onPreviewProgress(int time);

        void onPreviewFinish();
    }

}

package com.tencent.qcloud.ugckit.module.record;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;

/**
 * 录制-音乐管理
 */
public class RecordMusicManager {
    private static final String TAG = "RecordMusicManager";

    @NonNull
    private static RecordMusicManager sInstance = new RecordMusicManager();
    private        MusicInfo          mMusicInfo;

    private RecordMusicManager() {
        mMusicInfo = new MusicInfo();
    }

    @NonNull
    public static RecordMusicManager getInstance() {
        return sInstance;
    }

    public void setRecordMusicInfo(@NonNull MusicInfo musicInfo) {
        mMusicInfo.path = musicInfo.path;
        mMusicInfo.name = musicInfo.name;
        mMusicInfo.position = musicInfo.position;
        mMusicInfo.duration = musicInfo.duration;
        mMusicInfo.startTime = 0;
        mMusicInfo.endTime = musicInfo.duration;
    }

    /**
     * 判断是否选择了音乐
     *
     * @return
     */
    public boolean isChooseMusic() {
        return !TextUtils.isEmpty(mMusicInfo.path);
    }

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
    }

    /**
     * 开始音乐试听
     */
    public void startPreviewMusic() {
        if (!TextUtils.isEmpty(mMusicInfo.path)) {
            // 保证在试听的时候音乐是正常播放的
            VideoRecordSDK.getInstance().setRecordSpeed(TXRecordCommon.RECORD_SPEED_NORMAL);
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.playBGMFromTime((int) mMusicInfo.startTime, (int) mMusicInfo.endTime);
            }
        }
    }

    /**
     * 停止音乐试听
     */
    public void stopPreviewMusic() {
        // 选择完音乐返回时试听结束
        if (!TextUtils.isEmpty(mMusicInfo.path)) {
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.stopBGM();
                mMusicInfo.playingPath = null;
            }
            // 在试听结束时，再设置回原来的速度
            VideoRecordSDK.getInstance().setRecordSpeed(UGCKitRecordConfig.getInstance().mRecordSpeed);
        }
    }

    public void startMusic() {
        if (!TextUtils.isEmpty(mMusicInfo.path)) {
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                mMusicInfo.duration = record.setBGM(mMusicInfo.path);
                record.playBGMFromTime((int) mMusicInfo.startTime, (int) mMusicInfo.endTime);
            }
            mMusicInfo.playingPath = mMusicInfo.path;
        }
    }

    public void resumeMusic() {
        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            if (!TextUtils.isEmpty(mMusicInfo.path)) {
                if (mMusicInfo.playingPath == null || !mMusicInfo.path.equals(mMusicInfo.playingPath)) {
                    mMusicInfo.playingPath = mMusicInfo.path;
                    mMusicInfo.duration = record.setBGM(mMusicInfo.path);
                    record.playBGMFromTime((int) mMusicInfo.startTime, (int) mMusicInfo.duration);
                } else {
                    record.resumeBGM();
                }
            }
        }
    }

    public void pauseMusic() {
        Log.d(TAG, "pauseMusic");
        if (!TextUtils.isEmpty(mMusicInfo.playingPath)) {
            TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
            if (record != null) {
                record.pauseBGM();
            }
        }
    }

    public void deleteMusic() {
        mMusicInfo.path = null;
        mMusicInfo.position = -1;

        TXUGCRecord record = VideoRecordSDK.getInstance().getRecorder();
        if (record != null) {
            record.stopBGM();
            record.setBGM(null);
        }
    }
}

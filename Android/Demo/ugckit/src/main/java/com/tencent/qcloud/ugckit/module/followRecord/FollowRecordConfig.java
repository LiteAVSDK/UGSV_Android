package com.tencent.qcloud.ugckit.module.followRecord;

import android.support.annotation.NonNull;

import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

/**
 * 合唱信息配置
 */
public class FollowRecordConfig extends UGCKitRecordConfig {
    private static final String TAG = "FollowRecordConfig";
    @NonNull
    private static FollowRecordConfig instance = new FollowRecordConfig();

    private FollowRecordConfig() {
        super();
    }

    @NonNull
    public static FollowRecordConfig getInstance() {
        return instance;
    }

    public FollowRecordInfo videoInfo = new FollowRecordInfo();
    public TXVideoEditConstants.TXVideoInfo recordVideoInfo;
    public TXVideoEditConstants.TXVideoInfo playVideoInfo;

    /**
     * 加载合唱视频基本信息<br>
     * 1、加载录制完视频基本信息<br>
     * 2、加载跟拍视频基本信息
     */
    public void loadPlayVideoInfo() {
        playVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(videoInfo.playPath);

        videoInfo.fps = (int) playVideoInfo.fps;
        videoInfo.duration = (int) playVideoInfo.duration;
    }

    public void loadRecordVideoInfo() {
        recordVideoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(videoInfo.recordPath);
    }
}

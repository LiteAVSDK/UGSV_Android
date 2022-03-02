package com.tencent.qcloud.ugckit.module.editer;

import com.tencent.ugc.TXVideoEditConstants;

public class UGCKitEditConfig {
    /**
     * 视频分辨率【默认为540P】
     */
    public int resolution   = TXVideoEditConstants.VIDEO_COMPRESSED_540P;
    /**
     * 视频码率（Mbps）
     */
    public int videoBitrate = 6500;

    /**
     * 是否生成封面，默认为true
     */
    public boolean isCoverGenerate = true;
    /**
     * 是否保存到相册，默认为true
     */
    public boolean isSaveToDCIM    = true;

    /**
     * 是否需要发布视频
     */
    public boolean isPublish = true;

    /**
     * 视频水印
     */
    public WaterMarkConfig     mWaterMarkConfig;
    /**
     * 片尾水印
     */
    public TailWaterMarkConfig mTailWaterMarkConfig;
}

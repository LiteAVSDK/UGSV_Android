package com.tencent.qcloud.ugckit.basic;

public class UGCKitResult {
    /**
     * 错误码
     */
    public int    errorCode;
    /**
     * 详细描述
     */
    public String descMsg;
    /**
     * 封面
     */
    public String coverPath;
    /**
     * 输出视频路径
     */
    public String outputPath;

    /**
     * 是否需要发布
     */
    public boolean isPublish;
}

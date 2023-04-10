package com.tencent.qcloud.ugckit.module.upload.impl;

/**
 * 上传续点控制
 */
public interface IUploadResumeController {

    /**
     * 保存续点
     * @param filePath 文件路径
     * @param vodSessionKey 上传session
     * @param uploadId 上传id
     * @param uploadInfo 上传详情
     */
    void saveSession(String filePath, String vodSessionKey, String uploadId, TVCUploadInfo uploadInfo);

    /**
     * 获得续点，当enableResume为true的时候，才会被调用
     * @param filePath 文件路径
     */
    ResumeCacheData getResumeData(String filePath);

    /**
     * 清除过期续点
     */
    void clearLocalCache();

    /**
     * 判断是否是续点视频，当enableResume为true的时候，才会被调用
     */
    boolean isResumeUploadVideo(String uploadId, TVCUploadInfo uploadInfo, String vodSessionKey,
                                long fileLastModTime, long coverFileLastModTime);
}

package com.tencent.qcloud.ugckit.module.upload.impl;

/**
 * Upload breakpoint control
 * 上传续点控制
 */
public interface IUploadResumeController {

    /**
     * Save resume point
     * 保存续点
     * @param filePath File path
     *                 文件路径
     * @param vodSessionKey Upload session
     *                      上传session
     * @param uploadId Upload ID
     *                 上传id
     * @param uploadInfo Upload details
     *                   上传详情
     */
    void saveSession(String filePath, String vodSessionKey, String uploadId, TVCUploadInfo uploadInfo, String uploadKey);

    /**
     * Get resume point, only called when enableResume is true
     * 获得续点，当enableResume为true的时候，才会被调用
     * @param filePath File path
     *                 文件路径
     */
    ResumeCacheData getResumeData(String filePath, String uploadKey);

    /**
     * Clear expired resume points
     * 清除过期续点
     */
    void clearLocalCache();

    /**
     * Determine whether it is a resume point video, only called when enableResume is true
     * 判断是否是续点视频，当enableResume为true的时候，才会被调用
     */
    boolean isResumeUploadVideo(String uploadId, TVCUploadInfo uploadInfo, String vodSessionKey,
                                long fileLastModTime, long coverFileLastModTime, String uploadKey);
}

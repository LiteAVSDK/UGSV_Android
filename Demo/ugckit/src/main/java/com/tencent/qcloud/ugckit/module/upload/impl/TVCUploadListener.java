package com.tencent.qcloud.ugckit.module.upload.impl;

/**
 * Video upload interface
 * 视频上传接口
 */
public interface TVCUploadListener {
    /**
     * Upload successful
     * 上传成功
     *
     * @param fileId   File id
     *                 文件id
     * @param playUrl  VOD url
     *                 点播url
     * @param coverUrl Cover address
     *                 封面地址
     */
    void onSuccess(String fileId, String playUrl, String coverUrl);

    /**
     * Upload failed
     * 上传失败
     *
     * @param errCode Upload failed
     *                错误码
     * @param errMsg  Error description
     *                错误描述
     */
    void onFailed(int errCode, String errMsg);

    /**
     * Upload progress
     * 上传进度
     *
     * @param currentSize Uploaded file size
     *                    已上传文件大小
     * @param totalSize   Total file size
     *                    文件总大小
     */
    void onProgress(long currentSize, long totalSize);
}

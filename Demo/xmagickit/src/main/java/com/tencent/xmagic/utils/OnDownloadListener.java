package com.tencent.xmagic.utils;

public interface OnDownloadListener {
    /**
     * 下载成功或者文件已存在
     */
    void onDownloadSuccess(String directory);

    /**
     * @param progress 下载进度
     */
    void onDownloading(int progress);

    /**
     * 下载失败
     */
    void onDownloadFailed(int errorCode);

}

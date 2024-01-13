package com.tencent.liteav.beautykit.download;

public interface DownloadListener {
    void onDownloadFail(String errorMsg);

    void onDownloadProgress(final int progress);

    void onDownloadSuccess(String filePath);
}

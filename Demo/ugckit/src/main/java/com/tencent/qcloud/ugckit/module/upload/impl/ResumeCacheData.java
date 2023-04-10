package com.tencent.qcloud.ugckit.module.upload.impl;

public class ResumeCacheData {
    private String mVodSessionKey;
    private String mUploadId;
    private Long mFileLastModTime;
    private Long mCoverFileLastModTime;

    public String getVodSessionKey() {
        return mVodSessionKey;
    }

    public void setVodSessionKey(String mVodSessionKey) {
        this.mVodSessionKey = mVodSessionKey;
    }

    public String getUploadId() {
        return mUploadId;
    }

    public void setUploadId(String mUploadId) {
        this.mUploadId = mUploadId;
    }

    public Long getFileLastModTime() {
        return mFileLastModTime;
    }

    public void setFileLastModTime(Long mFileLastModTime) {
        this.mFileLastModTime = mFileLastModTime;
    }

    public Long getCoverFileLastModTime() {
        return mCoverFileLastModTime;
    }

    public void setCoverFileLastModTime(Long mCoverFileLastModTime) {
        this.mCoverFileLastModTime = mCoverFileLastModTime;
    }
}

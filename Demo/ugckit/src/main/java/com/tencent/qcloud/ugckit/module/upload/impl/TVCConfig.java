package com.tencent.qcloud.ugckit.module.upload.impl;

public class TVCConfig {

    public String mCustomKey;

    public String mSignature;

    public boolean mEnableResume = true;

    public boolean mEnableHttps = false;

    public int mVodReqTimeOutInSec = 10;

    public long mSliceSize = 0;

    public int mConcurrentCount = -1;

    public boolean mIsDebuggable = true;

    public IUploadResumeController mUploadResumeController;
}

package com.tencent.qcloud.ugckit.module.upload.impl;

import android.support.annotation.NonNull;

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

public class TVCDirectCredentialProvider extends BasicLifecycleCredentialProvider {
    private String secretId;
    private String secretKey;
    private String token;
    private long expiredTime;

    public TVCDirectCredentialProvider(String secretId, String secretKey, String token, long expiredTime) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.token = token;
        this.expiredTime = expiredTime;
    }

    @NonNull
    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials() throws QCloudClientException {
        return new SessionQCloudCredentials(secretId, secretKey, token, expiredTime);
    }
}

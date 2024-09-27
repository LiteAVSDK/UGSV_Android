package com.tencent.qcloud.ugckit.module.upload.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Map;

/**
 * Upload breakpoint control
 * 上传续点控制
 */
public class UploadResumeDefaultController implements IUploadResumeController {

    private static final String TAG = "UploadResumeDefaultController";

    // Breakpoint retransmission session local cache
    // Using the file MD5 as the key value, the stored content is <session, uploadId, fileLastModify, expiredTime>
    private static final String LOCAL_FILE_NAME = "TVCSession";
    private static final String KEY_SESSION = "session";
    private static final String KEY_UPLOAD_ID = "uploadId";
    private static final String KEY_EXPIRED_TIME = "expiredTime";
    private static final String KEY_FILE_LAST_MOD_TIME = "fileLastModTime";
    private static final String KEY_COVER_FILE_LAST_MOD_TIME = "coverFileLastModTime";

    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.Editor mShareEditor;

    public UploadResumeDefaultController(Context context) {
        mSharedPreferences = context.getSharedPreferences(LOCAL_FILE_NAME, Activity.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();
    }

    @Override
    public void saveSession(String filePath, String vodSessionKey, String uploadId, TVCUploadInfo uploadInfo
            , String uploadKey) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        if (mSharedPreferences != null) {
            try {
                String sessionKey = TVCUtils.getFileMD5(filePath) + uploadKey;
                // If vodSessionKey and uploadId are empty, it means to delete the record
                if (TextUtils.isEmpty(vodSessionKey) || TextUtils.isEmpty(uploadId)) {
                    mShareEditor.remove(sessionKey);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(KEY_SESSION, vodSessionKey);
                    jsonObject.put(KEY_UPLOAD_ID, uploadId);
                    // Expiration time is 1 day
                    jsonObject.put(KEY_EXPIRED_TIME, System.currentTimeMillis() / 1000 + 24 * 60 * 60);
                    jsonObject.put(KEY_FILE_LAST_MOD_TIME, uploadInfo.getFileLastModifyTime());
                    jsonObject.put(KEY_COVER_FILE_LAST_MOD_TIME, uploadInfo.isNeedCover()
                            ? uploadInfo.getCoverLastModifyTime() : 0);
                    String comment = jsonObject.toString();
                    mShareEditor.putString(sessionKey, comment);
                }
                mShareEditor.apply();
            } catch (Exception e) {
                TVCLog.e(TAG, "saveSession failed", e);
            }
        }
    }

    @Override
    public ResumeCacheData getResumeData(String filePath, String uploadKey) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        ResumeCacheData resumeCacheData = null;
        String sessionKey = TVCUtils.getFileMD5(filePath) + uploadKey;
        if (mSharedPreferences.contains(sessionKey)) {
            try {
                JSONObject json = new JSONObject(mSharedPreferences.getString(sessionKey, ""));
                long expiredTime = json.optLong(KEY_EXPIRED_TIME, 0);
                if (expiredTime > System.currentTimeMillis() / 1000) {
                    resumeCacheData = new ResumeCacheData();
                    resumeCacheData.setVodSessionKey(json.optString(KEY_SESSION, ""));
                    resumeCacheData.setUploadId(json.optString(KEY_UPLOAD_ID, ""));
                    resumeCacheData.setFileLastModTime(json.optLong(KEY_FILE_LAST_MOD_TIME, 0));
                    resumeCacheData.setCoverFileLastModTime(json.optLong(KEY_COVER_FILE_LAST_MOD_TIME, 0));
                }
            } catch (Exception e) {
                TVCLog.e(TAG, "getResumeData failed", e);
            }
        }
        return resumeCacheData;
    }

    @Override
    public void clearLocalCache() {
        if (mSharedPreferences != null) {
            try {
                Map<String, ?> allContent = mSharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : allContent.entrySet()) {
                    JSONObject json = new JSONObject((String) entry.getValue());
                    long expiredTime = json.optLong(KEY_EXPIRED_TIME, 0);
                    // Clear the key if it has expired
                    if (expiredTime < System.currentTimeMillis() / 1000) {
                        mShareEditor.remove(entry.getKey());
                        mShareEditor.apply();
                    }
                }
            } catch (Exception e) {
                TVCLog.e(TAG, "clearLocalCache failed", e);
            }
        }
    }

    @Override
    public boolean isResumeUploadVideo(String uploadId, TVCUploadInfo uploadInfo, String vodSessionKey,
                                       long fileLastModTime, long coverFileLastModTime, String uploadKey) {
        return !TextUtils.isEmpty(uploadId) && uploadInfo != null && !TextUtils.isEmpty(vodSessionKey);
    }
}

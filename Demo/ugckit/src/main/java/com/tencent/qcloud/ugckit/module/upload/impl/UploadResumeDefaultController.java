package com.tencent.qcloud.ugckit.module.upload.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Map;

/**
 * 上传续点控制
 */
public class UploadResumeDefaultController implements IUploadResumeController {

    // 断点重传session本地缓存
    // 以文件md5作为key值，存储的内容是<session, uploadId, fileLastModify, expiredTime>
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
    public void saveSession(String filePath, String vodSessionKey, String uploadId, TVCUploadInfo uploadInfo) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        if (mSharedPreferences != null) {
            try {
                String sessionKey = TVCUtils.getFileMD5(filePath);
                // vodSessionKey、uploadId为空就表示删掉该记录
                if (TextUtils.isEmpty(vodSessionKey) || TextUtils.isEmpty(uploadId)) {
                    mShareEditor.remove(sessionKey);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(KEY_SESSION, vodSessionKey);
                    jsonObject.put(KEY_UPLOAD_ID, uploadId);
                    jsonObject.put(KEY_EXPIRED_TIME, System.currentTimeMillis() / 1000 + 24 * 60 * 60);
                    jsonObject.put(KEY_FILE_LAST_MOD_TIME, uploadInfo.getFileLastModifyTime());
                    jsonObject.put(KEY_COVER_FILE_LAST_MOD_TIME, uploadInfo.isNeedCover()
                            ? uploadInfo.getCoverLastModifyTime() : 0);
                    String comment = jsonObject.toString();
                    mShareEditor.putString(sessionKey, comment);
                }
                mShareEditor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ResumeCacheData getResumeData(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        ResumeCacheData resumeCacheData = null;
        String sessionKey = TVCUtils.getFileMD5(filePath);
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
                e.printStackTrace();
            }
        }
        return resumeCacheData;
    }

    @Override
    public void clearLocalCache() {
        if (mSharedPreferences != null) {
            try {
                Map<String, ?> allContent = mSharedPreferences.getAll();
                //注意遍历map的方法
                for (Map.Entry<String, ?> entry : allContent.entrySet()) {
                    JSONObject json = new JSONObject((String) entry.getValue());
                    long expiredTime = json.optLong(KEY_EXPIRED_TIME, 0);
                    // 过期了清空key
                    if (expiredTime < System.currentTimeMillis() / 1000) {
                        mShareEditor.remove(entry.getKey());
                        mShareEditor.apply();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isResumeUploadVideo(String uploadId, TVCUploadInfo uploadInfo, String vodSessionKey,
                                       long fileLastModTime, long coverFileLastModTime) {
        return !TextUtils.isEmpty(uploadId) && uploadInfo != null && !TextUtils.isEmpty(vodSessionKey);
    }
}

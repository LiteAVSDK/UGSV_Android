package com.tencent.qcloud.ugckit.module.record.draft;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.qcloud.ugckit.utils.SharedPreferenceUtils;
import com.tencent.qcloud.ugckit.UGCKitConstants;

import java.util.List;

/**
 * 录制草稿箱管理类
 * 必须保证草稿箱中的分段以及将要录制的分段参数一致！
 * <p>
 * 一、最近一次的草稿箱
 * SharedPreferencesHelper中的 SP_KEY_RECORD_LAST_DRAFT 里保存最近录制的数据json格式如下，对应草稿箱实体类：RecordDraftInfo：
 * {
 * "aspectRatio": 0,
 * "partList":
 * [
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172350252.mp4"
 * },
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172353808.mp4"
 * },
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172413134.mp4"
 * }
 * ]
 * }
 * <p>
 * <p>
 * 二、历史草稿箱
 * SharedPreferencesHelper中的 SP_KEY_RECORD_HISTORY_DRAFT 里可以保存多次历史录制任务数据json格式如下，对应草稿箱实体类：HistoryRecordDraftInfo：
 * {
 * "historyDraftInfo":
 * [
 * {
 * "aspectRatio": 0,
 * "partList":
 * [
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172350252.mp4"
 * },
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172353808.mp4"
 * }
 * ]
 * },
 * {
 * "aspectRatio": 0,
 * "partList":
 * [
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172350252.mp4"
 * },
 * {
 * "path": "/storage/emulated/0/TXUGC/TXUGCParts/temp_TXUGC_20180923_172353808.mp4"
 * }
 * ]
 * }
 * ]
 * }
 */
public class RecordDraftManager {
    private final String TAG = "RecordDraftManager";

    public static RecordDraftManager    sInstance;
    private       SharedPreferenceUtils mSharedPreferenceUtils;

    public RecordDraftManager(@NonNull Context context) {
        mSharedPreferenceUtils = new SharedPreferenceUtils(context, UGCKitConstants.SP_NAME_RECORD);
    }

    public void setLastAspectRatio(int aspectRatio) {
        RecordDraftInfo recordDraftInfo = getLastDraftInfo();
        if (recordDraftInfo == null) {
            recordDraftInfo = new RecordDraftInfo();
        }
        recordDraftInfo.setAspectRatio(aspectRatio);
        saveLastDraft(recordDraftInfo);
    }

    /**
     * 保存最近录制的草稿的最新一段
     */
    public void saveLastPart(String partPath) {
        if (TextUtils.isEmpty(partPath)) {
            return;
        }

        RecordDraftInfo recordDraftInfo = getLastDraftInfo();
        if (recordDraftInfo == null) {
            recordDraftInfo = new RecordDraftInfo();
        }

        RecordDraftInfo.RecordPart recordPart = recordDraftInfo.new RecordPart();
        recordPart.setPath(partPath);
        recordDraftInfo.getPartList().add(recordPart);

        saveLastDraft(recordDraftInfo);
    }

    private void saveLastDraft(RecordDraftInfo recordDraftInfo) {
        Gson gson = new Gson();
        String recordDraftStr = gson.toJson(recordDraftInfo);
        mSharedPreferenceUtils.put(UGCKitConstants.SP_KEY_RECORD_LAST_DRAFT, recordDraftStr);
    }

    /**
     * 获取最近录制的草稿
     */
    @Nullable
    public RecordDraftInfo getLastDraftInfo() {
        String draftStr = mSharedPreferenceUtils.getSharedPreference(UGCKitConstants.SP_KEY_RECORD_LAST_DRAFT, "").toString();
        if (TextUtils.isEmpty(draftStr)) {
            return null;
        }
        Gson gson = new Gson();
        RecordDraftInfo recordDraftInfo = gson.fromJson(draftStr, new TypeToken<RecordDraftInfo>() {
        }.getType());
        return recordDraftInfo;
    }

    /**
     * 删除最近录制的草稿
     */
    public void deleteLastRecordDraft() {
        mSharedPreferenceUtils.put(UGCKitConstants.SP_KEY_RECORD_LAST_DRAFT, "");
    }

    /**
     * 删除最近录制的草稿的最后一段
     */
    public void deleteLastPart() {
        RecordDraftInfo recordDraftInfo = getLastDraftInfo();
        if (recordDraftInfo == null) {
            Log.e(TAG, "recordDraftInfo is null, ignore");
            return;
        }

        List<RecordDraftInfo.RecordPart> recordPartList = recordDraftInfo.getPartList();
        if (recordPartList == null || recordPartList.size() == 0) {
            Log.e(TAG, "recordDraftInfo is empty, ignore");
            return;
        }
        recordPartList.remove(recordPartList.size() - 1);

        saveLastDraft(recordDraftInfo);
    }

}

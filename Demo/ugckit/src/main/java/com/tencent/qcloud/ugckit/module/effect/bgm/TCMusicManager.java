package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.TCHttpURLClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TCMusicManager {
    private static final String            TAG    = "TCBgmManager";
    private              boolean           isLoading;
    private              SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(UGCKit.getAppContext());
    private              LoadMusicListener mLoadMusicListener;

    private static class TCMusicMgrHolder {
        @NonNull
        private static TCMusicManager instance = new TCMusicManager();
    }

    @NonNull
    public static TCMusicManager getInstance() {
        return TCMusicMgrHolder.instance;
    }

    public void loadMusicList() {
        if (isLoading) {
            Log.e(TAG, "loadMusicList, is loading");
            return;
        }
        isLoading = true;
        TCHttpURLClient.getInstance().get(UGCKitConstants.SVR_BGM_GET_URL, new TCHttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {
                Log.i(TAG, "http request success:  result = " + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject bgmObject = jsonObject.getJSONObject("bgm");
                    if (bgmObject == null && mLoadMusicListener != null) {
                        mLoadMusicListener.onBgmList(null);
                        return;
                    }
                    JSONArray list = bgmObject.getJSONArray("list");
                    Type listType = new TypeToken<ArrayList<TCMusicInfo>>() {
                    }.getType();
                    ArrayList<TCMusicInfo> bgmInfoList = new Gson().fromJson(list.toString(), listType);

                    getLocalPath(bgmInfoList);
                    if (mLoadMusicListener != null) {
                        mLoadMusicListener.onBgmList(bgmInfoList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    isLoading = false;
                }
            }

            @Override
            public void onError() {
                isLoading = false;
            }
        });
    }

    /**
     * 根据bgmList，获取本地已保存过的路径
     *
     * @param musicInfos
     */
    private void getLocalPath(@Nullable ArrayList<TCMusicInfo> musicInfos) {
        if (musicInfos == null || musicInfos.size() == 0) {
            return;
        }
        for (TCMusicInfo TCMusicInfo : musicInfos) {
            TCMusicInfo.localPath = mPrefs.getString(TCMusicInfo.name, "");
        }
        for (TCMusicInfo TCMusicInfo : musicInfos) {
            if (!TCMusicInfo.localPath.equals("")) {
                TCMusicInfo.status = TCMusicInfo.STATE_DOWNLOADED;
            }
        }
    }

    public void downloadMusicInfo(final String bgmName, final int position, String url) {
        TCMusicDownloadProgress TCMusicDownloadProgress = new TCMusicDownloadProgress(bgmName, position, url);
        TCMusicDownloadProgress.start(new TCMusicDownloadProgress.Downloadlistener() {
            @Override
            public void onDownloadFail(String errorMsg) {
                LoadMusicListener loadMusicListener = null;
                synchronized (TCMusicManager.this) {
                    loadMusicListener = mLoadMusicListener;
                }

                if (loadMusicListener != null) {
                    loadMusicListener.onDownloadFail(position, errorMsg);
                }
            }

            @Override
            public void onDownloadProgress(int progress) {
                Log.i(TAG, "downloadMusicInfo, progress = " + progress);
                LoadMusicListener loadMusicListener = null;
                synchronized (TCMusicManager.this) {
                    loadMusicListener = mLoadMusicListener;
                }
                if (loadMusicListener != null) {
                    loadMusicListener.onDownloadProgress(position, progress);
                }
            }

            @Override
            public void onDownloadSuccess(String filePath) {
                Log.i(TAG, "onDownloadSuccess, filePath = " + filePath);
                LoadMusicListener loadMusicListener = null;
                synchronized (TCMusicManager.this) {
                    loadMusicListener = mLoadMusicListener;
                }
                if (loadMusicListener != null) {
                    loadMusicListener.onBgmDownloadSuccess(position, filePath);
                }
                // 本地保存，防止重复下载
                synchronized (TCMusicManager.this) {
                    mPrefs.edit().putString(bgmName, filePath).apply();
                }
            }
        });
    }

    public void setOnLoadMusicListener(LoadMusicListener loadMusicListener) {
        synchronized (this) {
            mLoadMusicListener = loadMusicListener;
        }
    }

    public interface LoadMusicListener {

        void onBgmList(final ArrayList<TCMusicInfo> tcBgmInfoList);

        void onBgmDownloadSuccess(int position, String filePath);

        void onDownloadFail(int position, String errorMsg);

        void onDownloadProgress(int position, int progress);
    }
}

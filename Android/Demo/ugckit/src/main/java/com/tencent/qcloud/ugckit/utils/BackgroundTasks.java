package com.tencent.qcloud.ugckit.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;


public class BackgroundTasks {

    private BackgroundTasks() {

    }

    @NonNull
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    @NonNull
    public Handler getHandler() {
        return mHandler;
    }

    private static BackgroundTasks instance;

    public static BackgroundTasks getInstance() {
        if (instance == null) {
            synchronized (BackgroundTasks.class) {
                if (instance == null) {
                    instance = new BackgroundTasks();
                }
            }
        }
        return instance;
    }
}

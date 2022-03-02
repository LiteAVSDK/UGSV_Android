package com.tencent.qcloud.ugckit.utils;

import android.widget.Toast;

import com.tencent.qcloud.ugckit.UGCKit;

/**
 * UI通用方法类
 */
public class ToastUtil {
    public static final void toastLongMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UGCKit.getAppContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }


    public static final void toastShortMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UGCKit.getAppContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

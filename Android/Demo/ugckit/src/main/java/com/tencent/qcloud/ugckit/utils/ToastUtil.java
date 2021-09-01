package com.tencent.qcloud.ugckit.utils;

import androidx.annotation.Nullable;
import android.widget.Toast;

import com.tencent.qcloud.ugckit.UGCKit;

/**
 * UI通用方法类
 */
public class ToastUtil {

    @Nullable
    private static Toast mToast;

    public static final void toastLongMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                mToast = Toast.makeText(UGCKit.getAppContext(), message, Toast.LENGTH_LONG);
                mToast.show();
            }
        });
    }


    public static final void toastShortMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                mToast = Toast.makeText(UGCKit.getAppContext(), message, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }
}

package com.tencent.qcloud.ugckit.utils;

import android.widget.Toast;

import com.tencent.qcloud.ugckit.UGCKit;

/**
 * UI通用方法类
 */
public class ToastUtil {
    private static Toast mLastToast = null;

    public static final void toastLongMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(() -> showMessage(message, Toast.LENGTH_LONG));
    }

    public static final void toastShortMessage(final String message) {
        BackgroundTasks.getInstance().runOnUiThread(() -> showMessage(message, Toast.LENGTH_SHORT));
    }

    private static void showMessage(String message, int showLengthEnum) {
        if (mLastToast != null) {
            mLastToast.cancel();
        }
        mLastToast = Toast.makeText(UGCKit.getAppContext(), message, showLengthEnum);
        mLastToast.show();
        final int toastHashCode = mLastToast.hashCode();
        BackgroundTasks.getInstance().postDelayed(() -> releaseToast(toastHashCode), 5000);
    }

    private static void releaseToast(int toastHashCode) {
        if (mLastToast == null || mLastToast.hashCode() != toastHashCode) {
            return;
        }
        mLastToast = null;
    }
}

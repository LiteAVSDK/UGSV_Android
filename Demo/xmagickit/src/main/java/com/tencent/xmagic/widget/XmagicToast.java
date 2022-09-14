package com.tencent.xmagic.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Toast 不能自定义时长, 这里简单做法是: 到时间了重新 new 一个 Toast
 */
public class XmagicToast {

    private static final String TAG = "CustomToast";

    private Toast mToast;
    private Context mContext;
    private String mText;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable mRunnableAlwaysShow = new Runnable() {
        @Override
        public void run() {
            if (mToast == null) {
                mToast = Toast.makeText(mContext, null, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            }
            mToast.setText(mText);
            mToast.show();
            mHandler.postDelayed(mRunnableAlwaysShow, 3500/*Toast.LENGTH_LONG=3500*/);
        }
    };

    private final Runnable mRunnableDismiss = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mRunnableAlwaysShow);
            if (mToast != null) {
                mToast.cancel();
            }
            Log.d(TAG, "toast dismissed.");
        }
    };

    public void show(Context context, final String text, final long duration) {
        mContext = context;
        mText = text;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(mRunnableAlwaysShow);
                mHandler.postDelayed(mRunnableDismiss, duration);
                Log.d(TAG, "show toast '" + text + "' " + duration + "ms");
            }
        });
    }

    public void dismiss() {
        Log.d(TAG, "dismiss toast by user.");
        mHandler.removeCallbacks(mRunnableDismiss);//移除 show() 时 postDelayed 的, 避免重复
        mHandler.post(mRunnableDismiss);
    }
}
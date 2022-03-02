package com.tencent.qcloud.xiaoshipin.mainui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.utils.LogReport;

import java.lang.ref.WeakReference;

/**
 * 小视频"闪屏界面"
 */
public class TCSplashActivity extends Activity {

    private static final String TAG = "TCSplashActivity";

    private static final int START_LOGIN = 2873;
    private final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Message msg = Message.obtain();
        msg.arg1 = START_LOGIN;
        mHandler.sendMessageDelayed(msg, 1000);

        boolean firstRun = isFirstRun(this);
        Log.i(TAG, "firstRun:" + firstRun);
        if (firstRun) {
            saveFirstRun(this);

            LogReport.getInstance().reportInstall();
        }
    }

    private void saveFirstRun(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("share", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isFirstRun", false).commit();
    }

    public static boolean isFirstRun(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("share", Context.MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            editor.putBoolean("isFirstRun", false);
            editor.commit();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        //splashActivity下不允许back键退出
        //super.onBackPressed();
    }

    private void jumpToMainActivity() {
        Intent intent = new Intent(this, TCMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<TCSplashActivity> mActivity;

        public MyHandler(TCSplashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TCSplashActivity activity = mActivity.get();
            if (activity != null) {
                activity.jumpToMainActivity();
            }
        }
    }

}

package com.tencent.qcloud.xiaoshipin;

import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.module.upload.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.TCHttpEngine;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.xiaoshipin.config.TCConfigManager;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLog;
import com.tencent.ugc.TXUGCBase;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * 小视频应用类，用于全局的操作，如
 * sdk初始化,全局提示框
 */
public class TCApplication extends MultiDexApplication {
    private static final String TAG = "TCApplication";

//    private RefWatcher mRefWatcher;

    private static TCApplication instance;

    // 请替换成您的Licence信息
    private String ugcKey = "xxxxx";
    private String ugcLicenceUrl = "http://xxx/TXUgcSDK.licence";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        TCConfigManager.init(this);
        initSDK();

//        mRefWatcher = LeakCanary.install(this);

        // 短视频licence设置
        TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
        UGCKit.init(this);

        // ELK数据上报：启动次数
        LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_START_UP, 0, "");

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks(this));
    }

    public static TCApplication getApplication() {
        return instance;
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        TCApplication application = (TCApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    /**
     * 初始化SDK，包括Bugly，LiteAVSDK等
     */
    public void initSDK() {
        //启动bugly组件，bugly组件为腾讯提供的用于crash上报和分析的开放组件，如果您不需要该组件，可以自行移除
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(), UGCKitConstants.BUGLY_APPID, true, strategy);

        TCUserMgr.getInstance().initContext(getApplicationContext());
        TCHttpEngine.getInstance().initContext(getApplicationContext());

        TXLog.w(TAG, "app init sdk");
    }

    private class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        private int foregroundActivities;
        private boolean isChangingConfiguration;
        private long time;

        public MyActivityLifecycleCallbacks(TCApplication tcApplication) {

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            foregroundActivities++;
            if (foregroundActivities == 1 && !isChangingConfiguration) {
                // 应用进入前台
                time = System.currentTimeMillis();
            }
            isChangingConfiguration = false;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            foregroundActivities--;
            if (foregroundActivities == 0) {
                // 应用切入后台
                long bgTime = System.currentTimeMillis();
                long diff = (bgTime - time) / 1000;
                // ELK数据上报：使用时间
                LogReport.getInstance().uploadLogs(LogReport.ELK_ACTION_STAY_TIME, diff, "");
            }
            isChangingConfiguration = activity.isChangingConfigurations();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}

package com.tencent.qcloud.xiaoshipin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.multidex.MultiDexApplication;

import com.tencent.feedback.eup.CrashReport;
import com.tencent.feedback.anr.ANRReport;

import com.tencent.qcloud.ugckit.UGCKit;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
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
    private String ugcKey = "请替换成您的licenseKey";
    private String ugcLicenceUrl = "请替换成您的licenseUrl";

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

        TCUserMgr.getInstance().initContext(getApplicationContext());
        TXLog.w(TAG, "app init sdk");

        //启动bugly组件，bugly组件为腾讯提供的用于crash上报和分析的开放组件，如果您不需要该组件，可以自行移除
        final Context appContext = getApplicationContext();
        CrashReport.setProductVersion(appContext, TXLiveBase.getSDKVersionStr());
        CrashReport.setUserId(appContext, TCUserMgr.getInstance().getUserId());

        /** 这条语句已经能让你的java端异常被捕获，并且上报了。BuglyOA 在后台通过包名和 App ID 匹配，buglyoa网站上 BundleId 须和包名保持一致。 */
        CrashReport.initCrashReport(appContext);
        String tombDirectoryPath = appContext.getDir("tomb", Context.MODE_PRIVATE).getAbsolutePath();
        /**
         * 前提是你需要先初始化java端异常上报功能，
         * 这条语句已经能让你的native端异常被捕获，并且上报了。
         *
         * @param context
         * @param tombDirectoryPath（也可以默认设置为空）
         *            tomb文件的存放路径,tomb文件可以理解为详细的堆栈信息，平均每一个异常会产生一个tomb文件，
         *            平均10k
         * @param openNativeLog
         *            打开Native Log功能,true则输出debug级log，false则只有warn及error有log输出。
         */
        CrashReport.initNativeCrashReport(appContext, tombDirectoryPath, true);
        // 开启ANR监控,注意ANR的初始化一定要放在native sdk初始化之后，否则不生效
        ANRReport.startANRMonitor(appContext);
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

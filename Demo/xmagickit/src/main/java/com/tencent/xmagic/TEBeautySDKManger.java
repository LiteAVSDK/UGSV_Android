package com.tencent.xmagic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;

import com.tencent.effect.beautykit.TEBeautyKit;
import com.tencent.effect.beautykit.config.TEUIConfig;
import com.tencent.effect.beautykit.model.TEPanelViewResModel;
import com.tencent.effect.beautykit.utils.LogUtils;
import com.tencent.xmagic.telicense.TELicenseCheck;
import com.tencent.xmagic.utils.AppUtils;
import com.tencent.xmagic.utils.SPUtils;

import java.io.File;
import java.util.Locale;

public class TEBeautySDKManger {
    private static final String TAG = "TEBeautySDKManger";
    public static final String IS_BEAUTY_RES_COPIED = "beauty_resource_copied";

    private static String xmagicAuthKey = "";
    private static String xmagicAuthLicenceUrl = "";
    public static Context applicationContext = null;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static volatile TEBeautyCopyResCallBack beautyCopyResCallBack;
    private static volatile boolean isCoping = false;

    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        applicationContext = context.getApplicationContext();
        TEBeautyKit.setResPath(new File(applicationContext.getFilesDir(), "ugc_xmagic").getAbsolutePath());
    }

    public static void setXmagicAuthKeyAndUrl(String authLicenceUrl, String authKey) {
        if (authLicenceUrl == null || authKey == null) {
            throw new IllegalArgumentException("Auth URL and key cannot be null");
        }
        TEBeautySDKManger.xmagicAuthKey = authKey;
        TEBeautySDKManger.xmagicAuthLicenceUrl = authLicenceUrl;
    }

    /**
     * 进行美颜授权检验,注：调用此方法之前要保证init方法已被调用
     *
     * @param listener 授权检查监听器
     */
    public static void checkAuth(final TELicenseCheck.TELicenseCheckListener listener) {
        if (applicationContext == null) {
            throw new IllegalStateException("Please call init() first");
        }
        TEBeautyKit.setTELicense(applicationContext, xmagicAuthLicenceUrl, xmagicAuthKey, listener);
    }

    public static void initPanelConfig() {
        TEPanelViewResModel resModel = new TEPanelViewResModel();
        resModel.beauty = "beauty_panel_ugc/beauty.json";
        resModel.lut = "beauty_panel_ugc/lut.json";
        resModel.motion = "beauty_panel_ugc/motions.json";
        resModel.lightMakeup = "beauty_panel_ugc/light_makeup.json";
        resModel.segmentation = "beauty_panel_ugc/segmentation.json";
        TEUIConfig.getInstance().setTEPanelViewRes(resModel);
        TEUIConfig.getInstance().setSystemLocal(Locale.getDefault());
    }

    public static void setBeautyCopyResCallBack(TEBeautyCopyResCallBack callBack) {
        beautyCopyResCallBack = callBack;
    }

    public static synchronized void copyRes() {
        if (isCopyRes()) {
            notifyCallback(true);
            return;
        }

        if (isCoping) {
            LogUtils.i(TAG, "the xmagic res is coping");
            return;
        }

        isCoping = true;
        new Thread(() -> {
            boolean result = TEBeautyKit.copyRes(applicationContext);
            if (result) {
                saveCopyData();
            }
            isCoping = false;
            notifyCallback(result);
        }).start();
    }

    private static void notifyCallback(boolean result) {
        handler.post(() -> {
            if (beautyCopyResCallBack != null) {
                beautyCopyResCallBack.onCallback(result);
            }
        });
    }

    public interface TEBeautyCopyResCallBack {
        void onCallback(boolean success);
    }

    private static boolean isCopyRes() {
        String appVersionName = AppUtils.getAppVersionName(applicationContext);
        String savedVersionName = SPUtils.getSharedPreferences(applicationContext)
                .getString(IS_BEAUTY_RES_COPIED, "");
        return savedVersionName.equals(appVersionName);
    }

    private static void saveCopyData() {
        String appVersionName = AppUtils.getAppVersionName(applicationContext);
        SPUtils.getSharedPreferences(applicationContext).edit()
                .putString(IS_BEAUTY_RES_COPIED, appVersionName).apply();
    }

    public static void setBeautyStateOpen() {
        Editor editor = applicationContext.getSharedPreferences("xmagic_settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("xmagic_state", true).apply();
    }

    public static boolean getUserBeauty() {
        SharedPreferences sp = applicationContext.getSharedPreferences("xmagic_settings", Context.MODE_PRIVATE);
        return sp.getBoolean("xmagic_state", false);
    }
}

package com.tencent.qcloud.xiaoshipin.manager;

import android.content.ContentResolver;
import android.content.Context;

import com.tencent.ugc.TXUGCBase;
import com.tencent.xmagic.TEBeautySDKManger;

public class LicenseManager {
    // clang-format off
    private static String ugcLicenceUrl = "Please replace it with your licenseUrl";
    private static String ugcKey = "Please replace it with your licenseKey";

    private static String xmagicAuthLicenceUrl = "Please replace it with your licenseUrl";
    private static String xmagicAuthKey = "Please replace it with your licenseKey";
    // clang-format on

    public static void setUgcLicense(Context context) {
        TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
    }

    public static void setXMagicLicense() {
        TEBeautySDKManger.setXmagicAuthKeyAndUrl(xmagicAuthLicenceUrl, xmagicAuthKey);
    }

    public static void setLicense(Context context) {
        TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
        TEBeautySDKManger.setXmagicAuthKeyAndUrl(xmagicAuthLicenceUrl, xmagicAuthKey);
    }
}

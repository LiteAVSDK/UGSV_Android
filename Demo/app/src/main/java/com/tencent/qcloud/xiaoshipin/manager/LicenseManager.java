package com.tencent.qcloud.xiaoshipin.manager;

import android.content.ContentResolver;
import android.content.Context;

import com.tencent.ugc.TXUGCBase;
import com.tencent.xmagic.XMagicImpl;

public class LicenseManager {
    private static String ugcLicenceUrl = "请替换成您的licenseUrl";
    private static String ugcKey = "请替换成您的licenseKey";

    private static String xmagicAuthLicenceUrl = "请替换成您的licenseUrl";
    private static String xmagicAuthKey = "请替换成您的licenseKey";

    public static void setUgcLicense(Context context) {
        TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
    }

    public static void setXMagicLicense() {
        XMagicImpl.setXmagicAuthKeyAndUrl(xmagicAuthLicenceUrl, xmagicAuthKey);
    }

    public static void setLicense(Context context) {
        TXUGCBase.getInstance().setLicence(context, ugcLicenceUrl, ugcKey);
        XMagicImpl.setXmagicAuthKeyAndUrl(xmagicAuthLicenceUrl, xmagicAuthKey);
    }
}

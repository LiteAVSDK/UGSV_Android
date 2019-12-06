package com.tencent.qcloud.ugckit;

import android.content.Context;

import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.ugc.TXUGCBase;

public class UGCKitImpl {
    private static Context sAppContext;

    public static void init(Context context) {
        sAppContext = context;
    }

    public static Context getAppContext() {
        return sAppContext;
    }

}

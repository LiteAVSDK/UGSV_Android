package com.tencent.xmagic.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SPUtils {
    private static final String SH_NAME = "demo_settings";

    private static SharedPreferences sharedPreferences = null;

    public static SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context
                    .getSharedPreferences(SH_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }


}

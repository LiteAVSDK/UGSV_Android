package com.tencent.qcloud.xiaoshipin.config;


import android.content.Context;
import android.content.SharedPreferences;

public class TCConfigManager {

    private static Context mContext;

    public static void init(Context ctx) {
        mContext = ctx;
    }

    //到系统复杂之后，配置设置可能会有很多个模块
    //每一个模块，在这里声明一个配置域字符串（必须要跟其它的不同），用作sharedPreferences的name参数
    //每个模块的函数都单独放到一个静态类里面以方便分类
    private static final String CONFIG_SPACE_SYSTEM = "CONFIG_SPACE_SYSTEM";


    public static class SystemConfig {

        private static class SystemConfigKeys {
            private static final String LANGUAGE = "LANGUAGE";
        }

        public static class Languages {
            public final static int FOLLOW_SYSTEM = 0;
            public final static int SIMPLIFIED_CHINESE = 1;
            public final static int TRADITIONAL_CHINESE = 2;
            public final static int ENGLISH = 3;
        }

        public static int getLanguage() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(CONFIG_SPACE_SYSTEM, Context.MODE_PRIVATE);
            int lang = sharedPreferences.getInt(SystemConfigKeys.LANGUAGE, -1);
            if (lang == -1) {
                setLanguage(Languages.FOLLOW_SYSTEM);
                return Languages.FOLLOW_SYSTEM;
            } else {
                return lang;
            }
        }

        public static void setLanguage(int lang) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(CONFIG_SPACE_SYSTEM, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(SystemConfigKeys.LANGUAGE, lang);
            editor.commit();
        }
    }

}

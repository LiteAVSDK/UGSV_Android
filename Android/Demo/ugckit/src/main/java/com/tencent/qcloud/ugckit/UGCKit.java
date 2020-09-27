package com.tencent.qcloud.ugckit;

import android.content.Context;

public class UGCKit {
    private static Context sAppContext;

    /**
     * UGCKit的初始化函数
     *
     * @param context 应用的上下文，一般为对应应用的ApplicationContext
     */
    public static void init(Context context) {
        sAppContext = context;
    }

    /**
     * 获取UGCKit保存的上下文Context，该Context会长期持有，所以应该为Application级别的上下文
     *
     * @return
     */
    public static Context getAppContext() {
        return sAppContext;
    }

}

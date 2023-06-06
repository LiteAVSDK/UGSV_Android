package com.tencent.xmagic;

import android.content.Context;
import android.util.Log;

import com.tencent.xmagic.module.XmagicResParser;
import com.tencent.xmagic.panel.XmagicPanelDataManager;
import com.tencent.xmagic.widget.XmagicToast;
import com.tencent.xmagic.XmagicApi.OnXmagicPropertyErrorListener;
import java.io.File;
import java.io.FileOutputStream;

public class XmagicApiWrapper {

    private static String TAG = XmagicApiWrapper.class.getSimpleName();



    public static XmagicApi createXmagicApi(final Context context, boolean isAddDefaultBeauty,
                                            OnXmagicPropertyErrorListener errorListener) {
        XmagicApi commonXmagicApi = new XmagicApi(context, XmagicResParser.getResPath(), errorListener);
        //开发调试时，可以把日志级别设置为DEBUG，发布包请设置为 WARN，否则会影响性能
        commonXmagicApi.setXmagicLogLevel(Log.WARN);
        if (isAddDefaultBeauty) {
            commonXmagicApi.updateProperties(XmagicPanelDataManager.getInstance().getDefaultBeautyData());
        }


        commonXmagicApi.setTipsListener(new XmagicApi.XmagicTipsListener() {
            final XmagicToast mToast = new XmagicToast();

            @Override
            public void tipsNeedShow(String tips, String tipsIcon, int type, int duration) {
                mToast.show(context, tips, duration);
            }

            @Override
            public void tipsNeedHide(String tips, String tipsIcon, int type) {
                mToast.dismiss();
            }
        });
        return commonXmagicApi;
    }




}

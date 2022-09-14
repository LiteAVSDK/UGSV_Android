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
        commonXmagicApi.setAIDataListener(new XmagicApi.XmagicAIDataListener() {
            @Override
            public void onBodyDataUpdated(Object bodyDataList) {
//                Log.d(TAG, "onBodyDataUpdated");
            }

            @Override
            public void onHandDataUpdated(Object handDataList) {
//                Log.d(TAG, "onHandDataUpdated");
            }

            @Override
            public void onFaceDataUpdated(Object faceDataList) {
                //日志太多，影响性能，注释掉，需要时再打开
//                Log.d(TAG, "onFaceDataUpdated");
            }
        });

        commonXmagicApi.setYTDataListener(new XmagicApi.XmagicYTDataListener() {
            @Override
            public void onYTDataUpdate(String data) {
//                Log.d(TAG, "onYTDataUpdate，data=" + data);
                //log太长，需要时请打开下面的开关，写入文件，便于测试
                if (false) {
                    writeFaceData(data);
                }
            }
        });

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


    public interface OnCheckBeautyAuthComplete {
        void onCheckComplete();
    }

    private static void writeFaceData(String data) {
        data += "\n";
        String filePath = "/sdcard/onYTDataUpdate.txt";
        Log.e(TAG, "writeFaceData: " + filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();

            }
            FileOutputStream outStream = new FileOutputStream(file, true);
            outStream.write(data.getBytes());
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

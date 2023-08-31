package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;

import com.tencent.cos.xml.CosXmlBaseService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.qcloud.core.logger.QCloudLogger;

@SuppressWarnings("UnusedReturnValue")
public class TVCLog {

    private static final String MODULE_TAG = "[TVCUpload]";

    /**
     * 必须调用，调用之后才会添加日志adapter开始打印
     *
     * @param isDebug 是否打印日志
     */
    public static void setDebuggable(boolean isDebug, Context context) {
        // target qCloud log config
        new CosXmlBaseService(context, new CosXmlServiceConfig.Builder().setDebuggable(isDebug).builder());
    }

    public static void v(String tag, String msg) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.v(tag, traceMsg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.v(tag, tr, traceMsg);
    }

    public static void d(String tag, String msg) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.d(tag, traceMsg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.d(tag, tr, traceMsg);
    }

    public static void i(String tag, String msg) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.i(tag, traceMsg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.i(tag, tr, traceMsg);
    }

    public static void w(String tag, String msg) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.w(tag, traceMsg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.w(tag, tr, traceMsg);
    }

    public static void e(String tag, String msg) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.e(tag, traceMsg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        final String traceMsg = MODULE_TAG + msg;
        QCloudLogger.e(tag, tr, traceMsg);
    }
}

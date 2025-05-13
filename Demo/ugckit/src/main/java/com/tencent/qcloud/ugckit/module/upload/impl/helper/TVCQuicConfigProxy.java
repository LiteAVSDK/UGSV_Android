package com.tencent.qcloud.ugckit.module.upload.impl.helper;

import com.tencent.qcloud.ugckit.module.upload.impl.TVCLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TVCQuicConfigProxy {

    private static final String TAG = "TVCQuicConfigProxy";

    private Class<?> mBuilderClass;
    private Object mBuilderObj;

    public TVCQuicConfigProxy() {
        try {
            mBuilderClass = Class.forName("com.tencent.tquic.impl.TnetConfig$Builder");
            Constructor<?> builderConstructor = mBuilderClass.getConstructor();
            mBuilderObj = builderConstructor.newInstance();
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
    }

    public void setIsCustom(boolean isCustom) {
        try {
            Method setIsCustomMethod = mBuilderClass.getMethod("setIsCustom", boolean.class);
            setIsCustomMethod.invoke(mBuilderObj, isCustom);
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
    }

    public void setTotalTimeoutMillis(int totalTimeoutMillis) {
        try {
            Method setTotalTimeoutMillisMethod = mBuilderClass.getMethod("setTotalTimeoutMillis", int.class);
            setTotalTimeoutMillisMethod.invoke(mBuilderObj, totalTimeoutMillis);
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        try {
            Method setConnectTimeoutMillisMethod = mBuilderClass.getMethod("setConnectTimeoutMillis", int.class);
            setConnectTimeoutMillisMethod.invoke(mBuilderObj, connectTimeoutMillis);
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
    }

    public Object build() {
        try {
            Method buildMethod = mBuilderClass.getMethod("build");
            return buildMethod.invoke(mBuilderObj);
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
        return null;
    }

    public void buildAndSetToGlobal() {
        try {
            Object configObj = build();
            Class<?> configClass = Class.forName("com.tencent.tquic.impl.TnetConfig");
            Class<?> quicClientImplClass = Class.forName("com.tencent.qcloud.quic.QuicClientImpl");
            Method setConfigMethod = quicClientImplClass.getMethod("setTnetConfig", configClass);
            // 静态方法第一个参数传 null
            //noinspection JavaReflectionInvocation
            setConfigMethod.invoke(null, configObj);
        } catch (Exception e) {
            TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
        }
    }
}

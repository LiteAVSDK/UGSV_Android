package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.module.upload.impl.helper.TVCQuicConfigProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * quic process class
 * quic处理类
 */
public class QuicClient {

    private static final String TAG = "QuicClient";

    public static final int ERROR_CODE_QUIC_TIME_OUT = -1;
    public static final int ERROR_CODE_QUIC_FAILED = -2;
    public static final int PORT = 443;

    private final Handler mHandler;
    private Object mQuicClienObj;
    private QuicDetectListener mQuicDetectListener;
    private String mParams;
    private String mHost;
    private volatile boolean isCallback = false;
    private long reqStartTime;

    public QuicClient(Context context) {
        mHandler = new Handler(context.getMainLooper());
    }

    private final Object mNetworkCallback = createQuicCallback();

    private Object createQuicCallback() {
        // create poxy Objects through Reflection
        try {
            final Class<?> callbackInterface = Class.forName("com.tencent.ugcupload.ugcquic.impl.UGCQuicCallback");
            final Class<?> quicProxyClass = Class.forName("com.tencent.ugcupload.ugcquic.impl.UGCQuicClientProxy");
            return Proxy.newProxyInstance(
                    callbackInterface.getClassLoader(),
                    new Class[]{callbackInterface},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String methodName = method.getName();

                            // hande Basic Methods of Object
                            if ("toString".equals(methodName)) {
                                return "UGCQuicCallbackProxy@" + Integer.toHexString(hashCode());
                            }
                            if ("hashCode".equals(methodName)) {
                                return System.identityHashCode(proxy);
                            }

                            switch (methodName) {
                                case "onConnect":
                                    int error_code = (int) args[0];
                                    if (error_code == 0) {
                                        Method addHeadersMethod = quicProxyClass.getMethod("addHeaders",
                                                String.class, String.class);
                                        addHeadersMethod.invoke(mQuicClienObj, ":method", "HEAD");

                                        Method sendRequestMethod = quicProxyClass.getMethod("sendRequest",
                                                byte[].class, int.class, boolean.class);
                                        sendRequestMethod.invoke(mQuicClienObj, new byte[0], 0, true);
                                    } else {
                                        notifyCallback(false, error_code);
                                    }
                                    break;
                                case "onHeaderRecv":
                                    String header = (String) args[0];
                                    notifyCallback(true, 0);
                                    TVCLog.i(TAG, mHost + " responseData:" + header);
                                    break;
                                case "onDataRecv":
                                    byte[] body = (byte[]) args[0];
                                    notifyCallback(true, 0);
                                    String responseData = new String(body, StandardCharsets.ISO_8859_1);
                                    TVCLog.i(TAG, mHost + " responseData:" + responseData);
                                    break;
                                // 其他方法若无需逻辑可留空
                                case "onNetworkLinked":
                                case "onComplete":
                                case "onClose":
                                    break;
                            }
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            TVCLog.e(TAG, "failed to create UGCQuicCallback through reflection, may not depend quic", e);
            return null;
        }
    }

    private final Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCallback) {
                notifyCallback(false, ERROR_CODE_QUIC_TIME_OUT);
            }
        }
    };

    private void notifyCallback(boolean isQuic, int code) {
        if (isCallback) {
            return;
        }
        this.isCallback = true;
        mHandler.removeCallbacks(timeOutRunnable);
        if (null != mQuicDetectListener) {
            long requestTime = System.currentTimeMillis() - reqStartTime;
            mQuicDetectListener.onQuicDetectDone(isQuic, requestTime, code);
        }
    }

    /**
     * Check if the QUIC link is connected
     * <p>1. Whether the URL supports QUIC</p>
     * <p>2. Whether the current network environment supports QUIC/UDP links</p>
     * <h1>3. Need to run in a sub-thread</h1>
     *
     * 查询quic链路是否连通
     * <p>1、url是否支持quic</p>
     * <p>2、当前网络环境是否支持quic/udp链路</p>
     * <h1>3、需要在子线程运行</h1>
     */
    public void detectQuic(final String domain, final QuicDetectListener listener) {
        this.mQuicDetectListener = listener;
        String reqUrl = "http://" + domain;
        Uri originUri = Uri.parse(reqUrl);
        this.mHost = originUri.getHost();
        String domainIp = null;
        List<String> ipList = TXUGCPublishOptCenter.getInstance().query(domain);
        if (null != ipList && !ipList.isEmpty()) {
            domainIp = ipList.get(0);
        }
        if (!TextUtils.isEmpty(domainIp)) {
            if (null != originUri.getQuery()) {
                this.mParams = originUri.getPath() + "?" + originUri.getQuery();
            } else {
                this.mParams = originUri.getPath();
            }
            TVCQuicConfigProxy quicConfigProxy = new TVCQuicConfigProxy();
            quicConfigProxy.setIsCustom(false);
            quicConfigProxy.setTotalTimeoutMillis((int) TVCConstants.PRE_UPLOAD_QUIC_DETECT_TIMEOUT);
            Object configObj = quicConfigProxy.build();
            if (null != configObj) {
                // 使用反射创建 TnetQuicRequest 的实例
                try {
                    Class<?> quicRequestClass = Class.forName("com.tencent.ugcupload.ugcquic.impl.UGCQuicClientProxy");
                    Class<?> callbackClass = Class.forName("com.tencent.ugcupload.ugcquic.impl.UGCQuicCallback");
                    Class<?> configClass = Class.forName("com.tencent.tquic.impl.TnetConfig");
                    Constructor<?> quicRequestConstructor =
                            quicRequestClass.getConstructor(callbackClass, configClass, int.class);
                    mQuicClienObj = quicRequestConstructor.newInstance(mNetworkCallback, configObj, 0);
                    Method connectMethod = quicRequestClass.getMethod("connect", String.class, String.class);
                    connectMethod.invoke(mQuicClienObj, mHost, domainIp);
                    reqStartTime = System.currentTimeMillis();
                    mHandler.postDelayed(timeOutRunnable, TVCConstants.PRE_UPLOAD_QUIC_DETECT_TIMEOUT);
                } catch (Exception e) {
                    TVCLog.e(TAG, "quic opt failed, may not depend quic:" + e);
                    notifyCallback(false, ERROR_CODE_QUIC_FAILED);
                }
            } else {
                notifyCallback(false, ERROR_CODE_QUIC_FAILED);
            }
        } else {
            notifyCallback(false, ERROR_CODE_QUIC_FAILED);
        }
    }

    public interface QuicDetectListener {
        void onQuicDetectDone(boolean isQuic, long requestTime, int errorCode);
    }

}

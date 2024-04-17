package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.tencent.tquic.impl.TnetConfig;
import com.tencent.tquic.impl.TnetQuicRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * quic处理类
 */
public class QuicClient {

    private static final String TAG = "QuicClient";

    public static final int ERROR_CODE_QUIC_TIME_OUT = -1;
    public static final int ERROR_CODE_QUIC_FAILED = -2;
    public static final int PORT = 443;

    private final Handler mHandler;
    private TnetQuicRequest mQuicNative;
    private QuicDetectListener mQuicDetectListener;
    private String mParams;
    private String mHost;
    private volatile boolean isCallback = false;
    private long reqStartTime;

    public QuicClient(Context context) {
        mHandler = new Handler(context.getMainLooper());
    }

    private final TnetQuicRequest.Callback networkCallback = new TnetQuicRequest.Callback() {
        @Override
        public void onConnect(int error_code) throws Exception {
            if (error_code == 0) {
                mQuicNative.addHeaders(":method", "HEAD");
                mQuicNative.sendRequest(new byte[0], 0, true);
            } else {
                notifyCallback(false, error_code);
            }
        }

        @Override
        public void onNetworkLinked() throws Exception {

        }

        @Override
        public void onHeaderRecv(String header) throws Exception {
            notifyCallback(true, 0);
            TVCLog.i(TAG, mHost + " responseData:" + header);
        }

        @Override
        public void onDataRecv(byte[] body) throws Exception {
            notifyCallback(true, 0);
            String responseData = new String(body, StandardCharsets.ISO_8859_1);
            TVCLog.i(TAG, mHost + " responseData:" + responseData);
        }

        @Override
        public void onComplete(int stream_error) throws Exception {

        }

        @Override
        public void onClose(int error_code, String error_str) throws Exception {

        }
    };

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
            TnetConfig config = new TnetConfig.Builder()
                    .setIsCustom(false)
                    .setTotalTimeoutMillis((int) TVCConstants.PRE_UPLOAD_QUIC_DETECT_TIMEOUT)
                    .build();
            mQuicNative = new TnetQuicRequest(networkCallback, config, 0);
            mQuicNative.connect(mHost, domainIp);
            reqStartTime = System.currentTimeMillis();
            mHandler.postDelayed(timeOutRunnable, TVCConstants.PRE_UPLOAD_QUIC_DETECT_TIMEOUT);
        } else {
            notifyCallback(false, ERROR_CODE_QUIC_FAILED);
        }
    }

    public interface QuicDetectListener {
        void onQuicDetectDone(boolean isQuic, long requestTime, int errorCode);
    }

}

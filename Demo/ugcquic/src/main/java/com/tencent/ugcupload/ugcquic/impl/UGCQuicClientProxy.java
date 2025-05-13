package com.tencent.ugcupload.ugcquic.impl;

import com.tencent.tquic.impl.TnetConfig;
import com.tencent.tquic.impl.TnetQuicRequest;
import com.tencent.tquic.impl.TnetStats;

public class UGCQuicClientProxy {

    private final TnetQuicRequest mQuicNative;
    private final UGCQuicCallback mCallback;

    public UGCQuicClientProxy(UGCQuicCallback callback, TnetConfig jConfig, int type) {
        mCallback = callback;
        mQuicNative = new TnetQuicRequest(createCallback(), jConfig, type);
    }

    private TnetQuicRequest.Callback createCallback() {
        return new TnetQuicRequest.Callback() {
            @Override
            public void onConnect(int error_code) throws Exception {
                if (null != mCallback) {
                    mCallback.onConnect(error_code);
                }
            }

            @Override
            public void onNetworkLinked() throws Exception {
                if (null != mCallback) {
                    mCallback.onNetworkLinked();
                }
            }

            @Override
            public void onHeaderRecv(String header) throws Exception {
                if (null != mCallback) {
                    mCallback.onHeaderRecv(header);
                }
            }

            @Override
            public void onDataRecv(byte[] body) throws Exception {
                if (null != mCallback) {
                    mCallback.onDataRecv(body);
                }
            }

            @Override
            public void onComplete(int stream_error) throws Exception {
                if (null != mCallback) {
                    mCallback.onComplete(stream_error);
                }
            }

            @Override
            public void onClose(int error_code, String error_str) throws Exception {
                if (null != mCallback) {
                    mCallback.onClose(error_code, error_str);
                }
            }
        };
    }

    public void addHeaders(String key, String value) {
        mQuicNative.addHeaders(key, value);
    }

    public void connect(String url, String ipAddress) {
        mQuicNative.connect(url, ipAddress);
    }

    public boolean connectAndSend(String url,
                               String ipAddress,
                               byte[] body,
                               int length) {
        return mQuicNative.connectAndSend(url, ipAddress, body, length);
    }

    public void connectWithDomain(String url) {
        mQuicNative.connectWithDomain(url);
    }

    public boolean sendRequest(byte[] body, int length, boolean fin) {
        return mQuicNative.sendRequest(body, length, fin);
    }

    public void CancelRequest() {
        mQuicNative.CancelRequest();
    }

    public boolean isConnectCompleted() {
        return mQuicNative.isConnectCompleted();
    }

    public boolean isRequestFinished() {
        return mQuicNative.isRequestFinished();
    }

    public void Destroy() {
        mQuicNative.Destroy();
    }

    public void GetTnetStates(TnetStats jstats) {
        mQuicNative.GetTnetStates(jstats);
    }

    public TnetQuicRequest getQuicRequest() {
        return mQuicNative;
    }
}

package com.tencent.ugcupload.ugcquic.impl;

public interface UGCQuicCallback {
    void onConnect(int error_code) throws Exception;

    void onNetworkLinked() throws Exception;

    void onHeaderRecv(String header) throws Exception;

    void onDataRecv(byte[] body) throws Exception;

    void onComplete(int stream_error) throws Exception;

    void onClose(int error_code, String error_str) throws Exception;

}

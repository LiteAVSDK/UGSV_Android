package com.tencent.qcloud.ugckit.module.upload.impl.compute;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Protocol;

/**
 * 利用OKHTTP Event事件，用来统计TCP建立链接耗时 以及 收到收个回包的耗时
 */
public class TXOkHTTPEventListener extends EventListener {

    private long startTime;
    private long connectFinishTime;
    private long startRecvRspHeaderTime;


    public long getTCPConnectionTimeCost() {
        return connectFinishTime - startTime;
    }

    public long getRecvRspTimeCost() {
        return startRecvRspHeaderTime - startTime;
    }

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
        connectFinishTime = System.currentTimeMillis();
    }

    @Override
    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        connectFinishTime = System.currentTimeMillis();
    }

    @Override
    public void responseHeadersStart(Call call) {
        super.responseHeadersStart(call);
        startRecvRspHeaderTime = System.currentTimeMillis();
    }
}

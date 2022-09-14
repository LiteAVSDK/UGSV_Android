package com.tencent.qcloud.ugckit.module.upload.impl.compute;

import android.util.Log;

import com.tencent.qcloud.core.http.HttpTaskMetrics;

/**
 * 用于统计 Initiate Multipart Upload 请求耗时
 */
public class TXHttpTaskMetrics extends HttpTaskMetrics {
    private static final String TAG = "TXHttpTaskMetrics";

    private double tcpConnectionTimeCost;
    private double recvRspTimeCost;

    @Override
    public void onDataReady() {
        super.onDataReady();

        recvRspTimeCost = TXHttpTaskMetrics.getRecvRspTimeCost(this);

        tcpConnectionTimeCost = TXHttpTaskMetrics.getTCPConnectionTimeCost(this);

        Log.i(TAG, "onDataReady: tcpConnectionTimeCost = " + tcpConnectionTimeCost + " recvRspTimeCost = " + recvRspTimeCost);

        Log.i(TAG, "onDataReady: " + this.toString());
    }

    public long getTCPConnectionTimeCost() {
        return (long) (tcpConnectionTimeCost * 1000);
    }

    public long getRecvRspTimeCost() {
        return (long) (recvRspTimeCost * 1000);
    }

    public static double getTCPConnectionTimeCost(HttpTaskMetrics httpTaskMetrics) {
        return httpTaskMetrics.dnsLookupTookTime() + httpTaskMetrics.connectTookTime() + httpTaskMetrics.secureConnectTookTime();
    }

    public static double getRecvRspTimeCost(HttpTaskMetrics httpTaskMetrics) {
        return getTCPConnectionTimeCost(httpTaskMetrics) + httpTaskMetrics.writeRequestHeaderTookTime() + httpTaskMetrics.writeRequestBodyTookTime() + httpTaskMetrics.readResponseHeaderTookTime();
    }

}

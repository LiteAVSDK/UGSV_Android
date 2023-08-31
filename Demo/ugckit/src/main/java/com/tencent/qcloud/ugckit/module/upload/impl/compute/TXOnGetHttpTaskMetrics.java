package com.tencent.qcloud.ugckit.module.upload.impl.compute;

import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.qcloud.core.http.HttpTaskMetrics;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCLog;

/**
 * 用于统计 UploadService 第一个请求耗时
 */
public class TXOnGetHttpTaskMetrics implements COSXMLUploadTask.OnGetHttpTaskMetrics {
    private static final String  TAG = "TXOnGetHttpTaskMetrics";
    private              boolean isGet;
    private              double  tcpConnectionTimeCost;
    private              double  recvRspTimeCost;

    public long getTCPConnectionTimeCost() {
        return (long) (tcpConnectionTimeCost * 1000);
    }

    public long getRecvRspTimeCost() {
        return (long) (recvRspTimeCost * 1000);
    }

    public void onGetHttpMetrics(String s, HttpTaskMetrics httpTaskMetrics) {
        if (!isGet) {//是否已经获取到过第一个请求
            isGet = true;

            recvRspTimeCost = TXHttpTaskMetrics.getRecvRspTimeCost(httpTaskMetrics);

            tcpConnectionTimeCost = TXHttpTaskMetrics.getTCPConnectionTimeCost(httpTaskMetrics);

            TVCLog.i(TAG, "onDataReady: tcpConnectionTimeCost = " + tcpConnectionTimeCost + " recvRspTimeCost = " + recvRspTimeCost);

            TVCLog.i(TAG, "onDataReady: " + this.toString());
        }
    }
}

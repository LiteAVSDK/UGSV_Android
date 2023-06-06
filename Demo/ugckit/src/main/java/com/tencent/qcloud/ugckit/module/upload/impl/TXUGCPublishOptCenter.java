package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.qcloud.quic.QuicConfig;
import com.tencent.qcloud.quic.QuicNative;
import com.tencent.qcloud.quic.QuicProxy;
import com.tencent.qcloud.ugckit.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 短视频上传优化：涉及到httpdns、PrepareUploadUGC预先探测最优园区
 */

public class TXUGCPublishOptCenter {

    private static final int WHAT_UPDATE_BEST_COS = 0x01;

    private static final String KEY_COS_REGION = "cosRegion";
    private static final String KEY_COS_DOMAIN = "cosDomain";
    private static final String KEY_IS_QUIC = "isQUic";
    private static final String KEY_REQUEST_TIME = "requestTime";

    private class CosRegionInfo {
        private String region = "";
        private String domain = "";
        private boolean isQuic = false;
    }

    private static final String TAG = "TVC-OptCenter";
    private static TXUGCPublishOptCenter ourInstance;
    private TVCDnsCache dnsCache = null;
    private boolean isInited = false;
    private String signature = "";
    private CosRegionInfo bestCosInfo = new CosRegionInfo();
    private long minCosRespTime;
    private UGCClient ugcClient;
    private ConcurrentHashMap<String, Boolean> publishingList;

    private final Handler mDetectHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == WHAT_UPDATE_BEST_COS) {
                Bundle data = msg.getData();

                String domain = data.getString(KEY_COS_DOMAIN);
                String region = data.getString(KEY_COS_REGION);
                long costTime = data.getLong(KEY_REQUEST_TIME);
                boolean isQuic = data.getBoolean(KEY_IS_QUIC);

                compareBestCos(domain, region, costTime, isQuic);
            }
            return true;
        }
    });

    public static TXUGCPublishOptCenter getInstance() {
        if (ourInstance == null) {
            synchronized (TXUGCPublishOptCenter.class) {
                if (ourInstance == null) {
                    ourInstance = new TXUGCPublishOptCenter();
                }
            }
        }
        return ourInstance;
    }


    private TXUGCPublishOptCenter() {
        publishingList = new ConcurrentHashMap<String, Boolean>();
    }

    public interface IPrepareUploadCallback {
        void onFinish();
    }

    public void prepareUpload(final Context context, String signature, IPrepareUploadCallback prepareUploadCallback) {
        this.signature = signature;
        boolean ret = false;
        if (!isInited) {
            dnsCache = new TVCDnsCache();
            ret = reFresh(context, prepareUploadCallback);
        }
        if (ret) {
            isInited = true;
        } else {
            if (prepareUploadCallback != null) {
                prepareUploadCallback.onFinish();
            }
        }
    }

    /**
     * 耗时操作，同步执行完
     */
    private void prepareUploadFinal(final Context context) {
        ugcClient = UGCClient.getInstance(signature, 10);
        final long reqTime = System.currentTimeMillis();
        try {
            Response response = ugcClient.prepareUploadUGC();
            Log.i(TAG, "prepareUploadUGC resp:" + response.message());
            if (response.isSuccessful()) {
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                        TVCConstants.NO_ERROR, "", reqTime, System.currentTimeMillis() - reqTime);
                // 解析预上传结果，并且发起最优园区探测
                parsePrepareUploadResp(context, response.body().string());
            } else {
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                        TVCConstants.ERROR,
                        "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime);
            }
        } catch (IOException e) {
            Log.i(TAG, "prepareUploadUGC failed:" + e.getMessage());
            // 获取预上传失败
            reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                    TVCConstants.ERROR,
                    e.toString(), reqTime, System.currentTimeMillis() - reqTime);
        }
    }

    /**
     * 上报优化处理结果
     */
    private void reportPublishOptResult(Context context, int reqType, int errCode, String errMsg, long reqTime,
                                        long reqTimeCost) {
        UGCReport.ReportInfo reportInfo = new UGCReport.ReportInfo();
        reportInfo.reqType = reqType;
        reportInfo.errCode = errCode;
        reportInfo.errMsg = errMsg;
        reportInfo.reqTime = reqTime;
        reportInfo.reqTimeCost = reqTimeCost;

        UGCReport.getInstance(context).addReportInfo(reportInfo);
    }

    public boolean reFresh(final Context context, final IPrepareUploadCallback prepareUploadCallback) {

        synchronized (bestCosInfo) {
            minCosRespTime = 0;
            bestCosInfo.region = "";
            bestCosInfo.domain = "";
        }

        if (dnsCache == null || TextUtils.isEmpty(signature)) {
            return false;
        }

        dnsCache.clear();

        final long reqTime = System.currentTimeMillis();
        boolean ret = dnsCache.freshDomain(TVCConstants.VOD_SERVER_HOST, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 获取vod 域名失败
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT, TVCConstants.ERROR,
                        e.toString(), reqTime, System.currentTimeMillis() - reqTime);
                prepareUploadFinal(context);
                if (prepareUploadCallback != null) {
                    prepareUploadCallback.onFinish();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 获取vod 域名失败
                    reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT,
                            TVCConstants.ERROR,
                            "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime);
                } else {
                    reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT,
                            TVCConstants.NO_ERROR,
                            "", reqTime, System.currentTimeMillis() - reqTime);
                }

                prepareUploadFinal(context);
                if (prepareUploadCallback != null) {
                    prepareUploadCallback.onFinish();
                }
            }
        });
        return ret;
    }

    private synchronized void parsePrepareUploadResp(Context context, String rspString) {
        Log.i(TAG, "parsePrepareUploadRsp->response is " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            return;
        }

        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            String message = "";
            try {
                message = new String(jsonRsp.optString("message", "").getBytes("UTF-8"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (0 != code) {
                return;
            }

            JSONObject dataRsp = jsonRsp.getJSONObject("data");
            if (dataRsp == null) {
                return;
            }

            String appId = dataRsp.optString("appId", "");
            JSONArray cosArray = dataRsp.optJSONArray("cosRegionList");

            if (cosArray == null || cosArray.length() <= 0) {
                Log.e(TAG, "parsePrepareUploadRsp , cosRegionList is null!");
                return;
            }

            CountDownLatch latch = new CountDownLatch(cosArray.length() * 2);// 设置计数值
            int maxThreadCount = Math.min(cosArray.length() * 2, 8);
            ExecutorService exec = Executors.newFixedThreadPool(maxThreadCount); // 创建线程池

            final long reqTime = System.currentTimeMillis();

            //探测quic
            detectQuicNet(cosArray, context, latch, exec);

            //最优园区探测
            detectBestCos(cosArray, latch, exec);

            try {
                latch.await();// 等待所有线程完成操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            exec.shutdown();

            reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_DETECT_DOMAIN_RESULT,
                    TextUtils.isEmpty(bestCosInfo.region) ? TVCConstants.ERROR : TVCConstants.NO_ERROR,
                    TextUtils.isEmpty(bestCosInfo.region) ? "" : bestCosInfo.domain + "|" + bestCosInfo.region, reqTime,
                    System.currentTimeMillis() - reqTime);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void detectQuicNet(JSONArray cosArray, final Context context,
                               final CountDownLatch latch, ExecutorService exec) throws JSONException {
        // quic参数初始化
        QuicNative.init();
        QuicNative.setDebugLog(BuildConfig.DEBUG);

        QuicConfig quicConfig = new QuicConfig();
        quicConfig.setCustomProtocol(false);
        quicConfig.setRaceType(QuicConfig.RACE_TYPE_ONLY_QUIC);
        quicConfig.setTotalTimeoutSec(2);
        QuicProxy.setTnetConfig(quicConfig);

        for (int i = 0; i < cosArray.length(); ++i) {
            final JSONObject cosInfoJsonObject;
            cosInfoJsonObject = cosArray.getJSONObject(i);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    final String domain = cosInfoJsonObject.optString("domain", "");
                    final String region = cosInfoJsonObject.optString("region", "");
                    String ips = cosInfoJsonObject.optString("ip", "");
                    // 1、获取cos 的iplist
                    getCosDNS(domain, ips);
                    if (!TextUtils.isEmpty(domain)) {
                        QuicClient quicClient = new QuicClient(context);
                        quicClient.detectQuic(domain, new QuicClient.QuicDetectListener() {
                            @Override
                            public void onQuicDetectDone(boolean isQuic, long requestTime, int errorCode) {
                                Log.i(TAG, "detectQuicNet domain = " + domain
                                        + ", region = " + region
                                        + ", timeCos = " + requestTime
                                        + ", errorCode = " + errorCode
                                        + ", isQuic = " + isQuic);
                                if (isQuic) {
                                    sendToCompareCos(domain, region, requestTime, true);
                                }
                                latch.countDown();
                            }
                        });
                    }
                }
            });
        }
    }

    private void detectBestCos(JSONArray cosArray, final CountDownLatch latch,
                               ExecutorService exec) throws JSONException {
        for (int i = 0; i < cosArray.length(); ++i) {
            final JSONObject cosInfoJsonObject = cosArray.getJSONObject(i);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    String region = cosInfoJsonObject.optString("region", "");
                    String domain = cosInfoJsonObject.optString("domain", "");
                    int isAcc = cosInfoJsonObject.optInt("isAcc", 0);
                    String ips = cosInfoJsonObject.optString("ip", "");
                    if (!TextUtils.isEmpty(region) && !TextUtils.isEmpty(domain)) {
                        // 1、获取cos 的iplist
                        getCosDNS(domain, ips);
                        // 2、探测最优园区
                        detectBestCosIP(domain, region);
                    }
                    latch.countDown();
                }
            });
        }
    }

    private void detectBestCosIP(String domain, String region) {
        final long beginTS = System.currentTimeMillis();
        try {
            Response response = ugcClient.detectDomain(domain);
            if (response != null) {
                // 服务器有返回认为是检测成功，不需要判断isSuccessful，因为私有域会返回403，检测也是正常的
                long endTS = System.currentTimeMillis();
                long timeCost = endTS - beginTS;

                Log.i(TAG, "detectBestCosIP domain = " + domain
                        + ", region = " + region
                        + ", timeCos = " + timeCost
                        + ", response.code = " + response.code());

                sendToCompareCos(domain, region, timeCost, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToCompareCos(String domain, String region, long timeCost, boolean isQuic) {
        Bundle data = new Bundle();
        data.putString(KEY_COS_DOMAIN, domain);
        data.putString(KEY_COS_REGION, region);
        data.putLong(KEY_REQUEST_TIME, timeCost);
        data.putBoolean(KEY_IS_QUIC, isQuic);

        Message message = new Message();
        message.what = WHAT_UPDATE_BEST_COS;
        message.setData(data);

        mDetectHandler.sendMessage(message);
    }

    private void compareBestCos(String domain, String region, long timeCost, boolean isQuic) {
        synchronized (CosRegionInfo.class) {
            if (minCosRespTime == 0 || timeCost < minCosRespTime) {
                minCosRespTime = timeCost;
                bestCosInfo.region = region;
                bestCosInfo.domain = domain;
                bestCosInfo.isQuic = isQuic;

                Log.i(TAG, "detectBestCosIP bestCosDomain = " + bestCosInfo.domain
                        + ", bestCosRegion = " + bestCosInfo.region
                        + ", timeCos = " + minCosRespTime
                        + ", isQuic = " + isQuic);
            }
        }
    }

    private void getCosDNS(final String domain, String ips) {
        //返回的ip列表为空，首先执行http dns
        if (TextUtils.isEmpty(ips)) {
            // 异步转同步
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            boolean ret = dnsCache.freshDomain(domain, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    countDownLatch.countDown();
                }
            });
            if (ret) {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else { //返回的ip列表不为，首先把ip设置到HttpDnsCache中
            ArrayList<String> ipLists = new ArrayList<String>();
            if (ips.contains(";")) {
                String[] ipArray = ips.split(",");
                Collections.addAll(ipLists, ipArray);
            } else {
                ipLists.add(ips);
            }
            dnsCache.addDomainDNS(domain, ipLists);
        }
    }

    public boolean useHttpDNS(String host) {
        return (dnsCache != null && dnsCache.useHttpDNS(host));
    }

    public List<String> query(String host) {
        if (dnsCache != null) {
            List<String> ipList = dnsCache.query(host);
            Log.d(TAG, "query domain" + host + ",result:" + ipList);
            return ipList;
        } else {
            Log.d(TAG, "query domain" + host + ",result null");
            return null;
        }
    }

    public String getCosRegion() {
        synchronized (bestCosInfo) {
            return bestCosInfo.region;
        }
    }

    /**
     * 判断当前上传region与探测出来的最优region是否一致，如果一致，返回探测出来的quic开关。
     * 否则证明没有使用最优探测结果，使用的默认cos region，没有开启quic
     */
    public boolean isNeedEnableQuic(String currentRegion) {
        synchronized (bestCosInfo) {
            if (TextUtils.isEmpty(currentRegion) || TextUtils.equals(currentRegion, bestCosInfo.region)) {
                return bestCosInfo.isQuic;
            }
            return false;
        }
    }

    public void disableQuicIfNeed() {
        synchronized (bestCosInfo) {
            if (null != bestCosInfo && bestCosInfo.isQuic) {
                bestCosInfo.isQuic = false;
            }
        }
    }

    public void addPublishing(String videoPath) {
        publishingList.put(videoPath, true);
    }

    public void delPublishing(String videoPath) {
        publishingList.remove(videoPath);
    }

    public boolean isPublishing(String videoPath) {
        if (publishingList.containsKey(videoPath)) {
            return publishingList.get(videoPath);
        }
        return false;
    }
}

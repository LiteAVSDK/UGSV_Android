package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    private class CosRegionInfo {
        private String region = "";
        private String domain = "";
    }

    private static final String TAG = "TVC-OptCenter";

    private static TXUGCPublishOptCenter ourInstance;

    private TVCDnsCache dnsCache = null;

    private boolean       isInited    = false;
    private String        signature   = "";
    private CosRegionInfo bestCosInfo = new CosRegionInfo();
    private long          minCosRespTime;
    private UGCClient     ugcClient;

    private ConcurrentHashMap<String, Boolean> publishingList;

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
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT, TVCConstants.ERROR,
                        "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime);
            }
        } catch (IOException e) {
            Log.i(TAG, "prepareUploadUGC failed:" + e.getMessage());
            // 获取预上传失败
            reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT, TVCConstants.ERROR,
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
                    reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT, TVCConstants.ERROR,
                            "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime);
                } else {
                    reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT, TVCConstants.NO_ERROR,
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

            final CountDownLatch latch = new CountDownLatch(cosArray.length());// 设置计数值,
            int maxThreadCount = Math.min(cosArray.length(), 4);
            ExecutorService exec = Executors.newFixedThreadPool(maxThreadCount); // 创建线程池

            long reqTime = System.currentTimeMillis();
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

    private void detectBestCosIP(String domain, String region) {
        final long beginTS = System.currentTimeMillis();
        try {
            Response response = ugcClient.detectDomain(domain);
            if (response != null) {
                // 服务器有返回认为是检测成功，不需要判断isSuccessful，因为私有域会返回403，检测也是正常的
                long endTS = System.currentTimeMillis();
                long timeCost = endTS - beginTS;

                Log.i(TAG, "detectBestCosIP domain = " + domain + ", region = " + region + ", timeCos = " + timeCost +
                        ", response.code = " + response.code());

                if (minCosRespTime == 0 || timeCost < minCosRespTime) {
                    minCosRespTime = timeCost;
                    bestCosInfo.region = region;
                    bestCosInfo.domain = domain;

                    Log.i(TAG, "detectBestCosIP bestCosDomain = " + bestCosInfo.domain
                            + ", bestCosRegion = " + bestCosInfo.region
                            + ", timeCos = " + minCosRespTime);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                for (int i = 0; i < ipArray.length; ++i) {
                    ipLists.add(ipArray[i]);
                }
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
            return dnsCache.query(host);
        } else {
            return null;
        }
    }

    public String getCosRegion() {
        synchronized (bestCosInfo) {
            return bestCosInfo.region;
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

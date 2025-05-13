package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Short video upload optimization: Involves HTTPDNS and PrepareUploadUGC pre-detection of the best park
 * 短视频上传优化：涉及到httpdns、PrepareUploadUGC预先探测最优园区
 */
public class TXUGCPublishOptCenter {
    private static final long PRE_UPLOAD_TIME_OUT = 8 * 1000;
    private static final int WAIT_TIME_OUT = 101;

    private static class CosRegionInfo {
        private String region = "";
        private String domain = "";
        private boolean isQuic = false;
    }

    private static final String TAG = "TVC-OptCenter";
    private TVCDnsCache dnsCache = null;
    private boolean isInited = false;
    private String signature = "";
    private final CosRegionInfo bestCosInfo = new CosRegionInfo();
    private long minCosRespTime;
    private UGCClient ugcClient;
    private final ConcurrentHashMap<String, Boolean> publishingList = new ConcurrentHashMap<>();
    private final Handler mProtectHandler;

    private static final class OurInstanceHolder {
        static final TXUGCPublishOptCenter ourInstance = new TXUGCPublishOptCenter();
    }

    public static TXUGCPublishOptCenter getInstance() {
        return OurInstanceHolder.ourInstance;
    }

    public TXUGCPublishOptCenter() {
        // use main looper, can prevent Handler leak
        mProtectHandler = new Handler(Looper.getMainLooper());
    }


    public interface IPrepareUploadCallback {
        void onFinish();
    }

    public void prepareUpload(final Context context, String signature,
                              final IPrepareUploadCallback prepareUploadCallback) {
        this.signature = signature;
        boolean ret = false;
        if (!isInited) {
            ret = prepareUploadInner(context, prepareUploadCallback);
        }
        if (ret) {
            isInited = true;
        } else {
            TVCLog.i(TAG, "preUpload is already loading/init/failed, callback it ");
            if (prepareUploadCallback != null) {
                prepareUploadCallback.onFinish();
            }
        }
    }

    /**
     * Pre-upload initialization
     * 预上传初始化
     */
    private boolean prepareUploadInner(final Context context, final IPrepareUploadCallback prepareUploadCallback) {
        dnsCache = new TVCDnsCache();
        final long startTime = System.currentTimeMillis();
        final AtomicBoolean isCallback = new AtomicBoolean(false);
        final Runnable timeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if (isCallback.compareAndSet(false, true) && null != prepareUploadCallback) {
                    TVCLog.w(TAG, "prepareUpload timeOut, make a callback ahead of schedule");
                    TVCLog.i(TAG, "preloadCostTime " + (System.currentTimeMillis() - startTime));
                    prepareUploadCallback.onFinish();
                }
                isCallback.set(true);
            }
        };
        IPrepareUploadCallback wrapCallback = new IPrepareUploadCallback() {
            @Override
            public void onFinish() {
                mProtectHandler.removeCallbacks(timeOutRunnable);
                if (isCallback.compareAndSet(false, true) && null != prepareUploadCallback) {
                    TVCLog.i(TAG, "prepareUpload success, remove timeOut runnable");
                    TVCLog.i(TAG, "preloadCostTime " + (System.currentTimeMillis() - startTime));
                    prepareUploadCallback.onFinish();
                } else {
                    TVCLog.i(TAG, "prepareUpload was already called, because of timeout");
                }
            }
        };
        mProtectHandler.postDelayed(timeOutRunnable, PRE_UPLOAD_TIME_OUT);
        return reFresh(context, wrapCallback);
    }

    /**
     * Time-consuming operation, executed synchronously
     * 耗时操作，同步执行完
     */
    private void prepareUploadFinal(final Context context) {
        ugcClient = UGCClient.getInstance(signature, 10);
        final long reqTime = System.currentTimeMillis();
        try {
            Response response = ugcClient.prepareUploadUGC();
            TVCLog.i(TAG, "prepareUploadUGC resp:" + response.message());
            if (response.isSuccessful()) {
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                        TVCConstants.NO_ERROR, "", reqTime, System.currentTimeMillis() - reqTime);
                // Parse pre-upload results and initiate optimal park detection
                parsePrepareUploadResp(context, response.body().string());
            } else {
                reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                        TVCConstants.ERROR,
                        "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime);
            }
        } catch (IOException e) {
            TVCLog.i(TAG, "prepareUploadUGC failed:" + e.getMessage());
            reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT,
                    TVCConstants.ERROR,
                    e.toString(), reqTime, System.currentTimeMillis() - reqTime);
        }
    }

    /**
     * Report optimization processing results
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
            bestCosInfo.isQuic = false;
        }

        if (dnsCache == null || TextUtils.isEmpty(signature)) {
            mProtectHandler.removeCallbacksAndMessages(null);
            return false;
        }

        dnsCache.clear();

        final long reqTime = System.currentTimeMillis();
        boolean ret = dnsCache.freshDomain(TVCConstants.VOD_SERVER_HOST, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Failed to get VOD domain name
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
                    // Failed to get VOD domain name
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
        if (!ret) {
            mProtectHandler.removeCallbacksAndMessages(null);
        }
        return ret;
    }

    private synchronized void parsePrepareUploadResp(Context context, String rspString) {
        TVCLog.i(TAG, "parsePrepareUploadRsp->response is " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            return;
        }

        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            String message = new String(jsonRsp.optString("message", "").
                    getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            if (0 != code) {
                return;
            }

            JSONObject dataRsp = jsonRsp.getJSONObject("data");

            String appId = dataRsp.optString("appId", "");
            JSONArray cosArray = dataRsp.optJSONArray("cosRegionList");

            if (cosArray == null || cosArray.length() <= 0) {
                TVCLog.e(TAG, "parsePrepareUploadRsp , cosRegionList is null!");
                return;
            }
            CountDownLatch getCosIpLatch = new CountDownLatch(cosArray.length());
            int cosIpFetchMaxThreadCount = Math.min(cosArray.length(), 8);
            ExecutorService getCosIpExec = Executors.newFixedThreadPool(cosIpFetchMaxThreadCount);
            fetchCosIp(cosArray, getCosIpLatch, getCosIpExec);
            try {
                getCosIpLatch.await(); // Wait for all threads to complete operations
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            CountDownLatch latch = new CountDownLatch(cosArray.length() * 2);
            int maxThreadCount = Math.min(cosArray.length() * 2, 8);
            ExecutorService exec = Executors.newFixedThreadPool(maxThreadCount);
            final long reqTime = System.currentTimeMillis();
            // QUIC detection
            detectQuicNet(cosArray, context, latch, exec);
            // HTTP detection
            detectBestCos(cosArray, latch, exec);

            try {
                latch.await(); // Wait for all threads to complete operations
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            exec.shutdown();

            TVCLog.e(TAG, "preUploadResult:" + bestCosInfo.region + ", isQuic:" + bestCosInfo.isQuic
                    + ",costTime:" + minCosRespTime);

            reportPublishOptResult(context, TVCConstants.UPLOAD_EVENT_ID_DETECT_DOMAIN_RESULT,
                    TextUtils.isEmpty(bestCosInfo.region) ? TVCConstants.ERROR : TVCConstants.NO_ERROR,
                    TextUtils.isEmpty(bestCosInfo.region) ? "" : bestCosInfo.domain + "|" + bestCosInfo.region, reqTime,
                    System.currentTimeMillis() - reqTime);
        } catch (JSONException e) {
            TVCLog.e(TAG, e.toString());
        }
    }

    private void fetchCosIp(final JSONArray cosArray, final CountDownLatch latch, ExecutorService exec) throws JSONException {
        for (int i = 0; i < cosArray.length(); ++i) {
            final JSONObject cosInfoJsonObject = cosArray.getJSONObject(i);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    final String ips = cosInfoJsonObject.optString("ip", "");
                    final String domain = cosInfoJsonObject.optString("domain", "");
                    getCosDNS(domain, ips);
                    latch.countDown();
                }
            });
        }
    }

    private void detectQuicNet(JSONArray cosArray, final Context context,
                               final CountDownLatch latch, ExecutorService exec) throws JSONException {

        for (int i = 0; i < cosArray.length(); ++i) {
            final JSONObject cosInfoJsonObject;
            cosInfoJsonObject = cosArray.getJSONObject(i);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    final String domain = cosInfoJsonObject.optString("domain", "");
                    final String region = cosInfoJsonObject.optString("region", "");
                    if (!TextUtils.isEmpty(domain)) {
                        QuicClient quicClient = new QuicClient(context);
                        quicClient.detectQuic(domain, new QuicClient.QuicDetectListener() {
                            @Override
                            public void onQuicDetectDone(boolean isQuic, long requestTime, int errorCode) {
                                TVCLog.i(TAG, "detectQuicNet domain = " + domain
                                        + ", region = " + region
                                        + ", timeCos = " + requestTime
                                        + ", errorCode = " + errorCode
                                        + ", isQuic = " + isQuic);
                                if (isQuic) {
                                    compareBestCos(domain, region, requestTime, true);
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
                    if (!TextUtils.isEmpty(region) && !TextUtils.isEmpty(domain)) {
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
                // If the server returns a response, it is considered a successful detection,
                // no need to judge isSuccessful, because the private domain will return 403,
                // and the detection is normal
                long endTS = System.currentTimeMillis();
                long timeCost = endTS - beginTS;

                TVCLog.i(TAG, "detectBestCosIP domain = " + domain
                        + ", region = " + region
                        + ", timeCos = " + timeCost
                        + ", response.code = " + response.code());
                compareBestCos(domain, region, timeCost, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compareBestCos(String domain, String region, long timeCost, boolean isQuic) {
        synchronized (CosRegionInfo.class) {
            if (canUpdateBestCos(timeCost, isQuic)) {
                minCosRespTime = timeCost;
                bestCosInfo.region = region;
                bestCosInfo.domain = domain;
                bestCosInfo.isQuic = isQuic;
                TVCLog.i(TAG, "compareBestCosIP bestCosDomain = " + bestCosInfo.domain
                        + ", bestCosRegion = " + bestCosInfo.region
                        + ", timeCos = " + minCosRespTime
                        + ", isQuic = " + isQuic);
            }
        }
    }

    private boolean canUpdateBestCos(long timeCost, boolean isQuic) {
        // quic first
        boolean result;
        if (minCosRespTime == 0) {
            result = true;
        } else if (bestCosInfo.isQuic) {
            result = isQuic && timeCost < minCosRespTime;
        } else {
            if (isQuic) {
                result = true;
            } else {
                result = timeCost < minCosRespTime;
            }
        }
        return result;
    }

    private void getCosDNS(final String domain, String ips) {
        // If the returned IP list is empty, execute HTTP DNS first
        if (TextUtils.isEmpty(ips)) {
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
        } else {
            // If the returned IP list is not empty, set the IP in HttpDnsCache first
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
            TVCLog.i(TAG, "query domain" + host + ",result:" + ipList);
            return ipList;
        } else {
            TVCLog.i(TAG, "query domain" + host + ",result null");
            return null;
        }
    }

    public String getCosRegion() {
        synchronized (bestCosInfo) {
            return bestCosInfo.region;
        }
    }

    /**
     * Determine whether the current upload region is consistent with the detected optimal region.
     * If consistent, return the detected QUIC switch. Otherwise, it means that the optimal detection result
     * is not used, and the default COS region is used without enabling QUIC.
     * <p>
     * 判断当前上传region与探测出来的最优region是否一致，如果一致，返回探测出来的quic开关。
     * 否则证明没有使用最优探测结果，使用的默认cos region，没有开启quic
     */
    public boolean isNeedEnableQuic(String currentRegion) {
        synchronized (bestCosInfo) {
            if (TextUtils.isEmpty(currentRegion) || TextUtils.equals(currentRegion, bestCosInfo.region)) {
                List<String> ipList = query(bestCosInfo.domain);
                if (null != ipList && !ipList.isEmpty()) {
                    return bestCosInfo.isQuic;
                }
            }
            return false;
        }
    }

    public void disableQuicIfNeed() {
        synchronized (bestCosInfo) {
            if (bestCosInfo.isQuic) {
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

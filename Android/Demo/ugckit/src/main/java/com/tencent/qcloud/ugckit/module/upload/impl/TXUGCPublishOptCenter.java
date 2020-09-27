package com.tencent.qcloud.ugckit.module.upload.impl;

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

    private boolean isInited = false;
    private String signature = "";
    private long minCosRespTime = 0;
    private CosRegionInfo bestCosInfo = new CosRegionInfo();
    private UGCClient ugcClient;

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

    public void prepareUpload(String signature) {
        this.signature = signature;
        if (!isInited) {
            dnsCache = new TVCDnsCache();
            reFresh();
            isInited = true;
        }
    }

    private void prepareUploadFinal() {
        ugcClient = UGCClient.getInstance(signature, 10);
        ugcClient.PrepareUploadUGC(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "prepareUpload failed:" + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "prepareUpload resp:" + response.message());
                if (response.isSuccessful()) {
                    parsePrepareUploadRsp(response.body().string());
                }
            }
        });
    }

    public void reFresh() {
        bestCosInfo.region = "";
        bestCosInfo.domain = "";

        if (dnsCache == null) {
            return;
        }

        if (TextUtils.isEmpty(signature)) {
            return;
        }

        dnsCache.clear();
        dnsCache.freshDomain(TVCConstants.VOD_SERVER_HOST, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                prepareUploadFinal();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                prepareUploadFinal();
            }
        });
    }

    private void parsePrepareUploadRsp(String rspString) {
        Log.i(TAG, "parsePrepareUploadRsp->response is " + rspString);
        if (TextUtils.isEmpty(rspString))
            return;

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

            String appId = dataRsp.optString("appId", "");
            JSONArray cosArray = dataRsp.optJSONArray("cosRegionList");

            if (cosArray == null) {
                Log.e(TAG, "parsePrepareUploadRsp , cosRegionList is null!");
                return;
            }

            for (int i = 0; i < cosArray.length(); ++i) {
                JSONObject cosInfoJsonObject = cosArray.getJSONObject(i);
                String region = cosInfoJsonObject.optString("region", "");
                String domain = cosInfoJsonObject.optString("domain", "");
                int isAcc = cosInfoJsonObject.optInt("isAcc", 0);
                String ips = cosInfoJsonObject.optString("ip", "");

                if (TextUtils.isEmpty(region) || TextUtils.isEmpty(domain))
                    continue;

                getCosDNS(region, domain, isAcc, ips);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            return;
        }
    }

    private void getCosDNS(final String region, final String domain, int isAcc, String ips) {
        //返回的ip列表为空，首先执行httpdns
        if (TextUtils.isEmpty(ips)) {
            dnsCache.freshDomain(domain, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    detectBsetCosIP(region, domain);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    detectBsetCosIP(region, domain);
                }
            });
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
            detectBsetCosIP(region, domain);
        }
    }

    private void detectBsetCosIP(final String region, final String domain) {
        synchronized (ugcClient) {
            final long beginTS = System.currentTimeMillis();
            ugcClient.detectDomain(domain, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i(TAG, "detect cos domain " + domain + " failed , " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        long endTS = System.currentTimeMillis();
                        long timeCost = endTS - beginTS;
                        if (minCosRespTime == 0 || timeCost < minCosRespTime) {
                            minCosRespTime = timeCost;
                            bestCosInfo.region = region;
                            bestCosInfo.domain = domain;
                        }
                    } else {
                        Log.i(TAG, "detect cos domain " + domain + " failed , httpcode" + response.code());
                    }
                }
            });
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
        return bestCosInfo.region;
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

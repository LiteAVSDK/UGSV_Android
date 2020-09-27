package com.tencent.qcloud.ugckit.module.upload.impl;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * httpdns缓存，用于解决localdns慢、不准确的问题
 */

public class TVCDnsCache {
    private static final String TAG = "TVC-TVCDnsCache";

    private OkHttpClient okHttpClient;

    private static String HTTPDNS_SERVER = "http://119.29.29.29/d?dn=";      //httpdns服务器请求ip
    private ConcurrentHashMap<String, List<String>> cacheMap;
    private ConcurrentHashMap<String, List<String>> fixCacheMap;    //固定的dns缓存，从后台获取，认为这个是可信的

    public TVCDnsCache() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)    // 设置超时时间
                .readTimeout(5, TimeUnit.SECONDS)       // 设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)      // 设置写入超时时间
                .build();
        cacheMap = new ConcurrentHashMap<String, List<String>>();
        fixCacheMap = new ConcurrentHashMap<String, List<String>>();
    }

    // 对指定域名发起httpdns请求
    public void freshDomain(final String domain, final Callback callback) {
        if (useProxy())
            return;
        String reqUrl = HTTPDNS_SERVER + domain;
        Log.i(TAG, "freshDNS->request url:" + reqUrl);
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null)
                    callback.onFailure(call, e);
                Log.w(TAG, "freshDNS failed :" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String ips = response.body().string();
                    Log.i(TAG, "freshDNS succ :" + ips);

                    if (ips != null && ips.length() != 0) {
                        ArrayList<String> ipLists = new ArrayList<String>();
                        if (ips.contains(";")) {
                            String[] ipArray = ips.split(";");
                            for (int i = 0; i < ipArray.length; ++i) {
                                ipLists.add(ipArray[i]);
//                                if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST)) {
//                                    ipLists.add("183.60.81.104");
//                                } else {
//                                    ipLists.add(ipArray[i]);
//                                }
                            }
                        } else {
                            ipLists.add(ips);
//                            if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST)) {
//                                ipLists.add("183.60.81.104");
//                            } else {
//                                ipLists.add(ips);
//                            }
                        }
                        cacheMap.put(domain, ipLists);
                        if (callback != null) {
                            callback.onResponse(call, response);
                            return;
                        }
                    }
                }

                if (callback != null)
                    callback.onFailure(call, new IOException("freshDNS failed"));
            }
        });
    }


    // 添加指定域名的ip列表，ip列表是后台返回的
    public void addDomainDNS(String domain, ArrayList<String> ipLists) {
        if (useProxy())
            return;

        if (ipLists == null || ipLists.size() == 0)
            return;

        fixCacheMap.put(domain, ipLists);
    }

    public List<String> query(String hostname) {
        List<String> ipList = cacheMap.get(hostname);
        if (ipList != null && ipList.size() > 0) {
            return ipList;
        }

        ipList = fixCacheMap.get(hostname);
        if (ipList != null && ipList.size() > 0) {
            return ipList;
        }
        return null;
    }

    public boolean useHttpDNS(String hostname) {
        if (cacheMap.containsKey(hostname) && cacheMap.get(hostname).size() > 0) {
            return true;
        } else if (fixCacheMap.containsKey(hostname) && fixCacheMap.get(hostname).size() > 0) {
            return true;
        }

        return false;
    }

    public static boolean useProxy(){
        String host = System.getProperty("http.proxyHost");
        String port= System.getProperty("http.proxyPort");
        if (host != null && port != null) {
            // 使用了本地代理模式
            Log.i(TAG, "use proxy " + host + ":" + port + ", will not use httpdns");
            return true;
        }
        return false;
    }

    public void clear() {
        cacheMap.clear();
        fixCacheMap.clear();
    }
}

package com.tencent.qcloud.ugckit.module.upload.impl;

import android.text.TextUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * httpdns缓存，用于解决localdns慢、不准确的问题
 */

public class TVCDnsCache {
    private final Pattern patternIpV4 = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])"
            + "(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    private final Pattern patternIpV6 = Pattern.compile("^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]"
            + "{1,4}:){1,7}:)|(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4})"
            + "{1,2})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){1,3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4})"
            + "{1,4})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){1,5})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,6})"
            + "|(:(:[0-9A-Fa-f]{1,4}){1,7})|(([0-9A-Fa-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|"
            + "[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d"
            + "|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4})"
            + "{0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|"
            + "(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|"
            + "[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}:(\\d|[1-9]\\d"
            + "|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([0-9A-Fa-f]{1,4}:"
            + "(:[0-9A-Fa-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d"
            + "|25[0-5])){3})|(:(:[0-9A-Fa-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d"
            + "|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$");
    private static final String TAG = "TVC-TVCDnsCache";

    private OkHttpClient okHttpClient;
    private static String HTTPDNS_SERVER = "https://119.29.29.99/d?dn=";      //httpdns服务器请求ip
    private ConcurrentHashMap<String, List<String>> cacheMap;
    private ConcurrentHashMap<String, List<String>> fixCacheMap;    //固定的dns缓存，从后台获取，认为这个是可信的
    private static String HTTPDNS_TOKEN = "800654663"; //该token用于https加密标志，可明文保存

    public TVCDnsCache() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(TVCConstants.PRE_UPLOAD_ANA_DNS_TIME_OUT, TimeUnit.MILLISECONDS)    // 设置超时时间
                .readTimeout(TVCConstants.PRE_UPLOAD_ANA_DNS_TIME_OUT, TimeUnit.MILLISECONDS)       // 设置读取超时时间
                .writeTimeout(TVCConstants.PRE_UPLOAD_ANA_DNS_TIME_OUT, TimeUnit.MILLISECONDS)      // 设置写入超时时间
                .build();
        cacheMap = new ConcurrentHashMap<String, List<String>>();
        fixCacheMap = new ConcurrentHashMap<String, List<String>>();
    }

    // 对指定域名发起httpdns请求
    public boolean freshDomain(final String domain, final Callback callback) {
        if (useProxy()) return false;
        String reqUrl = HTTPDNS_SERVER + domain + "&token=" + HTTPDNS_TOKEN;
        TVCLog.i(TAG, "freshDNS->request url:" + reqUrl);
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(call, e);
                }
                TVCLog.w(TAG, "freshDNS failed :" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String ips = response.body().string();
                    TVCLog.i(TAG, "freshDNS succ :" + ips);
                    if (ips != null && ips.length() != 0) {
                        ArrayList<String> ipLists = new ArrayList<>();
                        if (ips.contains(";")) {
                            String[] ipArray = ips.split(";");
                            for (String ipStr : ipArray) {
                                if (checkIpValid(ipStr)) {
                                    TVCLog.i(TAG, "freshDNS add ip :" + ipStr);
                                    ipLists.add(ipStr);
                                }
                            }
                        } else if (checkIpValid(ips)) {
                            TVCLog.i(TAG, "freshDNS add ip :" + ips);
                            ipLists.add(ips);
                        }
                        TVCLog.i(TAG, domain + " add ips success, " + ipLists);
                        cacheMap.put(domain, ipLists);
                        if (callback != null) {
                            callback.onResponse(call, response);
                            return;
                        }
                    }
                }

                if (callback != null) {
                    callback.onFailure(call, new IOException("freshDNS failed"));
                }
            }
        });
        return true;
    }

    private boolean checkIpValid(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        return patternIpV4.matcher(content).find() || patternIpV6.matcher(content).find();
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

        ipList = getIpBySysDns(hostname);
        if (ipList.size() > 0) {
            cacheMap.put(hostname, ipList);
            return ipList;
        }
        return null;
    }

    private List<String> getIpBySysDns(String host) {
        List<String> ipList = new ArrayList<>();
        try {
            List<InetAddress> inetAddressList = Dns.SYSTEM.lookup(host);
            for (InetAddress address : inetAddressList) {
                if (!TextUtils.isEmpty(address.getHostAddress())) {
                    ipList.add(address.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            TVCLog.e(TAG, "getIpBySysDns failed:" + e);
        }
        return ipList;
    }

    public boolean useHttpDNS(String hostname) {
        if (cacheMap.containsKey(hostname) && cacheMap.get(hostname).size() > 0) {
            return true;
        } else if (fixCacheMap.containsKey(hostname) && fixCacheMap.get(hostname).size() > 0) {
            return true;
        }

        return false;
    }

    public static boolean useProxy() {
        String host = System.getProperty("http.proxyHost");
        String port = System.getProperty("http.proxyPort");
        if (host != null && port != null) {
            // 使用了本地代理模式
            TVCLog.i(TAG, "use proxy " + host + ":" + port + ", will not use httpdns");
            return true;
        }
        return false;
    }

    public void clear() {
        cacheMap.clear();
        fixCacheMap.clear();
    }
}

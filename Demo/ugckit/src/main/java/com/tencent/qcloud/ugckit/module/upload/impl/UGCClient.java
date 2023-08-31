package com.tencent.qcloud.ugckit.module.upload.impl;

import android.text.TextUtils;

import com.tencent.qcloud.ugckit.module.upload.impl.compute.TXOkHTTPEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * UGC Client
 */
public class UGCClient {
    private static final String                TAG      = "TVC-UGCClient";
    private              String                signature;
    private              OkHttpClient          okHttpClient;
    private              OkHttpClient          mHeadOkHttpClient;
    private              OkHttpClient          mPreUploadOkHttpClient;
    private              TXOkHTTPEventListener mTXOkHTTPEventListener;
    private              String                serverIP = "";
    private static       UGCClient             ourInstance;

    public static UGCClient getInstance(String signature, int iTimeOut) {
        synchronized (UGCClient.class) {
            if (ourInstance == null) {
                ourInstance = new UGCClient(signature, iTimeOut);
            } else if (signature != null && !TextUtils.isEmpty(signature)) {
                ourInstance.updateSignature(signature);
            }
        }

        return ourInstance;
    }


    private UGCClient(String signature, int iTimeOut) {
        this.signature = signature;
        mTXOkHTTPEventListener = new TXOkHTTPEventListener();
        okHttpClient = new OkHttpClient().newBuilder()
                .dns(new HttpDNS())
                .connectTimeout(iTimeOut, TimeUnit.SECONDS)    // 设置超时时间
                .readTimeout(iTimeOut, TimeUnit.SECONDS)       // 设置读取超时时间
                .writeTimeout(iTimeOut, TimeUnit.SECONDS)      // 设置写入超时时间
                .addNetworkInterceptor(new LoggingInterceptor())
                .eventListener(mTXOkHTTPEventListener)
                .build();
        mHeadOkHttpClient = new OkHttpClient().newBuilder()
                .dns(new HttpDNS())
                .connectTimeout(TVCConstants.PRE_UPLOAD_HTTP_DETECT_COMMON_TIMEOUT, TimeUnit.MILLISECONDS)
                // 设置超时时间
                .readTimeout(TVCConstants.PRE_UPLOAD_HTTP_DETECT_COMMON_TIMEOUT, TimeUnit.MILLISECONDS)
                // 设置读取超时时间
                .writeTimeout(TVCConstants.PRE_UPLOAD_HTTP_DETECT_COMMON_TIMEOUT, TimeUnit.MILLISECONDS)
                // 设置写入超时时间
                .addNetworkInterceptor(new LoggingInterceptor())
                .eventListener(mTXOkHTTPEventListener)
                .build();
        mPreUploadOkHttpClient = new OkHttpClient().newBuilder()
                .dns(new HttpDNS())
                .connectTimeout(TVCConstants.PRE_UPLOAD_TIMEOUT, TimeUnit.MILLISECONDS)    // 设置超时时间
                .readTimeout(TVCConstants.PRE_UPLOAD_TIMEOUT, TimeUnit.MILLISECONDS)       // 设置读取超时时间
                .writeTimeout(TVCConstants.PRE_UPLOAD_TIMEOUT, TimeUnit.MILLISECONDS)      // 设置写入超时时间
                .addNetworkInterceptor(new LoggingInterceptor())
                .eventListener(mTXOkHTTPEventListener)
                .build();
    }


    /**
     * 预上传（UGC接口）
     */
    public Response prepareUploadUGC() throws IOException {
        String reqUrl = "https://" + TVCConstants.VOD_SERVER_HOST + "/v3/index.php?Action=PrepareUploadUGC";
        TVCLog.d(TAG, "PrepareUploadUGC->request url:" + reqUrl);

        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("clientVersion", TVCConstants.TVCVERSION);
            jsonObject.put("signature", signature);
            body = jsonObject.toString();
            TVCLog.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder().url(reqUrl).post(requestBody).build();
        return mPreUploadOkHttpClient.newCall(request).execute();
    }

    /**
     * 发送head请求探测
     */
    public Response detectDomain(String domain) throws IOException {
        String reqUrl = "http://" + domain;
        TVCLog.d(TAG, "detectDomain->request url:" + reqUrl);
        Request request = new Request.Builder().url(reqUrl).method("HEAD", null).build();
        return mHeadOkHttpClient.newCall(request).execute();
    }

    /**
     * 申请上传（UGC接口）
     *
     * @param info          文件信息
     * @param customKey     customKey
     * @param vodSessionKey vodSessionKey
     * @param callback      回调  @return
     */
    public void initUploadUGC(String domain, TVCUploadInfo info, String customKey, String vodSessionKey,
                              final Callback callback) {
        String reqUrl = "https://" + domain + "/v3/index.php?Action=ApplyUploadUGC";
        TVCLog.d(TAG, "initUploadUGC->request url:" + reqUrl);

        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("signature", signature);
            jsonObject.put("videoName", info.getFileName());
            jsonObject.put("videoType", info.getFileType());
            jsonObject.put("videoSize", info.getFileSize());

            // 判断是否需要上传封面
            if (info.isNeedCover()) {
                jsonObject.put("coverName", info.getCoverName());
                jsonObject.put("coverType", info.getCoverImgType());
                jsonObject.put("coverSize", info.getCoverFileSize());
            }
            jsonObject.put("clientReportId", customKey);
            jsonObject.put("clientVersion", TVCConstants.TVCVERSION);
            if (!TextUtils.isEmpty(vodSessionKey)) {
                jsonObject.put("vodSessionKey", vodSessionKey);
            }
            String region = TXUGCPublishOptCenter.getInstance().getCosRegion();
            if (!TextUtils.isEmpty(region)) {
                jsonObject.put("storageRegion", region);
            }
            body = jsonObject.toString();
            TVCLog.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder().url(reqUrl).post(requestBody).build();
        if (TVCDnsCache.useProxy()) {
            final String host = request.url().host();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress address = InetAddress.getByName(host);
                        serverIP = address.getHostAddress();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 上传结束(UGC接口)
     *
     * @param domain        视频上传的域名
     * @param vodSessionKey 视频上传的会话key
     * @param callback      回调
     */
    public void finishUploadUGC(String domain, String customKey, String vodSessionKey, final Callback callback) {
        String reqUrl = "https://" + domain + "/v3/index.php?Action=CommitUploadUGC";
        TVCLog.d(TAG, "finishUploadUGC->request url:" + reqUrl);
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("signature", signature);
            jsonObject.put("clientReportId", customKey);
            jsonObject.put("clientVersion", TVCConstants.TVCVERSION);
            jsonObject.put("vodSessionKey", vodSessionKey);
            body = jsonObject.toString();
            TVCLog.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder().url(reqUrl).post(requestBody).build();

        if (TVCDnsCache.useProxy()) {
            final String host = request.url().host();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress address = InetAddress.getByName(host);
                        serverIP = address.getHostAddress();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        okHttpClient.newCall(request).enqueue(callback);
    }

    public String getServerIP() {
        return serverIP;
    }

    public long getTcpConnTimeCost() {
        return mTXOkHTTPEventListener.getTCPConnectionTimeCost();
    }

    public long getRecvRespTimeCost() {
        return mTXOkHTTPEventListener.getRecvRspTimeCost();
    }

    public void updateSignature(String signature) {
        this.signature = signature;
    }

    private class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            TVCLog.d(TAG, "Sending request " + request.url() + " on " + chain.connection() + "\n" + request.headers());
            if (!TVCDnsCache.useProxy()) {
                serverIP = chain.connection().route().socketAddress().getAddress().getHostAddress();
            }

            Response response = chain.proceed(request);

            return response;
        }
    }
}

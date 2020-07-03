package com.tencent.qcloud.ugckit.module.upload.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.transfer.UploadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 视频上传客户端
 */
public class TVCClient {
    private final static String TAG = "TVC-Client";
    private Context context;
    private Handler mainHandler;
    private boolean busyFlag = false;

    private TVCUploadInfo uploadInfo;

    private UGCClient ugcClient;
    private TVCUploadListener tvcListener;

    private int cosAppId;   //点播上传用到的COS appid
    private int userAppId;  //客户自己的appid，数据上报需要
    private String cosBucket;
    private String uploadRegion = "";
    private String cosTmpSecretId = "";
    private String cosTmpSecretKey = "";
    private String cosToken = "";
    private long cosExpiredTime;

    private String cosVideoPath;
    private String videoFileId;
    private String cosCoverPath;

    private String domain;
    @Nullable
    private String vodSessionKey = null;

    private long reqTime = 0;            //各阶段开始请求时间
    private long initReqTime = 0;        //上传请求时间，用于拼接reqKey。串联请求
    private String customKey = "";       //用于数据上报

    private CosXmlService cosService;
    private UploadService cosUploadHelper;

    // 断点重传session本地缓存
    // 以文件路径作为key值得，存储的内容是<session, uploadId, fileLastModify, expiredTime>
    private static final String LOCALFILENAME = "TVCSession";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mShareEditor;

    @Nullable
    private String uploadId = null;
    private long fileLastModTime = 0;     //视频文件最后修改时间
    private boolean enableResume = true;

    /**
     * 初始化上传实例
     *
     * @param signature 签名
     * @param iTimeOut  超时时间
     */
    public TVCClient(@NonNull Context context, String customKey, String signature, String cosRegion, boolean enableResume, int iTimeOut) {
        this.context = context.getApplicationContext();
        ugcClient = new UGCClient(context, signature, iTimeOut);
        mainHandler = new Handler(context.getMainLooper());
        mSharedPreferences = context.getSharedPreferences(LOCALFILENAME, Activity.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();
        this.uploadRegion = cosRegion;
        this.enableResume = enableResume;
        this.customKey = customKey;
        clearLocalCache();
    }

    /**
     * 初始化上传实例
     *
     * @param ugcSignature 签名
     */
    public TVCClient(@NonNull Context context, String customKey, String ugcSignature, String cosRegion, boolean resumeUpload) {
        this(context, customKey, ugcSignature, cosRegion, resumeUpload, 8);
    }

    // 清理一下本地缓存，过期的删掉
    private void clearLocalCache() {
        if (mSharedPreferences != null) {
            try {
                Map<String, ?> allContent = mSharedPreferences.getAll();
                //注意遍历map的方法
                for(Map.Entry<String, ?>  entry : allContent.entrySet()){
                    JSONObject json = new JSONObject((String) entry.getValue());
                    long expiredTime = json.optLong("expiredTime", 0);
                    // 过期了清空key
                    if (expiredTime < System.currentTimeMillis() / 1000) {
                        mShareEditor.remove(entry.getKey());
                        mShareEditor.commit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 通知上层上传成功
    private void notifyUploadSuccess(final String fileId, final String playUrl, final String coverUrl) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onSucess(fileId, playUrl, coverUrl);
            }
        });
    }

    // 通知上层上传失败
    private void notifyUploadFailed(final int errCode, final String errMsg) {
        mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    tvcListener.onFailed(errCode, errMsg);
                }
            });
    }

    // 通知上层上传进度
    private void notifyUploadProgress(final long currentSize, final long totalSize) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onProgress(currentSize, totalSize);
            }
        });
    }

    private boolean isVideoFileExist(@NonNull String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("getFileSize", "getFileSize: " + e);
            return false;
        }
        return false;
    }

    /**
     * 上传视频文件
     *
     * @param info     视频文件信息
     * @param listener 上传回调
     * @return
     */
    public int uploadVideo(@NonNull TVCUploadInfo info, TVCUploadListener listener) {
        if (busyFlag) {     // 避免一个对象传输多个文件
            return TVCConstants.ERR_CLIENT_BUSY;
        }
        busyFlag = true;
        this.uploadInfo = info;
        this.tvcListener = listener;

        if (!isVideoFileExist(info.getFilePath())) { //视频文件不存在 直接返回
            tvcListener.onFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, "file could not find");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_FILE_NOEXIT, "file could not find", System.currentTimeMillis(), 0, 0, "", "");

            return -1;

        }

        String fileName = info.getFileName();
        Log.d(TAG, "fileName = " + fileName);
        if (fileName != null && fileName.getBytes().length > 40) { //视频文件名太长 直接返回
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name too long");
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME, "file name too long", System.currentTimeMillis(), 0, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (info.isContainSpecialCharacters(fileName)) {//视频文件名包含特殊字符 直接返回
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name contains special character / : * ? \" < >");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME, "file name contains special character / : * ? \" < >", System.currentTimeMillis(), 0, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (enableResume)
            getResumeData(info.getFilePath());
        getCosUploadInfo(info, vodSessionKey);
        return TVCConstants.NO_ERROR;
    }

    /**
     * 取消（中断）上传。中断之后恢复上传再用相同的参数调用uploadVideo即可。
     * @return 成功或者失败
     */
    public void cancleUpload() {
        if (cosUploadHelper != null) {
            cosUploadHelper.pause();
            busyFlag = false;
        }
    }

    private void getCosUploadInfo(TVCUploadInfo info, String vodSessionKey) {
        // 第一步 向UGC请求上传(获取COS认证信息)

        reqTime = System.currentTimeMillis();
        initReqTime = reqTime;
        ugcClient.initUploadUGC(info, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, @NonNull IOException e) {
                Log.e(TAG, "initUploadUGC->onFailure: " + e.toString());
                notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, e.toString());

                txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
            }

            @Override
            public void onResponse(Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, "HTTP Code:" + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

                    setResumeData(uploadInfo.getFilePath(), "", "");

                    Log.e(TAG, "initUploadUGC->http code: " + response.code());
                    throw new IOException("" + response);
                } else {
                    parseInitRsp(response.body().string());
                }
            }
        });
    }

    // 解析上传请求返回信息
    private void parseInitRsp(String rspString) {
        Log.i(TAG, "parseInitRsp: " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            Log.e(TAG, "parseInitRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, "init response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, "init response is empty", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

            setResumeData(uploadInfo.getFilePath(), "", "");

            return;
        }

        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            Log.i(TAG, "parseInitRsp: " + code);

            String message = "";
            try {
                message = new String(jsonRsp.optString("message", "").getBytes("UTF-8"),"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (0 != code) {
                notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, code + "|" + message);

                txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, code + "|" + message, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

                vodSessionKey = null;
                setResumeData(uploadInfo.getFilePath(), "", "");

                return;
            }

            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONObject videoObj = dataObj.getJSONObject("video");
            cosVideoPath = videoObj.getString("storagePath");

            // cos上传临时证书
            JSONObject tempCertificate = dataObj.getJSONObject("tempCertificate");
            cosTmpSecretId = tempCertificate.optString("secretId");
            cosTmpSecretKey = tempCertificate.optString("secretKey");
            cosToken = tempCertificate.optString("token");
            cosExpiredTime = tempCertificate.optLong("expiredTime");

            Log.d(TAG, "isNeedCover:" + uploadInfo.isNeedCover());
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataObj.getJSONObject("cover");
                cosCoverPath = coverObj.getString("storagePath");
            }
            cosAppId = dataObj.getInt("storageAppId");
            cosBucket = dataObj.getString("storageBucket");
            uploadRegion = dataObj.getString("storageRegionV5");
            domain = dataObj.getString("domain");
            vodSessionKey = dataObj.getString("vodSessionKey");
            userAppId = dataObj.getInt("appId");

            Log.d(TAG, "cosVideoPath=" + cosVideoPath);
            Log.d(TAG, "cosCoverPath=" + cosCoverPath);
            Log.d(TAG, "cosAppId=" + cosAppId);
            Log.d(TAG, "cosBucket=" + cosBucket);
            Log.d(TAG, "uploadRegion=" + uploadRegion);
            Log.d(TAG, "domain=" + domain);
            Log.d(TAG, "vodSessionKey=" + vodSessionKey);

            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .setAppidAndRegion(String.valueOf(cosAppId), uploadRegion)
                    .setDebuggable(true)
                    .builder();
            cosService = new CosXmlService(context, cosXmlServiceConfig,
                    new TVCDirectCredentialProvider(cosTmpSecretId, cosTmpSecretKey, cosToken, cosExpiredTime));

            // 第二步 通过COS上传视频
            uploadCosVideo();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_PARSE_FAILED, e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, e.toString());
            return;
        }

        txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, 0, "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
    }

    // 通过COS上传封面
    private void uploadCosCover() {

        reqTime = System.currentTimeMillis();

        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, cosCoverPath, uploadInfo.getCoverPath());
        putObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.d(TAG, "uploadCosCover->progress: " + progress + "/" + max);
                // 上传封面无进度
                //tvcListener.onProgress(currentSize, totalSize);
            }
        });

        putObjectRequest.setSign(600,null,null);
        cosService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, @NonNull CosXmlResult cosXmlResult) {
                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getCoverFileSize(), uploadInfo.getCoverImgType(), uploadInfo.getCoverName());
                startFinishUploadUGC(cosXmlResult);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, @Nullable CosXmlClientException qcloudException, @NonNull CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                }else {
                    stringBuilder.append(qcloudServiceException.toString());
                }

                notifyUploadFailed(TVCConstants.ERR_UPLOAD_COVER_FAILED, "cos upload error:" + stringBuilder.toString());

                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_COVER_FAILED, stringBuilder.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getCoverFileSize(), uploadInfo.getCoverImgType(), uploadInfo.getCoverName());
            }
        });
    }


    // 解析cos上传视频返回信息
    private void startUploadCoverFile(@NonNull CosXmlResult result) {
        // 第三步 通过COS上传封面
        if (uploadInfo.isNeedCover()) {
            uploadCosCover();
        } else {
            startFinishUploadUGC(result);
        }
    }


    // 通过COS上传视频
    private void uploadCosVideo() {
        new Thread() {
            @Override
            public void run() {
                reqTime = System.currentTimeMillis();

                Log.i(TAG, "uploadCosVideo begin :  cosBucket " + cosBucket + " cosVideoPath: " + cosVideoPath + "  path " + uploadInfo.getFilePath());

                try {
                    CosXmlResult result;
                    UploadService.ResumeData resumeData = new UploadService.ResumeData();
                    resumeData.bucket = cosBucket;
                    resumeData.cosPath = cosVideoPath;
                    resumeData.srcPath = uploadInfo.getFilePath();
                    resumeData.sliceSize = 1024 * 1024;
                    if (isResumeUploadVideo()) {
                        resumeData.uploadId = uploadId;
                    } else {
                        InitMultipartUploadRequest initMultipartUploadRequest = new InitMultipartUploadRequest(cosBucket, cosVideoPath);
                        initMultipartUploadRequest.setSign(600,null,null);
                        InitMultipartUploadResult initMultipartUploadResult = cosService.initMultipartUpload(initMultipartUploadRequest);
                        uploadId = initMultipartUploadResult.initMultipartUpload.uploadId;
                        setResumeData(uploadInfo.getFilePath(), vodSessionKey, uploadId);
                        resumeData.uploadId = uploadId;
                    }

                    cosUploadHelper = new UploadService(cosService, resumeData);
                    cosUploadHelper.setProgressListener(new CosXmlProgressListener() {
                        @Override
                        public void onProgress(long progress, long max) {
                            notifyUploadProgress(progress, max);
                        }
                    });

                    result = cosUploadHelper.resume(resumeData);
                    //分片上传完成之后清空本地缓存的断点续传信息
                    setResumeData(uploadInfo.getFilePath(), "", "");
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

                    Log.w(TAG,result.accessUrl);
                    Log.i(TAG, "uploadCosVideo finish:  cosBucket " + cosBucket + " cosVideoPath: " + cosVideoPath + "  path: " + uploadInfo.getFilePath() + "  size: " + uploadInfo.getFileSize());

                    startUploadCoverFile(result);
                } catch (CosXmlClientException e) {
                    Log.w(TAG, "CosXmlClientException =" + e.getMessage());
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "CosXmlClientException:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
                	//网络中断导致的
                    if (!TVCUtils.isNetworkAvailable(context)) {
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error: network unreachable");
                    } else if (busyFlag) { //其他错误，非主动取消
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                        setResumeData(uploadInfo.getFilePath(), "", "");
                    }
                } catch (CosXmlServiceException e) {
                    Log.w(TAG, "CosXmlServiceException =" + e.toString());
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "CosXmlServiceException: " + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
                    // 临时密钥过期，重新申请一次临时密钥，不中断上传
                    if (e.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                        getCosUploadInfo(uploadInfo, vodSessionKey);
                    } else {
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                        setResumeData(uploadInfo.getFilePath(), "", "");
                    }
                } catch (Exception e) {
                    Log.w(TAG,"Exception =" + e.toString());
                    txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "HTTP Code:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED, "cos upload video error:" + e.getMessage());
                    setResumeData(uploadInfo.getFilePath(), "", "");
                }
            }
        }.start();
    }

    // 解析cos上传视频返回信息
    private void startFinishUploadUGC(@NonNull CosXmlResult result) {
        String strAccessUrl = result.accessUrl;
        Log.i(TAG, "startFinishUploadUGC: " + strAccessUrl);

        reqTime = System.currentTimeMillis();

        // 第三步 上传结束
        ugcClient.finishUploadUGC(domain, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, @NonNull IOException e) {
                Log.i(TAG, "FinishUploadUGC: fail" + e.toString());
                notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, e.toString());

                txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
            }

            @Override
            public void onResponse(Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, "HTTP Code:" + response.code());
                    Log.e(TAG, "FinishUploadUGC->http code: " + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

                    throw new IOException("" + response);
                } else {
                    Log.i(TAG, "FinishUploadUGC Suc onResponse body : " + response.body().toString());
                    parseFinishRsp(response.body().string());
                }
            }
        });
    }


    // 解析结束上传返回信息.
    private void parseFinishRsp(String rspString) {
        Log.i(TAG, "parseFinishRsp: " + rspString);
        if (TextUtils.isEmpty(rspString)) {
            Log.e(TAG, "parseFinishRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, "finish response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, "finish response is empty", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

            return;
        }
        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            String message = jsonRsp.optString("message", "");
            if (0 != code) {
                notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, code + "|" + message);

                txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, code + "|" + message, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());

                return;
            }
            JSONObject dataRsp = jsonRsp.getJSONObject("data");
            String coverUrl = "";
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataRsp.getJSONObject("cover");
                coverUrl = coverObj.getString("url");
            }
            JSONObject videoObj = dataRsp.getJSONObject("video");
            String playUrl = videoObj.getString("url");
            videoFileId = dataRsp.getString("fileId");
            notifyUploadSuccess(videoFileId, playUrl, coverUrl);

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, 0, "", reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), videoFileId);

            Log.d(TAG, "playUrl:" + playUrl);
            Log.d(TAG, "coverUrl: " + coverUrl);
            Log.d(TAG, "videoFileId: " + videoFileId);
        } catch (JSONException e) {
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, e.toString());

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName());
        }
    }

    void txReport(int reqType, int errCode, String errMsg, long reqTime, long reqTimeCost, long fileSize, String fileType, String fileName) {
        txReport(reqType, errCode, errMsg, reqTime, reqTimeCost, fileSize, fileType, fileName, "");
    }

    /**
     * 数据上报
     * @param reqType：请求类型，标识是在那个步骤
     * @param errCode：错误码
     * @param errMsg：错误详细信息，COS的错误把requestId拼在错误信息里带回
     * @param reqTime：请求时间
     * @param reqTimeCost：耗时，单位ms
     * @param fileSize :文件大小
     * @param fileType :文件类型
     * @param fileId :上传完成后点播返回的fileid
     */
    void txReport(int reqType, int errCode, String errMsg, long reqTime, long reqTimeCost, long fileSize, String fileType, String fileName, String fileId) {
        try {
            String body = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version", TVCConstants.TVCVERSION);
            jsonObject.put("reqType", reqType);
            jsonObject.put("errCode", errCode);
            jsonObject.put("errMsg", errMsg);
            jsonObject.put("reqTimeCost", reqTimeCost);
            jsonObject.put("reqServerIp", ugcClient.getServerIP());
            jsonObject.put("platform", 2000); // 1000 - iOS, 2000 - Android
            jsonObject.put("device", Build.MANUFACTURER + Build.MODEL);
            jsonObject.put("osType", String.valueOf(Build.VERSION.SDK_INT));
            jsonObject.put("netType", TVCUtils.getNetWorkType(context));
            jsonObject.put("reqTime", reqTime);
            jsonObject.put("reportId", customKey);
            jsonObject.put("uuid", TVCUtils.getDevUUID(context));
            jsonObject.put("reqKey", String.valueOf(uploadInfo.getFileLastModifyTime()) + ";" + String.valueOf(initReqTime));
            jsonObject.put("appId", userAppId);
            jsonObject.put("fileSize", fileSize);
            jsonObject.put("fileType", fileType);
            jsonObject.put("fileName", fileName);
            jsonObject.put("vodSessionKey", vodSessionKey);
            jsonObject.put("fileId", fileId);
            body = jsonObject.toString();
            ugcClient.reportEvent(body, new Callback() {
                        @Override
                        public void onFailure(Call call, @NonNull IOException e) {
                            Log.e(TAG, "data report failed, msg:" + e.toString());
                        }

                        @Override
                        public void onResponse(Call call, @NonNull Response response) throws IOException {
                            Log.i(TAG, "data report response, msg:" + response.toString());
                        }
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 断点续传
    // 本地保存 filePath --> <session, uploadId, expireTime> 的映射集合，格式为json
    // session的过期时间是1天
    private void getResumeData(String filePath) {
        vodSessionKey = null;
        uploadId = null;
        fileLastModTime = 0;
        if (TextUtils.isEmpty(filePath) || enableResume == false) {
            return;
        }

        if (mSharedPreferences != null && mSharedPreferences.contains(filePath)) {
            try {
                JSONObject json = new JSONObject(mSharedPreferences.getString(filePath, ""));
                long expiredTime = json.optLong("expiredTime", 0);
                if (expiredTime > System.currentTimeMillis() / 1000) {
                    vodSessionKey = json.optString("session", "");
                    uploadId = json.optString("uploadId", "");
                    fileLastModTime = json.optLong("fileLastModTime", 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return;
    }

    private void setResumeData(@Nullable String filePath, String vodSessionKey, String uploadId) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        if (mSharedPreferences != null) {
            try {
                // vodSessionKey、uploadId为空就表示删掉该记录
                String itemPath = filePath;
                if ( TextUtils.isEmpty(vodSessionKey) || TextUtils.isEmpty(uploadId)) {
                    mShareEditor.remove(itemPath);
                    mShareEditor.commit();
                } else {
                    String comment = "";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("session", vodSessionKey);
                    jsonObject.put("uploadId", uploadId);
                    jsonObject.put("expiredTime", System.currentTimeMillis() / 1000 + 24 * 60 * 60);
                    jsonObject.put("fileLastModTime", uploadInfo.getFileLastModifyTime());
                    comment = jsonObject.toString();
                    mShareEditor.putString(itemPath, comment);
                    mShareEditor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 视频是否走断点续传
    public boolean isResumeUploadVideo() {
        if (enableResume
                && !TextUtils.isEmpty(uploadId)
                && uploadInfo != null && fileLastModTime != 0 && fileLastModTime == uploadInfo.getFileLastModifyTime()) {
            return true;
        }
        return false;
    }

    public void updateSignature(String signature) {
        if (ugcClient != null) {
            ugcClient.updateSignature(signature);
        }
    }
}

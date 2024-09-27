package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

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
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.quic.QuicClientImpl;
import com.tencent.tquic.impl.TnetConfig;
import com.tencent.qcloud.ugckit.module.upload.TXUGCPublishTypeDef;
import com.tencent.qcloud.ugckit.module.upload.impl.compute.TXHttpTaskMetrics;
import com.tencent.qcloud.ugckit.module.upload.impl.compute.TXOnGetHttpTaskMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 视频上传客户端
 */
public class TVCClient {

    private static final String TAG = "TVC-Client";
    private static final long SLICE_SIZE_MIN = 1024 * 1024;
    private static final long SLICE_SIZE_MAX = 1024 * 1024 * 10;
    private static final long SLICE_SIZE_ADAPTATION = 0;

    private Context context;
    private Handler mainHandler;
    private boolean busyFlag = false;
    private boolean cancelFlag = false;
    private TVCUploadInfo uploadInfo;
    private UGCClient ugcClient;
    private TVCUploadListener tvcListener;
    private int cosAppId;   // COS appid used for on-demand upload
    private int userAppId;  // Customer's own appid, required for data reporting
    private String uploadRegion = "";
    private String cosBucket;
    private String cosTmpSecretId = "";
    private String cosTmpSecretKey = "";
    private String cosToken = "";
    private long cosExpiredTime;
    private long localTimeAdvance = 0;        // Local time ahead of Unix timestamp interval
    private String cosVideoPath;
    private String videoFileId;
    private String cosCoverPath;
    private boolean isOpenCosAcc = false;   // Whether to use COS dynamic acceleration
    private String cosAccDomain = "";       // Dynamic acceleration domain name
    private String cosHost = "";
    private String domain;
    private String cosIP = "";
    private String vodSessionKey = null;
    private long reqTime = 0;            // Start request time for each stage
    private long initReqTime = 0;        // Upload request time, used for stitching reqKey. Serial request
    private String customKey = "";       // Used for data reporting
    private CosXmlService mCosXmlService;
    private COSXMLUploadTask mCOSXMLUploadTask;
    private TransferConfig mTransferConfig;
    private TransferManager mTransferManager;
    private String uploadId = null;
    private long fileLastModTime = 0;           // Video file last modified time
    private long coverFileLastModTime = 0;      // Cover file last modified time
    private boolean enableResume = true;
    private boolean enableHttps = false;
    private UGCReport.ReportInfo reportInfo;
    private static final int VIRTUAL_TOTAL_PERCENT = 10;    // Percentage of virtual progress before and after
    private TimerTask virtualProgress = null;   // Virtual progress task
    private Timer mTimer;
    private int virtualPercent = 0;             // Virtual progress
    private boolean realProgressFired = false;
    private int vodCmdRequestCount = 0;           // VOD signaling retry count
    // Main domain request failure msg, used for backup domain request failure, bring back to report
    private String mainVodServerErrMsg;
    private long mSliceSize = SLICE_SIZE_MIN;
    private int mConcurrentCount;
    private long mTrafficLimit = -1;
    private IUploadResumeController mUploadResumeController; // Breakpoint controller
    private boolean mIsDebuggable = true;
    private final String mUploadKey;

    /**
     * Initialize upload instance
     * 初始化上传实例
     */
    public TVCClient(Context context, TVCConfig tvcConfig, String uploadKey) {
        mainHandler = new Handler(context.getMainLooper());
        this.context = context.getApplicationContext();
        reportInfo = new UGCReport.ReportInfo();
        mUploadKey = uploadKey;
        updateConfig(tvcConfig);
    }


    /**
     * Update configuration
     * 更新配置
     */
    public void updateConfig(TVCConfig tvcConfig) {
        this.enableResume = tvcConfig.mEnableResume;
        this.enableHttps = tvcConfig.mEnableHttps;
        this.customKey = tvcConfig.mCustomKey;
        this.mConcurrentCount = tvcConfig.mConcurrentCount;
        this.mIsDebuggable = tvcConfig.mIsDebuggable;
        this.mTrafficLimit = tvcConfig.mTrafficLimit;
        ugcClient = UGCClient.getInstance(tvcConfig.mSignature, tvcConfig.mVodReqTimeOutInSec);
        mUploadResumeController = tvcConfig.mUploadResumeController;
        if (null == mUploadResumeController) {
            mUploadResumeController = new UploadResumeDefaultController(context);
        }
        clearLocalCache();
        // The SDK minimum is 1M and the maximum is 10M. If it is not set,
        // it is set to 0, which is ten times the size of the file
        if (tvcConfig.mSliceSize == 0) {
            mSliceSize = SLICE_SIZE_ADAPTATION;
        } else {
            mSliceSize = fixSliceSize(tvcConfig.mSliceSize);
        }
    }

    // Clean up local cache, delete expired ones
    private void clearLocalCache() {
        mUploadResumeController.clearLocalCache();
    }

    private long fixSliceSize(long sliceSize) {
        if (sliceSize < SLICE_SIZE_MIN) {
            sliceSize = SLICE_SIZE_MIN;
        } else if (sliceSize > SLICE_SIZE_MAX) {
            sliceSize = SLICE_SIZE_MAX;
        }
        return sliceSize;
    }

    private void startTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (virtualProgress != null) {
            virtualProgress = null;
        }

        virtualProgress = new TimerTask() {
            @Override
            public void run() {
                postVirtualProgress();
            }
        };

        mTimer = new Timer();
        // Virtual progress before and after lasts for about 2 seconds
        mTimer.schedule(virtualProgress, 2000 / VIRTUAL_TOTAL_PERCENT, 2000 / VIRTUAL_TOTAL_PERCENT);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (virtualProgress != null) {
            virtualProgress = null;
        }
    }

    private void postVirtualProgress() {
        if (uploadInfo != null) {
            long total = uploadInfo.getFileSize() + (uploadInfo.isNeedCover() ? uploadInfo.getCoverFileSize() : 0);
            if ((virtualPercent >= 0 && virtualPercent < 10) || (virtualPercent >= 90 && virtualPercent < 100)) {
                ++virtualPercent;
                notifyUploadProgress(virtualPercent * total / 100, total);
            }
        }
    }

    // Notify upper layer of successful upload
    private void notifyUploadSuccess(final String fileId, final String playUrl, final String coverUrl) {
        TXUGCPublishOptCenter.getInstance().delPublishing(uploadInfo.getFilePath());
        final long total = uploadInfo.getFileSize() + (uploadInfo.isNeedCover() ? uploadInfo.getCoverFileSize() : 0);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onProgress(total, total);
                tvcListener.onSuccess(fileId, playUrl, coverUrl);
            }
        });
        stopTimer();
    }

    // Notify upper layer of upload failure
    private void notifyUploadFailed(final int errCode, final String errMsg) {
        TXUGCPublishOptCenter.getInstance().delPublishing(uploadInfo.getFilePath());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onFailed(errCode, errMsg);
            }
        });
        stopTimer();
    }

    // Notify upper layer of upload progress
    private void notifyUploadProgress(final long currentSize, final long totalSize) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                tvcListener.onProgress(currentSize, totalSize);
            }
        });
    }

    private boolean isVideoFileExist(String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            TVCLog.e("getFileSize", "getFileSize: " + e);
            return false;
        }
        return false;
    }

    /**
     * Upload video file
     * 上传视频文件
     */
    public int uploadVideo(TVCUploadInfo info, TVCUploadListener listener) {
        if (busyFlag) {     // Avoid one object transferring multiple files
            return TVCConstants.ERR_CLIENT_BUSY;
        }
        busyFlag = true;
        // reset
        cosVideoPath = null;
        cancelFlag = false;
        this.uploadInfo = info;
        this.tvcListener = listener;

        String fileName = info.getFileName();
        TVCLog.i(TAG, "fileName = " + fileName);
        if (fileName != null && fileName.getBytes().length > 200) { // Video file name is too long
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name too long");
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME, 0,
                    "", "file name too long",
                    System.currentTimeMillis(), 0, uploadInfo.getFileSize(), uploadInfo.getFileType(),
                    uploadInfo.getFileName()
                    , "", "", 0, 0);

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (info.isContainSpecialCharacters(fileName)) {// Video file name contains special characters
            tvcListener.onFailed(TVCConstants.ERR_UGC_FILE_NAME, "file name contains special character / : * ? \" < >");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_FILE_NAME, 0, "",
                    "file name contains " + "special character / : * ? \" < >", System.currentTimeMillis(), 0,
                    uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

            return TVCConstants.ERR_UGC_FILE_NAME;
        }

        if (!TXUGCPublishOptCenter.getInstance().isPublishing(info.getFilePath()) && enableResume) {
            getResumeData(info.getFilePath());
        }
        TXUGCPublishOptCenter.getInstance().addPublishing(info.getFilePath());
        applyUploadUGC(info, vodSessionKey);
        return TVCConstants.NO_ERROR;
    }

    /**
     * Cancel (interrupt) upload. After the interruption, resume uploading by calling
     * uploadVideo with the same parameters
     * 取消（中断）上传。中断之后恢复上传再用相同的参数调用uploadVideo即可。
     */
    public void cancelUpload() {
        cancelFlag = true;
        if (mCOSXMLUploadTask != null) {
            mCOSXMLUploadTask.pause();
        }
        if (null != mCosXmlService) {
            mCosXmlService.cancelAll();
        }
    }

    // Apply for upload to VOD and get COS upload information
    private void applyUploadUGC(TVCUploadInfo info, String vodSessionKey) {
        startTimer();   // Start initial virtual progress
        // Step 1: Request upload to UGC (get COS authentication information)
        reqTime = System.currentTimeMillis();
        initReqTime = reqTime;
        getCosUploadInfo(info, vodSessionKey, TVCConstants.VOD_SERVER_HOST);
    }

    private void getCosUploadInfo(final TVCUploadInfo info, final String vodSessionKey, final String domain) {
        ugcClient.initUploadUGC(domain, info, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TVCLog.e(TAG, "initUploadUGC->onFailure: " + e.toString());
                if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST)) {
                    if (++vodCmdRequestCount < TVCConstants.MAX_REQUEST_COUNT) {
                        getCosUploadInfo(info, vodSessionKey, TVCConstants.VOD_SERVER_HOST);
                    } else {
                        vodCmdRequestCount = 0;
                        mainVodServerErrMsg = e.toString();
                        getCosUploadInfo(info, vodSessionKey, TVCConstants.VOD_SERVER_HOST_BAK);
                    }
                } else if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST_BAK)) {
                    if (++vodCmdRequestCount < TVCConstants.MAX_REQUEST_COUNT) {
                        getCosUploadInfo(info, vodSessionKey, TVCConstants.VOD_SERVER_HOST_BAK);
                    } else {
                        notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, e.toString());
                        String errMsg = e.toString();
                        if (!TextUtils.isEmpty(mainVodServerErrMsg)) {
                            errMsg += "|" + mainVodServerErrMsg;
                        }
                        txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, 1,
                                "",
                                errMsg, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(),
                                uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, "HTTP Code:" + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED,
                            response.code(),
                            "", "HTTP Code:" + response.code(), reqTime, System.currentTimeMillis() - reqTime,
                            uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

                    setResumeData(uploadInfo.getFilePath(), "", "");

                    TVCLog.e(TAG, "initUploadUGC->http code: " + response.code());
                    throw new IOException("" + response);
                } else {
                    vodCmdRequestCount = 0;
                    mainVodServerErrMsg = "";
                    parseInitRsp(response.body().string());
                }
            }
        });
    }

    // Parse upload request return information
    private void parseInitRsp(String rspString) {
        TVCLog.i(TAG, "parseInitRsp: " + rspString);
        if (cancelFlag) {
            TVCLog.i(TAG, "upload is cancel when ready to upload to cos");
            busyFlag = false;
            cancelFlag = false;
            notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual pause");
            return;
        }
        if (TextUtils.isEmpty(rspString)) {
            TVCLog.e(TAG, "parseInitRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, "init response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_PARSE_FAILED, 2, "",
                    "init response " + "is" + " empty", reqTime, System.currentTimeMillis() - reqTime,
                    uploadInfo.getFileSize()
                    , uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

            setResumeData(uploadInfo.getFilePath(), "", "");

            return;
        }
        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            TVCLog.i(TAG, "parseInitRsp: " + code);

            String message = "";
            try {
                message = new String(jsonRsp.optString("message", "").getBytes("UTF-8"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (0 != code) {
                // Do not report expired signatures
                if (code == 10010) {
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_SIGN_EXPIRED, code + "|" + message);
                } else {
                    notifyUploadFailed(TVCConstants.ERR_UGC_REQUEST_FAILED, code + "|" + message);
                    txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_REQUEST_FAILED, code, "",
                            code + "|" + message, reqTime, System.currentTimeMillis() - reqTime,
                            uploadInfo.getFileSize(),
                            uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

                }
                vodSessionKey = null;
                setResumeData(uploadInfo.getFilePath(), "", "");
                return;
            }

            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONObject videoObj = dataObj.getJSONObject("video");
            cosVideoPath = videoObj.getString("storagePath");

            // COS upload temporary certificate
            JSONObject tempCertificate = dataObj.getJSONObject("tempCertificate");
            cosTmpSecretId = tempCertificate.optString("secretId");
            cosTmpSecretKey = tempCertificate.optString("secretKey");
            cosToken = tempCertificate.optString("token");
            cosExpiredTime = tempCertificate.optLong("expiredTime");

            long serverTS = dataObj.optLong("timestamp", 0);

            TVCLog.i(TAG, "isNeedCover:" + uploadInfo.isNeedCover());
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataObj.getJSONObject("cover");
                cosCoverPath = coverObj.getString("storagePath");
            }
            cosAppId = dataObj.getInt("storageAppId");
            // After upgrading from 5.4.10 to 5.4.20, the setAppIdAndRegion interface is deprecated,
            // and you need to stitch the costBucket format yourself to ensure it is bucket-appId
            cosBucket = dataObj.getString("storageBucket") + "-" + cosAppId;
            uploadRegion = dataObj.getString("storageRegionV5");
            domain = dataObj.getString("domain");
            vodSessionKey = dataObj.getString("vodSessionKey");
            userAppId = dataObj.getInt("appId");

            JSONObject cosAccObj = dataObj.optJSONObject("cosAcc");
            if (cosAccObj != null) {
                isOpenCosAcc = cosAccObj.optInt("isOpen", 0) == 0 ? false : true;
                cosAccDomain = cosAccObj.optString("domain", "");
            }

            TVCLog.i(TAG, "cosVideoPath=" + cosVideoPath);
            TVCLog.i(TAG, "cosCoverPath=" + cosCoverPath);
            TVCLog.i(TAG, "cosAppId=" + cosAppId);
            TVCLog.i(TAG, "cosBucket=" + cosBucket);
            TVCLog.i(TAG, "uploadRegion=" + uploadRegion);
            TVCLog.i(TAG, "domain=" + domain);
            TVCLog.i(TAG, "vodSessionKey=" + vodSessionKey);
            TVCLog.i(TAG, "cosAcc.isOpen=" + isOpenCosAcc);
            TVCLog.i(TAG, "cosAcc.domain=" + cosAccDomain);

            CosXmlServiceConfig.Builder builder = new CosXmlServiceConfig.Builder()
                    .setRegion(uploadRegion)
                    .setDebuggable(mIsDebuggable)
                    .setAccelerate(isOpenCosAcc)
                    .isHttps(enableHttps)
                    .setSocketTimeout(TVCConstants.UPLOAD_TIME_OUT_SEC * 1000)
                    .dnsCache(true);

            if (mConcurrentCount > 0) {
                builder.setUploadMaxThreadCount(mConcurrentCount);
            }
            boolean isQuic = TXUGCPublishOptCenter.getInstance().isNeedEnableQuic(uploadRegion);
            if (isQuic) {
                builder.enableQuic(true).setPort(QuicClient.PORT);
                QuicClientImpl.setTnetConfig(new TnetConfig.Builder()
                        .setIsCustom(false)
                        .setTotalTimeoutMillis(TVCConstants.UPLOAD_TIME_OUT_SEC * 1000)
                        .setConnectTimeoutMillis(TVCConstants.UPLOAD_CONNECT_TIME_OUT_MILL)
                        .build());
            }
            TVCLog.i(TAG, "domain:" + uploadRegion + ",isQuic:" + isQuic);

            CosXmlServiceConfig cosXmlServiceConfig = builder.builder();

            cosHost = getCosIP(cosXmlServiceConfig);

            long localTS = System.currentTimeMillis() / 1000L;
            if (serverTS > 0 && (localTS - serverTS > 5 * 60 || serverTS - localTS > 5 * 60)) {
                localTimeAdvance = localTS - serverTS;
            }
            if (null == mCosXmlService) {
                mCosXmlService = new CosXmlService(context, cosXmlServiceConfig,
                        new TVCDirectCredentialProvider(cosTmpSecretId, cosTmpSecretKey, cosToken,
                                localTS - localTimeAdvance, cosExpiredTime));
            } else {
                // force update request client
                mCosXmlService.setNetworkClient(cosXmlServiceConfig);
            }

            List<String> cosIps = TXUGCPublishOptCenter.getInstance().query(cosHost);
            if (cosIps != null && cosIps.size() > 0) {
                // prevent data race between #size() and #toArray()
                final int cosIpSize = cosIps.size();
                String[] ipArray = cosIps.toArray(new String[cosIpSize]);
                TVCLog.i(TAG, "add cos domain " + cosHost + " ,ip:" + Arrays.toString(ipArray));
                mCosXmlService.addCustomerDNS(cosHost, ipArray);
            }

            // Step 2: Upload the video through COS
            uploadCosVideo();
        } catch (JSONException e) {
            TVCLog.e(TAG, e.toString());
            setResumeData(uploadInfo.getFilePath(), "", "");
            txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, TVCConstants.ERR_UGC_PARSE_FAILED, 3, "",
                    e.toString(),
                    reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(), uploadInfo.getFileType(),
                    uploadInfo.getFileName(), "", "", 0, 0);
            notifyUploadFailed(TVCConstants.ERR_UGC_PARSE_FAILED, e.toString());
            return;
        } catch (CosXmlClientException e) {
            // An exception occurs in addCustomerDNS, which does not affect normal upload
            TVCLog.e(TAG, e.toString());
        }

        txReport(TVCConstants.UPLOAD_EVENT_ID_REQUEST_UPLOAD, 0, 0, "", "", reqTime,
                System.currentTimeMillis() - reqTime,
                uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
    }

    private String getCosIP(CosXmlServiceConfig cosXmlServiceConfig) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, cosCoverPath, uploadInfo.getCoverPath());
        final String cosHost = putObjectRequest.getRequestHost(cosXmlServiceConfig);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(cosHost);
                    cosIP = address.getHostAddress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return cosHost;
    }

    // Upload cover through COS
    private void uploadCosCover() {
        reqTime = System.currentTimeMillis();
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, cosCoverPath, uploadInfo.getCoverPath());
        putObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                TVCLog.i(TAG, "uploadCosCover->progress: " + progress + "/" + max);
                if (progress >= max) {
                    virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                    startTimer();   // Upload completed, start ending virtual progress
                } else {
                    max += uploadInfo.getFileSize();
                    notifyUploadProgress((progress + uploadInfo.getFileSize()) * (100 - 2 * VIRTUAL_TOTAL_PERCENT) / 100
                            + max * VIRTUAL_TOTAL_PERCENT / 100, max);
                }
            }
        });

        putObjectRequest.isSupportAccelerate(isOpenCosAcc);
        final TXHttpTaskMetrics metrics = new TXHttpTaskMetrics();
        putObjectRequest.attachMetrics(metrics);
        mCosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                String requestId = getRequestId(cosXmlResult);
                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, 0, "", "", reqTime,
                        System.currentTimeMillis() - reqTime,
                        uploadInfo.getCoverFileSize(), uploadInfo.getCoverImgType(), uploadInfo.getCoverName(), "",
                        requestId,
                        metrics.getTCPConnectionTimeCost(), metrics.getRecvRspTimeCost());
                reqTime = System.currentTimeMillis();
                startFinishUploadUGC(cosXmlResult, TVCConstants.VOD_SERVER_HOST);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException,
                               CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                String cosErr = "";
                if (qcloudException != null) {
                    stringBuilder.append(qcloudException.getMessage());
                    cosErr = String.valueOf(qcloudException.errorCode);
                } else {
                    stringBuilder.append(qcloudServiceException.toString());
                    cosErr = qcloudServiceException.getErrorCode();
                }

                notifyUploadFailed(TVCConstants.ERR_UPLOAD_COVER_FAILED,
                        "upload cover cos code:" + cosErr + ", cos desc:" + stringBuilder.toString());

                String requestId = "";
                if (qcloudServiceException != null) {
                    requestId = qcloudServiceException.getRequestId();
                }
                txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_COVER_FAILED, 0, cosErr,
                        stringBuilder.toString(), reqTime, System.currentTimeMillis() - reqTime,
                        uploadInfo.getCoverFileSize(),
                        uploadInfo.getCoverImgType(), uploadInfo.getCoverName(), "", requestId,
                        metrics.getTCPConnectionTimeCost(), metrics.getRecvRspTimeCost());
            }
        });
    }

    private String getRequestId(CosXmlResult result) {
        if (result == null || result.headers == null) {
            return "";
        }
        List<String> requestIds = result.headers.get("x-cos-request-id");
        String requestId = requestIds != null && requestIds.size() != 0 ? requestIds.get(0) : "";
        return requestId;
    }


    // Parse COS video upload return information
    private void startUploadCoverFile(CosXmlResult result) {
        // Step 3: Upload cover through COS
        if (uploadInfo.isNeedCover()) {
            uploadCosCover();
        } else {
            startFinishUploadUGC(result, TVCConstants.VOD_SERVER_HOST);
        }
    }


    // Upload video through COS
    private void uploadCosVideo() {
        new Thread() {
            @Override
            public void run() {
                reqTime = System.currentTimeMillis();

                TVCLog.i(TAG,
                        "uploadCosVideo begin :  cosBucket " + cosBucket + " cosVideoPath: " + cosVideoPath + "  path"
                                + " " + uploadInfo.getFilePath());
                long tcpConnectionTimeCost = 0;
                long recvRspTimeCost = 0;
                long sliceSize = getSliceSize();
                try {
                    TXUGCPublishTypeDef.TXPublishResumeData resumeData = new TXUGCPublishTypeDef.TXPublishResumeData();
                    resumeData.bucket = cosBucket;
                    resumeData.cosPath = cosVideoPath;
                    resumeData.srcPath = uploadInfo.getFilePath();
                    resumeData.sliceSize = sliceSize;

                    boolean hasComputeTimeCost = false;

                    if (!isResumeUploadVideo()) {
                        hasComputeTimeCost = true;
                        InitMultipartUploadRequest initMultipartUploadRequest =
                                new InitMultipartUploadRequest(cosBucket,
                                        cosVideoPath);
                        initMultipartUploadRequest.isSupportAccelerate(isOpenCosAcc);
                        // Use HttpTaskMetrics to count the time consumed
                        TXHttpTaskMetrics metrics = new TXHttpTaskMetrics();
                        initMultipartUploadRequest.attachMetrics(metrics);
                        InitMultipartUploadResult initMultipartUploadResult =
                                mCosXmlService.initMultipartUpload(initMultipartUploadRequest);
                        // After initMultipartUpload, you can get the time consumed
                        recvRspTimeCost = metrics.getRecvRspTimeCost();
                        tcpConnectionTimeCost = metrics.getTCPConnectionTimeCost();
                        uploadId = initMultipartUploadResult.initMultipartUpload.uploadId;
                        setResumeData(uploadInfo.getFilePath(), vodSessionKey, uploadId);
                    }
                    resumeData.uploadId = uploadId;

                    // This can be customized, but we can use the default settings
                    mTransferConfig = new TransferConfig.Builder().setSliceSizeForUpload(sliceSize).build();
                    mTransferManager = new TransferManager(mCosXmlService, mTransferConfig);
                    TVCLog.i(TAG, "resumeData.srcPath: " + resumeData.srcPath);
                    if (cancelFlag) {
                        TVCLog.i(TAG, "upload is cancel when ready to upload to cos");
                        busyFlag = false;
                        cancelFlag = false;
                        notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual pause");
                        return;
                    }
                    PutObjectRequest putObjectRequest;
                    if (resumeData.srcPath.startsWith("content://")) {
                        putObjectRequest = new PutObjectRequest(resumeData.bucket, resumeData.cosPath,
                                Uri.parse(resumeData.srcPath));
                    } else {
                        putObjectRequest = new PutObjectRequest(resumeData.bucket, resumeData.cosPath,
                                resumeData.srcPath);
                    }
                    if (mTrafficLimit > 0) {
                        putObjectRequest.setTrafficLimit(mTrafficLimit);
                    }
                    mCOSXMLUploadTask = mTransferManager.upload(putObjectRequest, resumeData.uploadId);

                    mCOSXMLUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
                        @Override
                        public void onProgress(long progress, long max) {
                            if (uploadInfo.isNeedCover()) {
                                max += uploadInfo.getCoverFileSize();
                            }

                            if (!realProgressFired) {
                                // COS upload starts to have progress, stop the start virtual progress callback
                                stopTimer();
                                realProgressFired = true;
                            }

                            if (progress >= max) {
                                virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                                // Upload completed, start ending virtual progress
                                startTimer();
                            } else {
                                notifyUploadProgress(progress * (100 - 2 * VIRTUAL_TOTAL_PERCENT) / 100
                                        + VIRTUAL_TOTAL_PERCENT * max / 100, max);
                            }
                        }
                    });

                    TXOnGetHttpTaskMetrics onGetHttpTaskMetrics = null;
                    // If it hasn't been initialized, the first packet connection has not been counted;
                    // then you need to add a listener to the Service
                    if (!hasComputeTimeCost) {
                        onGetHttpTaskMetrics = new TXOnGetHttpTaskMetrics();
                        mCOSXMLUploadTask.setOnGetHttpTaskMetrics(onGetHttpTaskMetrics);
                    }

                    // After synchronously obtaining the result, you can get the connection time consumption
                    if (onGetHttpTaskMetrics != null) {
                        tcpConnectionTimeCost = onGetHttpTaskMetrics.getTCPConnectionTimeCost();
                        recvRspTimeCost = onGetHttpTaskMetrics.getRecvRspTimeCost();
                    }

                    final long finalTcpConnectionTimeCost = tcpConnectionTimeCost;
                    final long finalRecvRspTimeCost = recvRspTimeCost;

                    mCOSXMLUploadTask.setCosXmlResultListener(new MyCosXmlResultListener(finalTcpConnectionTimeCost
                            , finalRecvRspTimeCost));

                    mCOSXMLUploadTask.setTransferStateListener(new TransferStateListener() {
                        @Override
                        public void onStateChanged(TransferState state) {
                            if (cancelFlag && state == TransferState.PAUSED) {
                                busyFlag = false;
                                cancelFlag = false;
                                setResumeData(uploadInfo.getFilePath(), vodSessionKey, uploadId);
                                notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual "
                                        + "pause");
                            }
                        }
                    });

                } catch (Exception e) {
                    TVCLog.w(TAG, "Exception =" + e);
                    if (mCosXmlService.getConfig().isEnableQuic()) {
                        // QUIC failed, re-upload with HTTP
                        quicTransToHttpRetry();
                        txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_QUIC_FAILED, 0,
                                "Exception",
                                "quic upload failed, retry on http ,error:" + e.getMessage(), reqTime,
                                System.currentTimeMillis() - reqTime,
                                uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "",
                                "", 0, 0);
                    } else {
                        setResumeData(uploadInfo.getFilePath(), "", "");
                        txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, TVCConstants.ERR_UPLOAD_VIDEO_FAILED, 0,
                                "Exception",
                                "HTTP Code:" + e.getMessage(), reqTime, System.currentTimeMillis() - reqTime,
                                uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "",
                                "", 0, 0);
                        notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED,
                                "cos upload video error:" + e.getMessage());
                    }
                }
            }
        }.start();
    }

    private void quicTransToHttpRetry() {
        // quic request failed,switch to http
        TVCLog.e(TAG, "quic request failed,switch to http");
        stopTimer();
        TXUGCPublishOptCenter.getInstance().disableQuicIfNeed();
        applyUploadUGC(uploadInfo, vodSessionKey);
    }

    private long getSliceSize() {
        long sliceSize = mSliceSize;
        if (sliceSize == SLICE_SIZE_ADAPTATION) {
            if (uploadInfo.getFileSize() > 0) {
                sliceSize = fixSliceSize(uploadInfo.getFileSize() / 10);
            } else {
                TVCLog.w(TAG, "file size invalid,set sliceSize to SLICE_SIZE_MIN");
                sliceSize = SLICE_SIZE_MIN;
            }
        }
        return sliceSize;
    }

    // Parse COS video upload return information
    private void startFinishUploadUGC(final CosXmlResult result, final String domain) {
        String strAccessUrl = result.accessUrl;
        TVCLog.i(TAG, "startFinishUploadUGC: " + strAccessUrl);

        // Step 3: Upload completed
        ugcClient.finishUploadUGC(domain, customKey, vodSessionKey, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TVCLog.i(TAG, "FinishUploadUGC: fail" + e.toString());
                if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST)) {
                    if (++vodCmdRequestCount < TVCConstants.MAX_REQUEST_COUNT) {
                        startFinishUploadUGC(result, TVCConstants.VOD_SERVER_HOST);
                    } else {
                        vodCmdRequestCount = 0;
                        mainVodServerErrMsg = e.toString();
                        startFinishUploadUGC(result, TVCConstants.VOD_SERVER_HOST_BAK);
                    }
                } else if (domain.equalsIgnoreCase(TVCConstants.VOD_SERVER_HOST_BAK)) {
                    if (++vodCmdRequestCount < TVCConstants.MAX_REQUEST_COUNT) {
                        startFinishUploadUGC(result, TVCConstants.VOD_SERVER_HOST_BAK);
                    } else {
                        String errMsg = e.toString();
                        if (!TextUtils.isEmpty(mainVodServerErrMsg)) {
                            errMsg += "|" + mainVodServerErrMsg;
                        }
                        notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, e.toString());
                        txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT,
                                TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, 1, "",
                                errMsg, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(),
                                uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED, "HTTP Code:" + response.code());
                    TVCLog.e(TAG, "FinishUploadUGC->http code: " + response.code());

                    txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_REQUEST_FAILED,
                            response.code(), "", "HTTP Code:" + response.code(), reqTime,
                            System.currentTimeMillis() - reqTime,
                            uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

                    throw new IOException("" + response);
                } else {
                    TVCLog.i(TAG, "FinishUploadUGC Suc onResponse body : " + response.body().toString());
                    parseFinishRsp(response.body().string());
                }
            }
        });
    }

    // Parse end upload return information
    private void parseFinishRsp(String rspString) {
        TVCLog.i(TAG, "parseFinishRsp: " + rspString);
        if (cancelFlag) {
            TVCLog.i(TAG, "upload is cancel when ready to upload to cos");
            busyFlag = false;
            cancelFlag = false;
            notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual pause");
            return;
        }
        if (TextUtils.isEmpty(rspString)) {
            TVCLog.e(TAG, "parseFinishRsp->response is empty!");
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, "finish response is empty");

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, 2, "",
                    "finish " + "response is empty", reqTime, System.currentTimeMillis() - reqTime,
                    uploadInfo.getFileSize(),
                    uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

            return;
        }
        try {
            JSONObject jsonRsp = new JSONObject(rspString);
            int code = jsonRsp.optInt("code", -1);
            String message = jsonRsp.optString("message", "");
            if (0 != code) {
                notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, code + "|" + message);

                txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED,
                        code, "",
                        code + "|" + message, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(),
                        uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);

                return;
            }
            JSONObject dataRsp = jsonRsp.getJSONObject("data");
            String coverUrl = "";
            if (uploadInfo.isNeedCover()) {
                JSONObject coverObj = dataRsp.getJSONObject("cover");
                coverUrl = coverObj.getString("url");
                if (enableHttps) {
                    coverUrl = coverUrl.replace("http:", "https:");
                }
            }
            JSONObject videoObj = dataRsp.getJSONObject("video");
            String playUrl = videoObj.getString("url");
            if (enableHttps) {
                playUrl = playUrl.replace("http:", "https:");
            }
            videoFileId = dataRsp.getString("fileId");
            notifyUploadSuccess(videoFileId, playUrl, coverUrl);

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, 0, 0, "", "", reqTime,
                    System.currentTimeMillis() - reqTime,
                    uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(), videoFileId, "", 0,
                    0);

            TVCLog.i(TAG, "playUrl:" + playUrl);
            TVCLog.i(TAG, "coverUrl: " + coverUrl);
            TVCLog.i(TAG, "videoFileId: " + videoFileId);
        } catch (JSONException e) {
            notifyUploadFailed(TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, e.toString());

            txReport(TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT, TVCConstants.ERR_UGC_FINISH_RESPONSE_FAILED, 3, "",
                    e.toString(), reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(),
                    uploadInfo.getFileType(), uploadInfo.getFileName(), "", "", 0, 0);
        }
    }


    /**
     * Data reporting
     * 数据上报
     *
     * @param reqType：Request type, indicating which step
     *               请求类型，标识是在那个步骤
     * @param errCode：Error code
     *               错误码
     * @param vodErrCode：Error code returned by VOD
     *                  点播返回的错误码
     * @param cosErrCode：Error code for COS upload, string
     *                  COS上传的错误码，字符串
     * @param errMsg：Error details, COS error with requestId included in the error information
     *              错误详细信息，COS的错误把requestId拼在错误信息里带回
     * @param reqTime：Request time
     *               请求时间
     * @param reqTimeCost：Time consumption, in milliseconds
     *                   耗时，单位ms
     * @param fileSize:File size
     *                文件大小
     * @param fileType:File type
     *                文件类型
     * @param fileId:Fileid returned by VOD after upload completion
     *              上传完成后点播返回的fileid
     */
    void txReport(int reqType, int errCode, int vodErrCode, String cosErrCode, String errMsg, long reqTime,
                  long reqTimeCost,
                  long fileSize, String fileType, String fileName, String fileId, String cosRequestId,
                  long cosTcpConnTimeCost,
                  long cosRecvRespTimeCost) {
        reportInfo.reqType = reqType;
        reportInfo.errCode = errCode;
        reportInfo.errMsg = errMsg;
        reportInfo.reqTime = reqTime;
        reportInfo.reqTimeCost = reqTimeCost;
        reportInfo.fileSize = fileSize;
        reportInfo.fileType = fileType;
        reportInfo.fileName = fileName;
        reportInfo.fileId = fileId;
        reportInfo.appId = userAppId;
        reportInfo.vodErrCode = vodErrCode;
        reportInfo.cosErrCode = cosErrCode;
        reportInfo.cosRegion = uploadRegion;
        if (reqType == TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD) {
            reportInfo.useHttpDNS = TXUGCPublishOptCenter.getInstance().useHttpDNS(cosHost) ? 1 : 0;
            reportInfo.reqServerIp = cosIP;
            reportInfo.tcpConnTimeCost = cosTcpConnTimeCost;
            reportInfo.recvRespTimeCost = cosRecvRespTimeCost;
            reportInfo.requestId = cosRequestId == null ? "" : cosRequestId;
        } else {
            reportInfo.useHttpDNS = TXUGCPublishOptCenter.getInstance().useHttpDNS(TVCConstants.VOD_SERVER_HOST) ? 1
                    : 0;
            reportInfo.reqServerIp = ugcClient.getServerIP();
            reportInfo.tcpConnTimeCost = ugcClient.getTcpConnTimeCost();
            reportInfo.recvRespTimeCost = ugcClient.getRecvRespTimeCost();
            reportInfo.requestId = "";
        }
        reportInfo.useCosAcc = isOpenCosAcc ? 1 : 0;
        reportInfo.reportId = customKey;
        reportInfo.reqKey = String.valueOf(uploadInfo.getFileLastModifyTime()) + ";" + String.valueOf(initReqTime);
        reportInfo.vodSessionKey = vodSessionKey;
        reportInfo.cosVideoPath = cosVideoPath;
        UGCReport.getInstance(context).addReportInfo(reportInfo);

        if ((errCode == 0 && reqType == TVCConstants.UPLOAD_EVENT_ID_UPLOAD_RESULT) || errCode != 0) {
            UGCReport.ReportInfo dauReportInfo = new UGCReport.ReportInfo(reportInfo);
            dauReportInfo.reqType = TVCConstants.UPLOAD_EVENT_DAU;
            UGCReport.getInstance(context).addReportInfo(dauReportInfo);
        }
    }

    // Resumable upload
    private void getResumeData(String filePath) {
        vodSessionKey = null;
        uploadId = null;
        fileLastModTime = 0;
        coverFileLastModTime = 0;
        if (enableResume) {
            ResumeCacheData resumeCacheData = mUploadResumeController.getResumeData(filePath, mUploadKey);
            if (null != resumeCacheData) {
                vodSessionKey = resumeCacheData.getVodSessionKey();
                uploadId = resumeCacheData.getUploadId();
                fileLastModTime = resumeCacheData.getFileLastModTime();
                coverFileLastModTime = resumeCacheData.getCoverFileLastModTime();
            }
        }
    }

    private void setResumeData(String filePath, String vodSessionKey, String uploadId) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        mUploadResumeController.saveSession(filePath, vodSessionKey, uploadId, uploadInfo, mUploadKey);
    }

    // Whether the video is resumable
    public boolean isResumeUploadVideo() {
        if (enableResume) {
            return mUploadResumeController.isResumeUploadVideo(uploadId, uploadInfo, vodSessionKey,
                    fileLastModTime, coverFileLastModTime, mUploadKey);
        }
        return false;
    }

    public void updateSignature(String signature) {
        if (ugcClient != null) {
            ugcClient.updateSignature(signature);
        }
    }

    public Bundle getStatusInfo() {
        Bundle b = new Bundle();
        b.putString("reqType", String.valueOf(reportInfo.reqType));
        b.putString("errCode", String.valueOf(reportInfo.errCode));
        b.putString("errMsg", reportInfo.errMsg);
        b.putString("reqTime", String.valueOf(reportInfo.reqTime));
        b.putString("reqTimeCost", String.valueOf(reportInfo.reqTimeCost));
        b.putString("fileSize", String.valueOf(reportInfo.fileSize));
        b.putString("fileType", reportInfo.fileType);
        b.putString("fileName", reportInfo.fileName);
        b.putString("fileId", reportInfo.fileId);
        b.putString("appId", String.valueOf(reportInfo.appId));
        b.putString("reqServerIp", reportInfo.reqServerIp);
        b.putString("reportId", reportInfo.reportId);
        b.putString("cosVideoPath", reportInfo.cosVideoPath);
        b.putString("reqKey", reportInfo.reqKey);
        b.putString("vodSessionKey", reportInfo.vodSessionKey);

        b.putString("cosRegion", reportInfo.cosRegion);
        b.putInt("vodErrCode", reportInfo.vodErrCode);
        b.putString("cosErrCode", reportInfo.cosErrCode);
        b.putInt("useHttpDNS", reportInfo.useHttpDNS);
        b.putInt("useCosAcc", reportInfo.useCosAcc);
        b.putLong("tcpConnTimeCost", reportInfo.tcpConnTimeCost);
        b.putLong("recvRespTimeCost", reportInfo.recvRespTimeCost);
        return b;
    }

    public void setAppId(int appId) {
        this.userAppId = appId;
    }

    class MyCosXmlResultListener implements CosXmlResultListener {

        private final long mFinalTcpConnectionTimeCost;
        private final long mFinalRecvRspTimeCost;

        public MyCosXmlResultListener(long finalTcpConnectionTimeCost, long finalRecvRspTimeCost) {
            this.mFinalTcpConnectionTimeCost = finalTcpConnectionTimeCost;
            this.mFinalRecvRspTimeCost = finalRecvRspTimeCost;
        }

        @Override
        public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
            String requestId = getRequestId(cosXmlResult);
            // Clear the local cache of breakpoint continuation information after the slice upload is completed
            setResumeData(uploadInfo.getFilePath(), "", "");
            txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, 0, 0,
                    "", "", reqTime, System.currentTimeMillis() - reqTime,
                    uploadInfo.getFileSize(), uploadInfo.getFileType(), uploadInfo.getFileName(),
                    "", requestId, mFinalTcpConnectionTimeCost, mFinalRecvRspTimeCost);
            TVCLog.i(TAG, "uploadCosVideo finish:  cosBucket " + cosBucket
                    + " cosVideoPath: " + cosVideoPath + "  path: "
                    + uploadInfo.getFilePath() + "  size: " + uploadInfo.getFileSize()
                    + " finalTcpConnectionTimeCost: " + mFinalTcpConnectionTimeCost + " finalRecvRspTimeCost: "
                    + mFinalRecvRspTimeCost);
            startUploadCoverFile(cosXmlResult);
        }

        @Override
        public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException,
                           CosXmlServiceException qcloudServiceException) {
            boolean isQuic = mCosXmlService.getConfig().isEnableQuic();
            if (qcloudException != null) {
                int errorReportCode = TVCConstants.ERR_UPLOAD_VIDEO_FAILED;
                TVCLog.w(TAG, "CosXmlClientException = " + qcloudException.getMessage());
                // Caused by network interruption
                if (!TVCUtils.isNetworkAvailable(context)) {
                    TVCLog.w(TAG, "network interruption");
                    setResumeData(uploadInfo.getFilePath(), vodSessionKey, uploadId);
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED,
                            "cos upload video error: network unreachable");
                } else if (isQuic) {
                    // QUIC failed, re-upload with HTTP
                    quicTransToHttpRetry();
                    errorReportCode = TVCConstants.ERR_UPLOAD_QUIC_FAILED;
                } else if (!cancelFlag) { // Other errors, not actively cancelled
                    TVCLog.w(TAG, "exception interruption");
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED,
                            "cos upload video error:" + qcloudException.getMessage());
                    setResumeData(uploadInfo.getFilePath(), "", "");
                } else {
                    TVCLog.i(TAG, "upload is cancel when ready to upload to cos");
                    busyFlag = false;
                    cancelFlag = false;
                    notifyUploadFailed(TVCConstants.ERR_USER_CANCEL, "request is cancelled by manual pause");
                }

                String errorMsg = "CosXmlClientException:" + qcloudException.getMessage();
                if (isQuic) {
                    errorMsg = "quic upload failed, retry on http, " + errorMsg;
                }

                reportCosUploadException(errorReportCode, String.valueOf(qcloudException.errorCode), errorMsg, "", 0,
                        0);
            }

            if (qcloudServiceException != null) {
                int errorReportCode = TVCConstants.ERR_UPLOAD_VIDEO_FAILED;
                final String cosErrorCode = qcloudServiceException.getErrorCode() == null ? "" :
                        qcloudServiceException.getErrorCode();
                TVCLog.w(TAG, "CosXmlServiceException =" + qcloudServiceException);
                // Temporary key expired, reapply for a temporary key without interrupting the upload
                if (qcloudServiceException.getErrorCode() != null
                        && qcloudServiceException.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                    TVCLog.w(TAG, "key expire,retry");
                    applyUploadUGC(uploadInfo, vodSessionKey);
                } else if (isQuic) {
                    // QUIC failed, re-upload with HTTP
                    quicTransToHttpRetry();
                    errorReportCode = TVCConstants.ERR_UPLOAD_QUIC_FAILED;
                } else {
                    notifyUploadFailed(TVCConstants.ERR_UPLOAD_VIDEO_FAILED,
                            "cos upload video error:" + qcloudServiceException.getMessage());
                    setResumeData(uploadInfo.getFilePath(), "", "");
                }

                String errorMsg = "CosXmlServiceException:" + qcloudServiceException.getMessage();
                if (isQuic) {
                    errorMsg = "quic upload failed, retry on http, " + errorMsg;
                }
                reportCosUploadException(errorReportCode, cosErrorCode, errorMsg,
                        qcloudServiceException.getRequestId(), mFinalTcpConnectionTimeCost, mFinalRecvRspTimeCost);
            }
        }

        private void reportCosUploadException(int errorCode, String cosErrorCode, String errorMsg, String requestId,
                                              long cosTcpConnTime, long cosRecRspTime) {
            txReport(TVCConstants.UPLOAD_EVENT_ID_COS_UPLOAD, errorCode, 0,
                    cosErrorCode, errorMsg, reqTime, System.currentTimeMillis() - reqTime, uploadInfo.getFileSize(),
                    uploadInfo.getFileType(), uploadInfo.getFileName(), "", requestId,
                    cosTcpConnTime, cosRecRspTime);
        }
    }
}
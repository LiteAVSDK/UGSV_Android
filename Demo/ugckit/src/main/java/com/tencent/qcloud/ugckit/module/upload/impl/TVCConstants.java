package com.tencent.qcloud.ugckit.module.upload.impl;

/**
 * Video Upload Constant Definition
 * 视频上传常量定义
 */
public class TVCConstants {
    public static final String TVCVERSION = "12.4.0.17372";
    public static       String VOD_SERVER_HOST     = "vod2.qcloud.com";
    public static       String VOD_SERVER_HOST_BAK = "vod2.dnsv1.com";

    // Maximum Retry Times
    public static int MAX_REQUEST_COUNT = 2;

    // Network Type
    public static final int NETTYPE_NONE = 0;
    public static final int NETTYPE_WIFI = 1;
    public static final int NETTYPE_4G   = 2;
    public static final int NETTYPE_3G   = 3;
    public static final int NETTYPE_2G   = 4;

    /**
     * quic total timeout
     */
    public static final long PRE_UPLOAD_QUIC_DETECT_TIMEOUT = 2000;
    /**
     *  for connect,read and write
     */
    public static final long PRE_UPLOAD_HTTP_DETECT_COMMON_TIMEOUT = 2000;
    public static final long PRE_UPLOAD_TIMEOUT = 3000;
    public static final long PRE_UPLOAD_ANA_DNS_TIME_OUT = 2000;
    public static final int UPLOAD_TIME_OUT_SEC = 120;
    public static final int UPLOAD_CONNECT_TIME_OUT_MILL = 5000;


    /************************************************ Client Error Code **********************************/
    /**
     * Success
     * 成功
     */
    public static final int NO_ERROR = 0;

    /**
     * Failure
     * 失败
     */
    public static final int ERROR = 1;

    /**
     * UGC Upload Request Failed
     * UGC请求上传失败
     */
    public static final int ERR_UGC_REQUEST_FAILED = 1001;

    /**
     * UGC Request Information Parsing Failed
     * UGC请求信息解析失败
     */
    public static final int ERR_UGC_PARSE_FAILED = 1002;

    /**
     * COS Video Upload Failed
     * COS上传视频失败
     */
    public static final int ERR_UPLOAD_VIDEO_FAILED = 1003;

    /**
     * COS Cover Upload Failed
     * COS上传封面失败
     */
    public static final int ERR_UPLOAD_COVER_FAILED = 1004;

    /**
     * UGC End Upload Request Failed
     * UGC结束上传请求失败
     */
    public static final int ERR_UGC_FINISH_REQUEST_FAILED = 1005;

    /**
     * UGC End Upload Response Error
     * UGC结束上传响应错误
     */
    public static final int ERR_UGC_FINISH_RESPONSE_FAILED = 1006;

    /**
     * Client is Busy (Object Cannot Handle More Requests)
     * 客户端正忙(对象无法处理更多请求)
     */
    public static final int ERR_CLIENT_BUSY = 1007;

    public static final int ERR_FILE_NOEXIT = 1008;

    /**
     * Video is Uploading
     * 视频正在上传中
     */
    public static final int ERR_UGC_PUBLISHING = 1009;

    public static final int ERR_UGC_INVALID_PARAM = 1010;

    /**
     * Video Upload SecretID Error, Deprecated, Will Not Throw
     * 视频上传secretID错误，已经废弃，不会抛出
     */
    public static final int ERR_UGC_INVALID_SECRETID = 1011;

    /**
     * Video Upload Signature Error
     * 视频上传signature错误
     */
    public static final int ERR_UGC_INVALID_SIGNATURE = 1012;

    /**
     * Video File Path Error
     * 视频文件的路径错误
     */
    public static final int ERR_UGC_INVALID_VIDOPATH   = 1013;

    /**
     * Video File Does Not Exist at Current Path
     * 当前路径下视频文件不存在
     */
    public static final int ERR_UGC_INVALID_VIDEO_FILE = 1014;

    /**
     * Video Upload Filename Too Long or Contains Special Characters
     * 视频上传文件名太长或含有特殊字符
     */
    public static final int ERR_UGC_FILE_NAME = 1015;

    /**
     * Video File Cover Path Incorrect
     * 视频文件封面路径不对
     */
    public static final int ERR_UGC_INVALID_COVER_PATH = 1016;

    /**
     * User Cancelled Operation
     * 用户取消操作
     */
    public static final int ERR_USER_CANCEL = 1017;

    /**
     * [Deprecated] Direct Upload Failed
     * [已废弃]直接上传失败
     */
    public static final int ERR_UPLOAD_VOD = 1018;

    /**
     * COS Failed to Upload Video Using QUIC, Switch to HTTP Upload
     * COS使用quic上传视频失败，转http上传
     */
    public static final int ERR_UPLOAD_QUIC_FAILED = 1019;

    /**
     * Signature Expired
     * 签名过期
     */
    public static final int ERR_UPLOAD_SIGN_EXPIRED = 1020;


    /************************************************ Data Reporting Definition
     * **********************************/
    public static int UPLOAD_EVENT_ID_REQUEST_UPLOAD                = 10001;  // UGC Request Upload
    public static int UPLOAD_EVENT_ID_COS_UPLOAD                    = 20001;  // UGC Calls COS Upload
    public static int UPLOAD_EVENT_ID_UPLOAD_RESULT                 = 10002;  // UGC Ends Upload
    public static int UPLOAD_EVENT_ID_VOD_UPLOAD                    = 30001;  // Direct Upload to VOD
    public static int UPLOAD_EVENT_DAU                              = 40001;  // Short Video Upload DAU Reporting
    public static int UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT        = 11001;  // VOD HTTP DNS Request Result
    public static int UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT = 11002;  // PrepareUploadUGC Request Result
    public static int UPLOAD_EVENT_ID_DETECT_DOMAIN_RESULT          = 11003;
    // Best Park Detection Result (Including COS IP List)
}

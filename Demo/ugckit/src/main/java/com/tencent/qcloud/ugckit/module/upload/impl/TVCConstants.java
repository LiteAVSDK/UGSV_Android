package com.tencent.qcloud.ugckit.module.upload.impl;

/**
 * 视频上传常量定义
 */
public class TVCConstants {
    public static final String TVCVERSION = "11.4.0.13189";
    public static       String VOD_SERVER_HOST     = "vod2.qcloud.com";
    public static       String VOD_SERVER_HOST_BAK = "vod2.dnsv1.com";

    // 最大重试次数
    public static int MAX_REQUEST_COUNT = 2;

    //网络类型
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


    /************************************************ 客户端错误码 **********************************/
    /**
     * 成功
     */
    public static final int NO_ERROR = 0;

    /**
     * 失败
     */
    public static final int ERROR = 1;

    /**
     * UGC请求上传失败
     */
    public static final int ERR_UGC_REQUEST_FAILED = 1001;

    /**
     * UGC请求信息解析失败
     */
    public static final int ERR_UGC_PARSE_FAILED = 1002;

    /**
     * COS上传视频失败
     */
    public static final int ERR_UPLOAD_VIDEO_FAILED = 1003;

    /**
     * COS上传封面失败
     */
    public static final int ERR_UPLOAD_COVER_FAILED = 1004;

    /**
     * UGC结束上传请求失败
     */
    public static final int ERR_UGC_FINISH_REQUEST_FAILED = 1005;

    /**
     * UGC结束上传响应错误
     */
    public static final int ERR_UGC_FINISH_RESPONSE_FAILED = 1006;

    /**
     * 客户端正忙(对象无法处理更多请求)
     */
    public static final int ERR_CLIENT_BUSY = 1007;

    public static final int ERR_FILE_NOEXIT = 1008;

    /**
     * 视频正在上传中
     */
    public static final int ERR_UGC_PUBLISHING = 1009;

    public static final int ERR_UGC_INVALID_PARAM = 1010;

    /**
     * 视频上传secretID错误，已经废弃，不会抛出
     */
    public static final int ERR_UGC_INVALID_SECRETID = 1011;

    /**
     * 视频上传signature错误
     */
    public static final int ERR_UGC_INVALID_SIGNATURE = 1012;

    /**
     * 视频文件的路径错误
     */
    public static final int ERR_UGC_INVALID_VIDOPATH   = 1013;
    /**
     * 当前路径下视频文件不存在
     */
    public static final int ERR_UGC_INVALID_VIDEO_FILE = 1014;

    /**
     * 视频上传文件名太长或含有特殊字符
     */
    public static final int ERR_UGC_FILE_NAME = 1015;

    /**
     * 视频文件封面路径不对
     */
    public static final int ERR_UGC_INVALID_COVER_PATH = 1016;

    /**
     * 用户取消操作
     */
    public static final int ERR_USER_CANCEL = 1017;

    /**
     * [已废弃]直接上传失败
     */
    public static final int ERR_UPLOAD_VOD = 1018;

    /**
     * COS使用quic上传视频失败，转http上传
     */
    public static final int ERR_UPLOAD_QUIC_FAILED = 1019;

    /**
     * 签名过期
     */
    public static final int ERR_UPLOAD_SIGN_EXPIRED = 1020;


    /************************************************ 数据上报定义 **********************************/
    public static int UPLOAD_EVENT_ID_REQUEST_UPLOAD                = 10001;  //UGC请求上传
    public static int UPLOAD_EVENT_ID_COS_UPLOAD                    = 20001;  //UGC调用cos上传
    public static int UPLOAD_EVENT_ID_UPLOAD_RESULT                 = 10002;  //UGC结束上传
    public static int UPLOAD_EVENT_ID_VOD_UPLOAD                    = 30001;  //直接上传到vod
    public static int UPLOAD_EVENT_DAU                              = 40001;  //短视频上传DAU上报
    public static int UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT        = 11001;          //vod http dns请求结果
    public static int UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT = 11002;          //PrepareUploadUGC请求结果
    public static int UPLOAD_EVENT_ID_DETECT_DOMAIN_RESULT          = 11003;          //检测最优园区结果(包含cos iplist)
}

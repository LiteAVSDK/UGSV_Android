package com.tencent.xmagic.download;

public class DownloadErrorCode {
    public static final int NONE = 0;
    public static final int NETWORK_ERROR = -1;
    public static final int NETWORK_FILE_ERROR = -2;
    public static final int FILE_IO_ERROR = -3;
    public static final int UNZIP_FAIL = -4;
    public static final int MD5_FAIL = -5;

    public static String getErrorMsg(int errorCode) {
        switch (errorCode) {
            case NONE:
                return "success";
            case NETWORK_ERROR:
                return "network error";
            case NETWORK_FILE_ERROR:
                return "network file error";
            case FILE_IO_ERROR:
                return "file io error";
            case UNZIP_FAIL:
                return "unzip error";
            case MD5_FAIL:
                return "md5 error";
            default:
                return "other error";
        }
    }
}

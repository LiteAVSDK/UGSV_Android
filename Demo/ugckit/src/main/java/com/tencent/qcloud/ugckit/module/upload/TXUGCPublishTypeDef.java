package com.tencent.qcloud.ugckit.module.upload;

import com.tencent.qcloud.ugckit.module.upload.impl.IUploadResumeController;

/**
 * Created by yuejiaoli on 2017/7/19.
 */

public class TXUGCPublishTypeDef {
    /**
     * 短视频发布参数定义
     * secretId:腾讯云存储cos服务密钥ID，已经废弃，不用填写
     * signature:signature
     * videoPath:视频路径
     * coverPath：自定义封面
     * resumeUpload：是否启动断点续传，默认开启
     * fileName：视频名称
     * enablePreparePublish: 是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
     */
    public final static class TXPublishParam {
        public String  secretId;                                                //腾讯云存储cos服务密钥ID，已经废弃，不用填写
        public String  signature;                                               //signature
        public String  videoPath;                                              //视频地址，支持uri
        public String  coverPath;                                              //封面
        public boolean enableResume         = true;                                    //是否启动断点续传，默认开启
        public boolean enableHttps          = false;                                   //上传是否使用https。默认关闭，走http
        public String  fileName;                                               //视频名称
        public boolean enablePreparePublish = true;                           //是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
        public long    sliceSize            = 0;                               // 分片大小,支持最小为1M,最大10M，默认0，代表上传文件大小除以10
        public int     concurrentCount      = -1;                              // 分片上传并发数量，<=0 则表示SDK内部默认为2个
        public IUploadResumeController uploadResumeController;                // 续点控制
    }

    /**
     * 媒体内容发布参数定义
     * signature   ：signature
     * mediaPath   ：媒体地址
     * enableResume ：是否启动断点续传，默认开启
     * enableHttps  ：上传是否使用https。默认关闭，走http
     * fileName   ：媒体名称
     * enablePreparePublish: 是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
     */
    public final static class TXMediaPublishParam {
        public String  signature;                                              //signature
        public String  mediaPath;                                              //媒体地址
        public boolean enableResume         = true;                                    //是否启动断点续传，默认开启
        public boolean enableHttps          = false;                                   //上传是否使用https。默认关闭，走http
        public String  fileName;                                               //媒体名称
        public boolean enablePreparePublish = true;                           //是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
        public long    sliceSize            = 0;                               // 分片大小,支持最小为1M,最大10M，默认0，代表上传文件大小除以10
        public int     concurrentCount      = -1;                              // 分片上传并发数量，<=0 则表示SDK内部默认为2个
        public IUploadResumeController uploadResumeController;                // 续点控制
    }

    /**
     * 短视频/媒体发布结果错误码定义，短视频发布流程分为三步
     * step1: 请求上传文件
     * step2: 上传文件
     * step3: 请求发布短视频/媒体
     */
    public static final int PUBLISH_RESULT_OK                    = 0;        //发布成功
    public static final int PUBLISH_RESULT_PUBLISH_PREPARE_ERROR = 1000;     //step0: 准备发布失败
    public static final int PUBLISH_RESULT_UPLOAD_REQUEST_FAILED = 1001;     //step1: “短视频/媒体”发送失败
    public static final int PUBLISH_RESULT_UPLOAD_RESPONSE_ERROR = 1002;     //step1: “短视频/媒体上传请求”收到错误响应

    public static final int PUBLISH_RESULT_UPLOAD_VIDEO_FAILED = 1003;     //step2: “视频文件”上传失败
    public static final int PUBLISH_RESULT_UPLOAD_MEDIA_FAILED = 1003;     //step2: “媒体文件”上传失败
    // 这里媒体文件上传code和视频文件上传code失败是一致的

    public static final int PUBLISH_RESULT_UPLOAD_COVER_FAILED    = 1004;     //step2: “封面文件”上传失败
    public static final int PUBLISH_RESULT_PUBLISH_REQUEST_FAILED = 1005;     //step3: “短视频/媒体发布请求”发送失败
    public static final int PUBLISH_RESULT_PUBLISH_RESPONSE_ERROR = 1006;     //step3: “短视频/媒体发布请求”收到错误响应

    /**
     * 短视频发布结果定义
     */
    public final static class TXPublishResult {
        public int    retCode;                                                  //错误码
        public String descMsg;                                                  //错误描述信息
        public String videoId;                                                  //视频文件Id
        public String videoURL;                                                 //视频播放地址
        public String coverURL;                                                 //封面存储地址

    }

    ;

    /**
     * 媒体内容发布结果定义
     */
    public final static class TXMediaPublishResult {
        public int    retCode;                                                  //错误码
        public String descMsg;                                                  //错误描述信息
        public String mediaId;                                                  //媒体文件Id
        public String mediaURL;                                                 //媒体地址
    }

    ;

    /**
     * 短视频发布回调定义
     */
    public interface ITXVideoPublishListener {
        /**
         * 短视频发布进度
         */
        void onPublishProgress(long uploadBytes, long totalBytes);

        /**
         * 短视频发布完成
         */
        void onPublishComplete(TXPublishResult result);
    }

    /**
     * 媒体发布回调定义
     */
    public interface ITXMediaPublishListener {

        /**
         * 媒体内容发布进度
         */
        void onMediaPublishProgress(long uploadBytes, long totalBytes);

        /**
         * 媒体内容发布完成
         */
        void onMediaPublishComplete(TXMediaPublishResult mediaResult);
    }

    public static class TXPublishResumeData {
        public long   sliceSize;
        public String bucket;
        public String cosPath;
        public String srcPath;
        public String uploadId;
        public String customerKeyForSSEC;
        public String customerKeyIdForSSEKMS;
        public String jsonContentForSSEKMS;
    }
}

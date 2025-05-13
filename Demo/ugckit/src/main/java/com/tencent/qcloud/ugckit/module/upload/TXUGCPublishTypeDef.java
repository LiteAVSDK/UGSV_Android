package com.tencent.qcloud.ugckit.module.upload;

import com.tencent.qcloud.ugckit.module.upload.impl.IUploadResumeController;

/**
 * Created by yuejiaoli on 2017/7/19.
 */

public class TXUGCPublishTypeDef {

    /**
     * Definition of short video publishing parameters
     * 短视频发布参数定义
     */
    public static class TXPublishParam {
        /**
         * Tencent Cloud Storage COS service key ID, which has been deprecated, does not need to be filled in
         * 腾讯云存储cos服务密钥ID，已经废弃，不用填写
         */
        public String  secretId;
        /**
         * signature
         */
        public String  signature;
        /**
         * Video URL, which supports Uri
         * 视频地址，支持uri
         */
        public String  videoPath;
        /**
         * Video cover
         * 封面
         */
        public String  coverPath;
        /**
         * Whether to enable breakpoint continuation, which is disabled by default
         * 是否启动断点续传，默认开启
         */
        public boolean enableResume         = true;
        /**
         * Whether to use HTTPS for uploading, which is disabled by default
         * 上传是否使用https，默认关闭
         */
        public boolean enableHttps          = false;
        /**
         * Video name
         * 视频名称
         */
        public String  fileName;
        /**
         * Whether to enable the pre-upload mechanism, which is enabled by default.
         * Note: The pre-upload mechanism can significantly improve the upload quality of files.
         * 是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
         */
        public boolean enablePreparePublish = true;
        /**
         * Slice size, which supports a minimum of 1M and a maximum of 10M, and is defaulted to 0,
         * representing the file size uploaded divided by 10
         * 分片大小,支持最小为1M,最大10M，默认0，代表上传文件大小除以10
         */
        public long    sliceSize            = 0;
        /**
         * Maximum number of concurrent slices for upload, which should be less than or equal to 0,
         * indicating that the SDK defaults to 4 internally
         * 分片上传最大并发数量，<=0 则表示SDK内部默认为4个
         */
        public int     concurrentCount      = -1;
        /**
         * The speed limit value setting range is 819200~838860800, which is 100KB/s~100MB/s.
         * If it exceeds this range, a 400 error will be returned. It is not recommended to set this value
         * too small to prevent timeouts. -1 indicates no speed limit.
         * 限速值设置范围为819200~838860800，即100KB/s~100MB/s，如果超出该范围会返回400错误。不建议将该值设置太小，防止超时。-1 表示不限速.
         */
        public long trafficLimit = -1;
        /**
         * Breakpoint control
         * 续点控制
         */
        public IUploadResumeController uploadResumeController;
    }

    /**
     * Definition of media content publishing parameters
     * 媒体内容发布参数定义
     */
    public static class TXMediaPublishParam {
        /**
         * signature
         */
        public String  signature;
        /**
         * Media URL, which supports Uri
         * 媒体地址，支持uri
         */
        public String  mediaPath;
        /**
         * Whether to enable breakpoint continuation, which is disabled by default
         * 是否启动断点续传，默认开启
         */
        public boolean enableResume         = true;
        /**
         * Whether to use HTTPS for uploading, which is disabled by default
         * 上传是否使用https，默认关闭
         */
        public boolean enableHttps          = false;
        /**
         * Media name
         * 媒体名称
         */
        public String  fileName;
        /**
         * Whether to enable the pre-upload mechanism, which is enabled by default.
         * Note: The pre-upload mechanism can significantly improve the upload quality of files.
         * 是否开启预上传机制，默认开启，备注：预上传机制可以大幅提升文件的上传质量
         */
        public boolean enablePreparePublish = true;
        /**
         * Slice size, which supports a minimum of 1M and a maximum of 10M, and is defaulted to 0,
         * representing the file size uploaded divided by 10
         * 分片大小,支持最小为1M,最大10M，默认0，代表上传文件大小除以10
         */
        public long    sliceSize            = 0;
        /**
         * Maximum number of concurrent slices for upload, which should be less than or equal to 0,
         * indicating that the SDK defaults to 4 internally
         * 分片上传最大并发数量，<=0 则表示SDK内部默认为4个
         */
        public int     concurrentCount      = -1;
        /**
         * The speed limit value setting range is 819200~838860800, which is 100KB/s~100MB/s.
         * If it exceeds this range, a 400 error will be returned. It is not recommended to set this value
         * too small to prevent timeouts. -1 indicates no speed limit.
         * 限速值设置范围为819200~838860800，即100KB/s~100MB/s，如果超出该范围会返回400错误。不建议将该值设置太小，防止超时。-1 表示不限速.
         */
        public long trafficLimit = -1;
        /**
         * Breakpoint control
         * 续点控制
         */
        public IUploadResumeController uploadResumeController;
    }

    /**
     * Definition of short video/media publishing result error codes.
     * The short video publishing process consists of three steps:
     * Step 1: Request to upload the file
     * Step 2: Upload the file
     * Step 3: Request to publish the short video/media
     */
    // Publish successful
    public static final int PUBLISH_RESULT_OK                    = 0;
    // step0: Preparing for publishing failed
    public static final int PUBLISH_RESULT_PUBLISH_PREPARE_ERROR = 1000;
    // step1: "Short video/media" sending failed
    public static final int PUBLISH_RESULT_UPLOAD_REQUEST_FAILED = 1001;
    // step1: "Short video/media upload request" received error response
    public static final int PUBLISH_RESULT_UPLOAD_RESPONSE_ERROR = 1002;
    // step2: "Video file" upload failed
    public static final int PUBLISH_RESULT_UPLOAD_VIDEO_FAILED = 1003;
    // step2: "Media file" upload failed
    public static final int PUBLISH_RESULT_UPLOAD_MEDIA_FAILED = 1003;
    // step2: "Cover file" upload failed
    public static final int PUBLISH_RESULT_UPLOAD_COVER_FAILED    = 1004;
    // step3: "Short video/media publishing request" sending failed
    public static final int PUBLISH_RESULT_PUBLISH_REQUEST_FAILED = 1005;
    // step3: "Short video/media publishing request" received error response
    public static final int PUBLISH_RESULT_PUBLISH_RESPONSE_ERROR = 1006;

    /**
     * Short Video Publishing Result Definition
     * 短视频发布结果定义
     */
    public final static class TXPublishResult {
        // Error Code
        public int    retCode;
        // Error Description Information
        public String descMsg;
        // Video File Id
        public String videoId;
        // Video Playback Address
        public String videoURL;
        // Cover Storage Address
        public String coverURL;

    }

    ;

    /**
     * Media Content Publishing Result Definition
     * 媒体内容发布结果定义
     */
    public static class TXMediaPublishResult {
        // Error Code
        public int    retCode;
        // Error Description Information
        public String descMsg;
        // Media File Id
        public String mediaId;
        // Media Address
        public String mediaURL;
    }

    ;

    /**
     * Short Video Publishing Callback Definition
     * 短视频发布回调定义
     */
    public interface ITXVideoPublishListener {
        /**
         * Short Video Publishing Progress
         * 短视频发布进度
         */
        void onPublishProgress(long uploadBytes, long totalBytes);

        /**
         * Short Video Publishing Completion
         * 短视频发布完成
         */
        void onPublishComplete(TXPublishResult result);
    }

    /**
     * Media Publishing Callback Definition
     * 媒体发布回调定义
     */
    public interface ITXMediaPublishListener {

        /**
         * Media Content Publishing Progress
         * 媒体内容发布进度
         */
        void onMediaPublishProgress(long uploadBytes, long totalBytes);

        /**
         * Media Content Publishing Completion
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

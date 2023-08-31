package com.tencent.qcloud.ugckit.module.upload;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.module.upload.impl.TVCClient;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCConfig;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCConstants;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCLog;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCUploadInfo;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCUploadListener;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCUtils;
import com.tencent.qcloud.ugckit.module.upload.impl.TXUGCPublishOptCenter;

import java.io.File;
import java.io.FileOutputStream;


/**
 * 短视频发布接口类
 */
public class TXUGCPublish {
    private static final String TAG = "TXVideoPublish";
    private static final long COVER_TIME = 500 * 1000;
    private Context mContext;
    private Handler mHandler;
    private TXUGCPublishTypeDef.ITXVideoPublishListener mListener;
    private TXUGCPublishTypeDef.ITXMediaPublishListener mMediaListener;
    private boolean mPublishing;
    private TVCClient mTVCClient = null;
    private String mCustomKey = "";
    private boolean mIsDebug = true;
    private boolean mIsCancel = false;

    public TXUGCPublish(Context context, String customKey) {
        mCustomKey = customKey;
        if (context != null) {
            mContext = context;
            mHandler = new Handler(mContext.getMainLooper());
            setIsDebug(true);
        }
    }

    public TXUGCPublish(Context context) {
        this(context, "");
    }

    public void setListener(TXUGCPublishTypeDef.ITXVideoPublishListener listener) {
        mListener = listener;
    }

    public void setListener(TXUGCPublishTypeDef.ITXMediaPublishListener listener) {
        mMediaListener = listener;
    }

    /**
     * 设置是否打印日志
     */
    public void setIsDebug(boolean isDebug) {
        mIsDebug = isDebug;
        TVCLog.setDebuggable(isDebug, mContext);
    }

    private int publishVideoImpl(TXUGCPublishTypeDef.TXPublishParam param) {
        if (TextUtils.isEmpty(param.videoPath)) {
            TVCLog.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        boolean bVideoFileExist = TVCUtils.isExistsForPathOrUri(mContext, param.videoPath);

        if (!bVideoFileExist) {
            TVCLog.e(TAG, "publishVideo invalid video file");
            return TVCConstants.ERR_UGC_INVALID_VIDEO_FILE;
        }

        String coverPath = "";
        if (!TextUtils.isEmpty(param.coverPath)) {
            coverPath = param.coverPath;
            File file = new File(coverPath);
            if (!file.exists()) {
                return TVCConstants.ERR_UGC_INVALID_COVER_PATH;
            }
        }

        TVCConfig tvcConfig = new TVCConfig();
        tvcConfig.mCustomKey = mCustomKey;
        tvcConfig.mSignature = param.signature;
        tvcConfig.mEnableResume = param.enableResume;
        tvcConfig.mEnableHttps = param.enableHttps;
        tvcConfig.mVodReqTimeOutInSec = 10;
        tvcConfig.mSliceSize = param.sliceSize;
        tvcConfig.mConcurrentCount = param.concurrentCount;
        tvcConfig.mUploadResumeController = param.uploadResumeController;
        tvcConfig.mIsDebuggable = mIsDebug;

        if (mTVCClient == null) {
            mTVCClient = new TVCClient(mContext, tvcConfig);
        } else {
            mTVCClient.updateConfig(tvcConfig);
        }

        final TVCUploadInfo info = new TVCUploadInfo(getFileType(param.videoPath), param.videoPath,
                getFileType(coverPath),
                coverPath, param.fileName);

        if (info.getFileSize() == 0) {
            TVCLog.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        final long upLoadStartTime = System.currentTimeMillis();
        int ret = mTVCClient.uploadVideo(info, new TVCUploadListener() {
            @Override
            public void onSuccess(final String fileId, final String playUrl, final String coverUrl) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                TXUGCPublishTypeDef.TXPublishResult result = new TXUGCPublishTypeDef.TXPublishResult();
                                result.retCode = TXUGCPublishTypeDef.PUBLISH_RESULT_OK;
                                result.descMsg = "publish success";
                                result.videoId = fileId;
                                result.videoURL = playUrl;
                                result.coverURL = coverUrl;
                                mListener.onPublishComplete(result);
                            }
                            TVCLog.i(TAG, "upload cost Time:" + (System.currentTimeMillis() - upLoadStartTime));
                        }
                    });
                }
                mTVCClient = null;
                mPublishing = false;
            }

            @Override
            public void onFailed(final int errCode, final String errMsg) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                TXUGCPublishTypeDef.TXPublishResult result = new TXUGCPublishTypeDef.TXPublishResult();
                                result.retCode = errCode;
                                result.descMsg = errMsg;
                                mListener.onPublishComplete(result);
                            }
                        }
                    });
                }
                mTVCClient = null;
                mPublishing = false;
            }

            @Override
            public void onProgress(final long currentSize, final long totalSize) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onPublishProgress(currentSize, totalSize);
                            }
                        }
                    });
                }
                mPublishing = false;
            }
        });
        return ret;
    }

    /**
     * 上传视频文件 （视频文件 + 封面图）
     *
     * @param param
     * @return
     */
    public int publishVideo(final TXUGCPublishTypeDef.TXPublishParam param) {
        if (mPublishing) {
            TVCLog.e(TAG, "there is existing publish task");
            return TVCConstants.ERR_UGC_PUBLISHING;
        }

        if (param == null) {
            TVCLog.e(TAG, "publishVideo invalid param");
            return TVCConstants.ERR_UGC_INVALID_PARAM;
        }
        if (TextUtils.isEmpty(param.signature)) {
            TVCLog.e(TAG, "publishVideo invalid UGCSignature");
            return TVCConstants.ERR_UGC_INVALID_SIGNATURE;
        }
        // 正在发布，包含预上传
        mPublishing = true;
        mIsCancel = false;
        if (param.enablePreparePublish) {
            // 启动预发布初始化，预发布后再开始上传
            TXUGCPublishOptCenter.getInstance().prepareUpload(mContext, param.signature,
                    new TXUGCPublishOptCenter.IPrepareUploadCallback() {
                        @Override
                        public void onFinish() {
                            if (mIsCancel) {
                                mIsCancel = false;
                                TVCLog.i(TAG,"upload is cancel after prepare upload");
                                TXUGCPublishTypeDef.TXPublishResult result = new TXUGCPublishTypeDef.TXPublishResult();
                                result.retCode = TVCConstants.ERR_USER_CANCEL;
                                result.descMsg = "request is cancelled by manual pause";
                                mListener.onPublishComplete(result);
                                return;
                            }
                            int ret = publishVideoImpl(param);
                            mPublishing = (ret == TVCConstants.NO_ERROR);
                        }
                    });
            return TVCConstants.NO_ERROR;
        } else {
            TXUGCPublishOptCenter.getInstance().prepareUpload(mContext, param.signature, null);
            int ret = publishVideoImpl(param);
            mPublishing = (ret == TVCConstants.NO_ERROR);
            return ret;
        }
    }

    private int publishMediaImpl(TXUGCPublishTypeDef.TXMediaPublishParam param) {
        if (TextUtils.isEmpty(param.mediaPath)) {
            TVCLog.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        boolean bVideoFileExist = false;
        try {
            File file = new File(param.mediaPath);
            bVideoFileExist = file.isFile() && file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!bVideoFileExist) {
            TVCLog.e(TAG, "publishVideo invalid video file");
            return TVCConstants.ERR_UGC_INVALID_VIDEO_FILE;
        }

        TVCConfig tvcConfig = new TVCConfig();
        tvcConfig.mCustomKey = mCustomKey;
        tvcConfig.mSignature = param.signature;
        tvcConfig.mEnableResume = param.enableResume;
        tvcConfig.mEnableHttps = param.enableHttps;
        tvcConfig.mVodReqTimeOutInSec = 10;
        tvcConfig.mSliceSize = param.sliceSize;
        tvcConfig.mConcurrentCount = param.concurrentCount;
        tvcConfig.mUploadResumeController = param.uploadResumeController;
        tvcConfig.mIsDebuggable = mIsDebug;

        if (mTVCClient == null) {
            mTVCClient = new TVCClient(mContext, tvcConfig);
        } else {
            mTVCClient.updateConfig(tvcConfig);
        }

        TVCUploadInfo info = new TVCUploadInfo(getFileType(param.mediaPath), param.mediaPath, null, null,
                param.fileName);

        if (info.getFileSize() == 0) {
            TVCLog.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        final long upLoadStartTime = System.currentTimeMillis();
        int ret = mTVCClient.uploadVideo(info, new TVCUploadListener() {
            @Override
            public void onSuccess(final String fileId, final String playUrl, final String coverUrl) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaListener != null) {
                                TXUGCPublishTypeDef.TXMediaPublishResult result =
                                        new TXUGCPublishTypeDef.TXMediaPublishResult();
                                result.retCode = TXUGCPublishTypeDef.PUBLISH_RESULT_OK;
                                result.descMsg = "publish success";
                                result.mediaId = fileId;
                                result.mediaURL = playUrl;
                                mMediaListener.onMediaPublishComplete(result);
                            }
                            TVCLog.i(TAG, "upload cost Time:" + (System.currentTimeMillis() - upLoadStartTime));
                        }
                    });
                }
                mTVCClient = null;
                mPublishing = false;
            }

            @Override
            public void onFailed(final int errCode, final String errMsg) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaListener != null) {
                                TXUGCPublishTypeDef.TXMediaPublishResult result =
                                        new TXUGCPublishTypeDef.TXMediaPublishResult();
                                result.retCode = errCode;
                                result.descMsg = errMsg;
                                mMediaListener.onMediaPublishComplete(result);
                            }
                        }
                    });
                }
                mTVCClient = null;
                mPublishing = false;
            }

            @Override
            public void onProgress(final long currentSize, final long totalSize) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaListener != null) {
                                mMediaListener.onMediaPublishProgress(currentSize, totalSize);
                            }
                        }
                    });
                }
                mPublishing = false;
            }
        });
        return ret;
    }

    /**
     * 上传媒体文件
     *
     * @param param
     * @return
     */
    public int publishMedia(final TXUGCPublishTypeDef.TXMediaPublishParam param) {
        if (mPublishing) {
            TVCLog.e(TAG, "there is existing publish task");
            return TVCConstants.ERR_UGC_PUBLISHING;
        }
        if (param == null) {
            TVCLog.e(TAG, "publishVideo invalid param");
            return TVCConstants.ERR_UGC_INVALID_PARAM;
        }
        if (TextUtils.isEmpty(param.signature)) {
            TVCLog.e(TAG, "publishVideo invalid UGCSignature");
            return TVCConstants.ERR_UGC_INVALID_SIGNATURE;
        }
        mPublishing = true;
        mIsCancel = false;
        if (param.enablePreparePublish) {
            TXUGCPublishOptCenter.getInstance().prepareUpload(mContext, param.signature,
                    new TXUGCPublishOptCenter.IPrepareUploadCallback() {
                        @Override
                        public void onFinish() {
                            if (mIsCancel) {
                                mIsCancel = false;
                                TVCLog.i(TAG,"upload is cancel after prepare upload");
                                TXUGCPublishTypeDef.TXPublishResult result = new TXUGCPublishTypeDef.TXPublishResult();
                                result.retCode = TVCConstants.ERR_USER_CANCEL;
                                result.descMsg = "request is cancelled by manual pause";
                                mListener.onPublishComplete(result);
                                return;
                            }
                            int ret = publishMediaImpl(param);
                            mPublishing = (ret == TVCConstants.NO_ERROR);
                        }
                    });
            return TVCConstants.NO_ERROR;
        } else {
            TXUGCPublishOptCenter.getInstance().prepareUpload(mContext, param.signature, null);
            int ret = publishMediaImpl(param);
            mPublishing = (ret == TVCConstants.NO_ERROR);
            return ret;
        }
    }

    /**
     * 设置点播appId
     * 作用是方便定位上传过程中出现的问题
     */
    public void setAppId(int appId) {
        if (mTVCClient != null) {
            mTVCClient.setAppId(appId);
        }
    }

    /**
     * 取消上传 （取消媒体/取消短视频发布）
     * 注意：取消的是未开始的分片。如果上传源文件太小，取消的时候已经没有分片还未触发上传，最终文件还是会上传完成
     */
    public void canclePublish() {
        if (mTVCClient != null) {
            mTVCClient.cancleUpload();
        }
        mPublishing = false;
    }

    /**
     * 获取上报信息
     *
     * @return
     */
    public Bundle getStatusInfo() {
        if (mTVCClient != null) {
            return mTVCClient.getStatusInfo();
        } else {
            return null;
        }
    }


    private String getFileType(String filePath) {
        String fileType = "";
        if (null == filePath || filePath.length() == 0) {
            return fileType;
        }
        fileType = getFileTypeBySuffix(filePath);
        if (TextUtils.isEmpty(fileType) && filePath.startsWith("content://")) {
            fileType = getFileTypeByUri(Uri.parse(filePath));
        }
        if (TextUtils.isEmpty(fileType)) {
            String absolutePath = TVCUtils.getAbsolutePath(mContext, filePath);
            fileType = getFileTypeBySuffix(absolutePath);
        }
        return fileType;
    }

    private String getFileTypeBySuffix(String filePath) {
        String fileType = "";
        if (null != filePath && filePath.length() != 0) {
            int index = filePath.lastIndexOf(".");
            if (index != -1) {
                fileType = filePath.substring(index + 1);
            }
        }
        return fileType;
    }

    private String getFileTypeByUri(Uri uri) {
        String fileType = "";
        if (null != uri) {
            fileType = mContext.getContentResolver().getType(uri);
            if (!TextUtils.isEmpty(fileType)) {
                int index = fileType.lastIndexOf("/");
                if (index != -1) {
                    fileType = fileType.substring(index + 1);
                }
            }
        }
        return fileType;
    }


    private String getVideoThumb(String videoPath) {
        String strCoverFilePath = null;
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                TVCLog.w(TAG, "record: video file is not exists when record finish");
                return null;
            }
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(videoPath);
            Bitmap thumb = media.getFrameAtTime(COVER_TIME);

            String fileName = "";
            int index = videoPath.lastIndexOf(".");
            if (index != -1) {
                fileName = videoPath.substring(0, index);
            }

            strCoverFilePath = fileName + ".jpg";
            File f = new File(strCoverFilePath);
            if (f.exists()) f.delete();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(f);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strCoverFilePath;
    }
}

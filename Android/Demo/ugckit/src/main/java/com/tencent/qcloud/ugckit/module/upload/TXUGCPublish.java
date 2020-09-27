package com.tencent.qcloud.ugckit.module.upload;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.qcloud.ugckit.module.upload.impl.TVCClient;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCConstants;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCUploadInfo;
import com.tencent.qcloud.ugckit.module.upload.impl.TVCUploadListener;

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

    public TXUGCPublish(Context context, String customKey) {
        mCustomKey = customKey;
        if (context != null) {
            mContext = context;
            mHandler = new Handler(mContext.getMainLooper());
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
     * 上传视频文件 （视频文件 + 封面图）
     *
     * @param param
     * @return
     */
    public int publishVideo(TXUGCPublishTypeDef.TXPublishParam param) {
        if (mPublishing) {
            Log.e(TAG, "there is existing publish task");
            return TVCConstants.ERR_UGC_PUBLISHING;
        }

        if (param == null) {
            Log.e(TAG, "publishVideo invalid param");
            return TVCConstants.ERR_UGC_INVALID_PARAM;
        }
        if (TextUtils.isEmpty(param.signature)) {
            Log.e(TAG, "publishVideo invalid UGCSignature");
            return TVCConstants.ERR_UGC_INVALID_SIGNATURE;
        }

        if (TextUtils.isEmpty(param.videoPath)) {
            Log.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        boolean bVideoFileExist = false;
        try {
            File file = new File(param.videoPath);
            bVideoFileExist = file.isFile() && file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bVideoFileExist == false) {
            //TXCLog.e(TAG, "publishVideo invalid video file");
            return TVCConstants.ERR_UGC_INVALID_VIDEO_FILE;
        }

        String coverPath = "";
        if (!TextUtils.isEmpty(param.coverPath)) {
            coverPath = param.coverPath;
            File file = new File(coverPath);
            if (!file.exists())
                return TVCConstants.ERR_UGC_INVALID_COVER_PATH;
        }

        if (mTVCClient == null) {
            mTVCClient = new TVCClient(mContext, mCustomKey, param.signature, param.enableResume, param.enableHttps, 10);
        } else {
            mTVCClient.updateSignature(param.signature);
        }

        TVCUploadInfo info = new TVCUploadInfo(getFileType(param.videoPath), param.videoPath, getFileType(coverPath), coverPath, param.fileName);
        int ret = mTVCClient.uploadVideo(info, new TVCUploadListener() {
            @Override
            public void onSucess(final String fileId, final String playUrl, final String coverUrl) {
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

        if (ret == TVCConstants.NO_ERROR)
            mPublishing = true;
        else
            mPublishing = false;
        return ret;
    }


    /**
     * 上传媒体文件
     *
     * @param param
     * @return
     */
    public int publishMedia(TXUGCPublishTypeDef.TXMediaPublishParam param) {
        if (mPublishing) {
            Log.e(TAG, "there is existing publish task");
            return TVCConstants.ERR_UGC_PUBLISHING;
        }

        if (param == null) {
            Log.e(TAG, "publishVideo invalid param");
            return TVCConstants.ERR_UGC_INVALID_PARAM;
        }
        if (TextUtils.isEmpty(param.signature)) {
            Log.e(TAG, "publishVideo invalid UGCSignature");
            return TVCConstants.ERR_UGC_INVALID_SIGNATURE;
        }

        if (TextUtils.isEmpty(param.mediaPath)) {
            Log.e(TAG, "publishVideo invalid videoPath");
            return TVCConstants.ERR_UGC_INVALID_VIDOPATH;
        }

        boolean bVideoFileExist = false;
        try {
            File file = new File(param.mediaPath);
            bVideoFileExist = file.isFile() && file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bVideoFileExist == false) {
            //TXCLog.e(TAG, "publishVideo invalid video file");
            return TVCConstants.ERR_UGC_INVALID_VIDEO_FILE;
        }

        if (mTVCClient == null) {
            mTVCClient = new TVCClient(mContext, mCustomKey, param.signature, param.enableResume, param.enableHttps, 10);
        } else {
            mTVCClient.updateSignature(param.signature);
        }

        TVCUploadInfo info = new TVCUploadInfo(getFileType(param.mediaPath), param.mediaPath, null, null, param.fileName);
        int ret = mTVCClient.uploadVideo(info, new TVCUploadListener() {
            @Override
            public void onSucess(final String fileId, final String playUrl, final String coverUrl) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaListener != null) {
                                TXUGCPublishTypeDef.TXMediaPublishResult result = new TXUGCPublishTypeDef.TXMediaPublishResult();
                                result.retCode = TXUGCPublishTypeDef.PUBLISH_RESULT_OK;
                                result.descMsg = "publish success";
                                result.mediaId = fileId;
                                result.mediaURL = playUrl;
                                mMediaListener.onMediaPublishComplete(result);
                            }
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
                                TXUGCPublishTypeDef.TXMediaPublishResult result = new TXUGCPublishTypeDef.TXMediaPublishResult();
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

        if (ret == TVCConstants.NO_ERROR)
            mPublishing = true;
        else
            mPublishing = false;
        return ret;
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
     *
     */
    public void canclePublish() {
        if (mTVCClient != null) {
            mTVCClient.cancleUpload();
        }
        mPublishing = false;
    }

    /**
     * 获取上报信息
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
        if (filePath != null && filePath.length() != 0) {
            int index = filePath.lastIndexOf(".");
            if (index != -1) {
                fileType = filePath.substring(index + 1);
            }
        }
        return fileType;
    }


    private String getVideoThumb(String videoPath) {
        String strCoverFilePath = null;
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                Log.w(TAG, "record: video file is not exists when record finish");
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

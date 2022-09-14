package com.tencent.qcloud.ugckit.utils;

import android.text.TextUtils;

import com.tencent.qcloud.ugckit.UGCKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ****************************************************************************
 * 版权声明：腾讯科技版权所有
 * Copyright(C)2008-2013 Tencent All Rights Reserved
 *
 * @author yonnielu
 * v 1.0.0
 * Create at 2013-08-06 5:20 PM
 * <p/>
 * *****************************************************************************
 */
public class HttpFileUtil extends HttpCommon {
    private String           mUrl;
    private String           mFolder;
    private String           mFilename;
    private long             mContentLength;
    private long             mDownloadingSize;
    private boolean          mNeedProgress;
    private HttpFileListener mListener;

    public HttpFileUtil(String url, String folder, String filename, HttpFileListener listener, boolean needProgress) {
        mUrl = url;
        mFolder = folder;
        mFilename = filename;
        mListener = listener;
        mNeedProgress = needProgress;
    }

    @Override
    public void run() {
        if (!VideoDeviceUtil.isNetworkAvailable(UGCKit.getAppContext()) ||
                TextUtils.isEmpty(mUrl) || TextUtils.isEmpty(mFolder) || TextUtils.isEmpty(mFilename) || !mUrl.startsWith("http")) {
            fail(null, 0);
            return;
        }
        File dstFolder = new File(mFolder);
        if (!dstFolder.exists()) {
            dstFolder.mkdirs();
        } else {
            if (dstFolder.isFile()) {
                if (mListener != null) {
                    mListener.onSaveFailed(dstFolder, null);
                    return;
                }
            }
        }
        File dstFile = new File(mFolder + File.separator + mFilename);
        HttpURLConnection client = null;
        InputStream responseIs = null;
        FileOutputStream fos = null;
        int statusCode = -1;
        boolean success = false;
        Exception failException = null;

        try {
            if (dstFile.exists()) {
                dstFile.delete();
            }
            dstFile.createNewFile();
            client = (HttpURLConnection) new URL(mUrl).openConnection();

            // 设置网络超时参数
            client.setConnectTimeout(TIMEOUT);
            client.setReadTimeout(TIMEOUT);
            client.setDoInput(true);
            client.setRequestMethod("GET");

            statusCode = client.getResponseCode();
            success = client.getResponseCode() == HttpURLConnection.HTTP_OK;

            if (success) {
                if (mNeedProgress) {
                    mContentLength = client.getContentLength();
                    if (!VideoDeviceUtil.isExternalStorageSpaceEnough(mContentLength)) {
                        if (mListener != null) {
                            mListener.onSaveFailed(dstFile, new Exception("存储空间不足"));
                        }
                        return;
                    }
                }
                responseIs = client.getInputStream();
                int length = -1;
                byte[] buffer = new byte[BUFFERED_READER_SIZE];
                fos = new FileOutputStream(dstFile);
                mDownloadingSize = 0;
                mListener.onProgressUpdate(0);
                while ((length = responseIs.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                    if (mNeedProgress) {
                        int pre = (int) (mDownloadingSize * 100 / mContentLength);
                        mDownloadingSize += length;
                        int now = (int) (mDownloadingSize * 100 / mContentLength);
                        if (pre != now && mListener != null) {
                            mListener.onProgressUpdate(now);
                        }
                    }
                }
                fos.flush();
                if (mListener != null) {
                    mListener.onProgressUpdate(100);
                    mListener.onSaveSuccess(dstFile);
                }
            } else {
                failException = new HttpStatusException("http status got exception. code = " + statusCode);
            }
        } catch (Exception e) {
            failException = e;
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (responseIs != null) {
                    responseIs.close();
                }
                if (client != null) {
                    client.disconnect();
                }
                mListener.onProcessEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!success || null != failException) {
            mListener.onSaveFailed(dstFile, failException);
        }
    }

    private void fail(Exception e, int statusCode) {
        if (mListener != null) {
            mListener.onSaveFailed(null, e);
        }
        mListener = null;
    }
}

package com.tencent.qcloud.ugckit.utils;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.UGCKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {
    private static DownloadUtil instance;
    private        OkHttpClient okHttpClient;

    public static DownloadUtil getInstance() {
        if (instance == null) {
            synchronized (DownloadUtil.class) {
                if (instance == null) {
                    instance = new DownloadUtil();
                }
            }
        }
        return instance;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    public void download(@NonNull final String url, @NonNull final String saveDir, @NonNull final DownloadListener downloadListener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadListener.onDownloadFailed();
            }

            @Override
            public void onResponse(Call call, @NonNull Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String saveFolder = isExistDir(saveDir);
                if (TextUtils.isEmpty(saveFolder)) {
                    return;
                }
                String tempPath = saveFolder + File.separator + "temp_" + getNameFromUrl(url);
                String savePath = saveFolder + File.separator + getNameFromUrl(url);
                try {
                    FileUtils.deleteFile(tempPath);
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(tempPath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        downloadListener.onDownloading(progress);
                    }
                    fos.flush();
                    // 下载完成
                    FileUtils.fileRename(tempPath, savePath);
                    downloadListener.onDownloadSuccess(savePath);
                } catch (Exception e) {
                    downloadListener.onDownloadFailed();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @NonNull
    private String isExistDir(@NonNull String saveDir) throws IOException {
        // 下载位置
        File sdcardDir = UGCKit.getAppContext().getExternalFilesDir(null);
        if (sdcardDir == null) {
            return null;
        }

        File downloadFile = new File(sdcardDir, saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    @NonNull
    public static String getNameFromUrl(@NonNull String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface DownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(String path);

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }
}

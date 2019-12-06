package com.tencent.qcloud.ugckit.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 封面工具类
 */
public class CoverUtil {

    private String TAG = "CoverUtil";
    @NonNull
    private static CoverUtil instance = new CoverUtil();

    private CoverUtil() {
    }

    @NonNull
    public static CoverUtil getInstance() {
        return instance;
    }

    private String mPath;

    public void setInputPath(String path) {
        mPath = path;
    }

    /**
     * 创建缩略图，必须先调用{@link CoverUtil#setInputPath(String)} 设置视频路径
     */
    @SuppressLint("StaticFieldLeak")
    public void createThumbFile(@Nullable final ICoverListener listener) {
        AsyncTask<Void, String, String> task = new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                File outputVideo = new File(mPath);
                if (!outputVideo.exists()) {
                    return null;
                }
                Bitmap bitmap = TXVideoInfoReader.getInstance().getSampleImage(0, mPath);
                if (bitmap == null) {
                    return null;
                }
                String folder = Environment.getExternalStorageDirectory() + File.separator + UGCKitConstants.DEFAULT_MEDIA_PACK_FOLDER;
                File appDir = new File(folder);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                String fileName = "thumbnail" + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }

            @Override
            protected void onPostExecute(String coverImagePath) {
                if (listener != null) {
                    listener.onCoverPath(coverImagePath);
                }
            }

        };
        task.execute();
    }

    public interface ICoverListener {
        void onCoverPath(String coverPath);
    }
}

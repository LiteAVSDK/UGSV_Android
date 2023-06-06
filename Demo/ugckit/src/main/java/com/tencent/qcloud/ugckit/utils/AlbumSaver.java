package com.tencent.qcloud.ugckit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import android.util.Log;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.UGCKit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于将视频保存到本地相册
 */
public class AlbumSaver {

    private static final String TAG                     = "AlbumSaver";
    public  static final String VOLUME_EXTERNAL_PRIMARY = "external_primary";
    private static final String IS_PENDING              = "is_pending";

    private static AlbumSaver      sInstance;
    private final  ContentResolver mContentResolver;
    private final  Context         mContext;
    private        long            mVideoDuration;
    private        String          mVideoOutputPath;
    private        String          mCoverImagePath;
    private        ExecutorService mExecutorService;

    public static AlbumSaver getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AlbumSaver(context);
        }
        return sInstance;
    }

    private AlbumSaver(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mExecutorService = Executors.newFixedThreadPool(7);
        mContentResolver = context.getApplicationContext().getContentResolver();
    }

    /**
     * 设置保存视频的信息
     *
     * @param videoPath 视频路径
     * @param duration  视频时长
     * @param coverPath 封面路径
     */
    public void setOutputProfile(String videoPath, long duration, String coverPath) {
        mVideoOutputPath = videoPath;
        mVideoDuration = duration;
        mCoverImagePath = coverPath;
    }

    /**
     * 异步插入到本地相册
     */
    public void saveVideoToDCIMAsync(final OnSaveVideoToDCIMListener listener) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
               if (saveVideoToDCIM()) {
                   if (listener != null) {
                       listener.onSavedSuccess();
                   }
               } else {
                   if (listener != null) {
                       listener.onSavedFailed();
                   }
               }
            }
        });
    }

    /**
     * 同步插入到本地相册
     */
    public boolean saveVideoToDCIM() {
        if (Build.VERSION.SDK_INT >= 29) {
            return saveVideoToDCIMOnAndroid10();
        } else {
            return saveVideoToDCIMBelowAndroid10();
        }
    }


    private boolean saveVideoToDCIMBelowAndroid10() {
        File file = new File(mVideoOutputPath);
        if (file.exists()) {
            try {
                ContentValues values = initCommonContentValues(file);
                values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.VideoColumns.DURATION, mVideoDuration);
                mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

                if (mCoverImagePath != null) {
                    insertVideoThumb(file.getPath(), mCoverImagePath);
                }
                ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_publish_save_aibum));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.d(TAG, "file :" + mVideoOutputPath + " is not exists");
            return false;
        }
    }

    /**
     * Android 10(Q) 保存视频文件到本地的方法
     */
    private boolean saveVideoToDCIMOnAndroid10() {
        File file = new File(mVideoOutputPath);
        if (file.exists()) {
            ContentValues values = new ContentValues();
            long currentTimeInSeconds = System.currentTimeMillis();
            values.put(MediaStore.MediaColumns.TITLE, file.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeInSeconds);
            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeInSeconds);
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            // 时长
            values.put(MediaStore.Video.VideoColumns.DURATION, mVideoDuration);
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
            // Android 10 插入到图库标志位
            values.put(IS_PENDING, 1);

            Uri collection = MediaStore.Video.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY);
            Uri item = UGCKit.getAppContext().getContentResolver().insert(collection, values);
            ParcelFileDescriptor pfd = null;
            FileOutputStream fos = null;
            FileInputStream fis = null;
            try {
                pfd = UGCKit.getAppContext().getContentResolver().openFileDescriptor(item, "w");
                // Write data into the pending image.
                fos = new FileOutputStream(pfd.getFileDescriptor());
                fis = new FileInputStream(file);
                byte[] data = new byte[1024];
                int length = -1;
                while ((length = fis.read(data)) != -1) {
                    fos.write(data, 0, length);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pfd != null) {
                    try {
                        pfd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 插入成功后，更新状态，让其他 app 可以看到新的视频
            values.clear();
            values.put(IS_PENDING, 0);
            UGCKit.getAppContext().getContentResolver().update(item, values, null, null);

            ToastUtil.toastShortMessage(UGCKit.getAppContext().getString(R.string.ugckit_publish_save_aibum));
            return true;
        } else {
            Log.d(TAG, "file :" + mVideoOutputPath + " is not exists");
            return false;
        }
    }

    @NonNull
    private ContentValues initCommonContentValues(@NonNull File saveFile) {
        ContentValues values = new ContentValues();
        long currentTimeInSeconds = System.currentTimeMillis();
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeInSeconds);
        values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeInSeconds);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    /**
     * 插入视频缩略图
     *
     * @param videoPath
     * @param coverPath
     */
    private void insertVideoThumb(String videoPath, String coverPath) {
        //以下是查询上面插入的数据库Video的id（用于绑定缩略图）
        //根据路径查询
        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Thumbnails._ID},//返回id列表
                String.format("%s = ?", MediaStore.Video.Thumbnails.DATA), //根据路径查询数据库
                new String[]{videoPath}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String videoId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails._ID));
                //查询到了Video的id
                ContentValues thumbValues = new ContentValues();
                thumbValues.put(MediaStore.Video.Thumbnails.DATA, coverPath);//缩略图路径
                thumbValues.put(MediaStore.Video.Thumbnails.VIDEO_ID, videoId);//video的id 用于绑定
                //Video的kind一般为1
                thumbValues.put(MediaStore.Video.Thumbnails.KIND, MediaStore.Video.Thumbnails.MINI_KIND);
                //只返回图片大小信息，不返回图片具体内容
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(coverPath, options);
                if (bitmap != null) {
                    thumbValues.put(MediaStore.Video.Thumbnails.WIDTH, bitmap.getWidth());//缩略图宽度
                    thumbValues.put(MediaStore.Video.Thumbnails.HEIGHT, bitmap.getHeight());//缩略图高度
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
                mContentResolver.insert(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbValues);//缩略图数据库
            }
            cursor.close();
        }
    }

    public void release() {
        sInstance = null;
        mExecutorService.shutdown();
    }

    public interface OnSaveVideoToDCIMListener {
        void onSavedSuccess();

        void onSavedFailed();
    }
}

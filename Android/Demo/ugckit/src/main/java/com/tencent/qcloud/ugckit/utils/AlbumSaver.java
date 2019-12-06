package com.tencent.qcloud.ugckit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.tencent.qcloud.ugckit.UGCKitConstants;

import java.io.File;

/**
 * 用于将视频保存到本地相册
 */
public class AlbumSaver {

    private static final String TAG = "AlbumSaver";
    private static AlbumSaver sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private String mVideoOutputPath;
    private long mVideoDuration;
    private String mCoverImagePath;

    public static AlbumSaver getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AlbumSaver(context);
        }
        return sInstance;
    }

    private AlbumSaver(@NonNull Context context) {
        mContext = context.getApplicationContext();
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
     * 插入到本地相册
     */
    public String saveVideoToDCIM() {
        File file = new File(mVideoOutputPath);
        if (file.exists()) {
            try {
                File newFile = new File(Environment.getExternalStorageDirectory()
                        + File.separator + Environment.DIRECTORY_DCIM
                        + File.separator + "Camera" + File.separator + file.getName());
                file.renameTo(newFile);
                mVideoOutputPath = newFile.getAbsolutePath();

                ContentValues values = initCommonContentValues(newFile);
                values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.VideoColumns.DURATION, mVideoDuration);
                mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

                if (mCoverImagePath != null) {
                    insertVideoThumb(newFile.getPath(), mCoverImagePath);
                }
                ToastUtil.toastShortMessage("视频已保存到" + UGCKitConstants.DCIM_PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mVideoOutputPath;
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
}

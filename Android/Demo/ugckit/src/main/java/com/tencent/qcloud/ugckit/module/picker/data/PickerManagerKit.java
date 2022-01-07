package com.tencent.qcloud.ugckit.module.picker.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

public class PickerManagerKit {
    private static final String TAG = "PickerManagerKit";

    private static PickerManagerKit sInstance;
    private final  Context          mContext;
    private final  ContentResolver  mContentResolver;
    private final  Uri              mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    public static PickerManagerKit getInstance(@NonNull Context context) {
        if (sInstance == null)
            sInstance = new PickerManagerKit(context);
        return sInstance;
    }

    private PickerManagerKit(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mContentResolver = context.getApplicationContext().getContentResolver();
    }

    @NonNull
    public ArrayList<TCVideoFileInfo> getAllVideo() {
        ArrayList<TCVideoFileInfo> videos = new ArrayList<TCVideoFileInfo>();
        String[] mediaColumns = new String[]{
                MediaStore.Video.VideoColumns._ID,
                //DATA 数据在 Android Q 以前代表了文件的路径，但在 Android Q上该路径无法被访问。
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION
        };
        Cursor cursor = mContentResolver.query(mUri, mediaColumns, null, null, null);

        if (cursor == null) return videos;

        if (cursor.moveToFirst()) {
            do {
                TCVideoFileInfo fileItem = new TCVideoFileInfo();
                // 兼容 Android 10以上
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndexOrThrow((MediaStore.Video.Media._ID))));
                fileItem.setFileUri(uri);
                fileItem.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                fileItem.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                if (duration < 0)
                    duration = 0;
                fileItem.setDuration(duration);

                String filePath = fileItem.getFilePath();
                if (filePath != null) {
                    if (filePath.toLowerCase().endsWith("mp4") || filePath.toLowerCase().endsWith(".mov")) {
                        File file = new File(filePath);
                        if (file.exists() && file.canRead()) {
                            videos.add(fileItem);
                        }
                    }
                }
                Log.d(TAG, "fileItem = " + fileItem.toString());
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return videos;
    }

    @NonNull
    public ArrayList<TCVideoFileInfo> getAllPictrue() {
        ArrayList<TCVideoFileInfo> pictureList = new ArrayList<TCVideoFileInfo>();
        String[] mediaColumns = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                //DATA 数据在 Android Q 以前代表了文件的路径，但在 Android Q上该路径无法被访问。
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DESCRIPTION
        };
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                TCVideoFileInfo fileItem = new TCVideoFileInfo();
                // 兼容 Android 10以上
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndexOrThrow((MediaStore.Images.Media._ID))));
                fileItem.setFileUri(uri);
                fileItem.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                fileItem.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                fileItem.setFileType(TCVideoFileInfo.FILE_TYPE_PICTURE);
                pictureList.add(fileItem);
            }
            cursor.close();
        }
        return pictureList;
    }
}
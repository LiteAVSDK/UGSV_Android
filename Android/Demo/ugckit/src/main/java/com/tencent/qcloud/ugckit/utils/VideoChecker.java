package com.tencent.qcloud.ugckit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import java.util.List;

public class VideoChecker {

    public static boolean isVideoDamaged(Context context, TCVideoFileInfo info) {
        if (info.getDuration() == 0) {
            //数据库获取到的时间为0，使用Retriever再次确认是否损坏
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    retriever.setDataSource(context, info.getFileUri());
                } else {
                    retriever.setDataSource(info.getFilePath());
                }
            } catch (Exception e) {
                return true;//无法正常打开，也是错误
            }
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            if (TextUtils.isEmpty(duration))
                return true;
            return Integer.valueOf(duration) == 0;
        }
        return false;
    }

    public static boolean isVideoDamaged(Context context, List<TCVideoFileInfo> list) {
        for (TCVideoFileInfo info : list) {
            if (isVideoDamaged(context, info)) {
                return true;
            }
        }
        return false;
    }

    public static void showErrorDialog(Context context, String msg) {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(context, R.style.UGCKitConfirmDialogStyle);
        normalDialog.setMessage(msg);
        normalDialog.setCancelable(false);
        normalDialog.setPositiveButton("知道了", null);
        normalDialog.show();
    }
}

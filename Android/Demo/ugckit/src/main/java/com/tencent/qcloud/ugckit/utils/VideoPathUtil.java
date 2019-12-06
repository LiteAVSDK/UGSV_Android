package com.tencent.qcloud.ugckit.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.tencent.qcloud.ugckit.UGCKitConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 视频路径生成器
 */
public class VideoPathUtil {
    /**
     * 生成编辑后输出视频路径
     *
     * @return
     */
    public static String generateVideoPath() {
        String outputPath = Environment.getExternalStorageDirectory() + File.separator + UGCKitConstants.DEFAULT_MEDIA_PACK_FOLDER;
        File outputFolder = new File(outputPath);

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        String current = String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = sdf.format(new Date(Long.valueOf(current + "000")));
        String saveFileName = String.format("TXVideo_%s.mp4", time);
        return outputFolder + "/" + saveFileName;
    }

    public static String getCustomVideoOutputPath() {
        return getCustomVideoOutputPath(null);
    }

    public static String getCustomVideoOutputPath(String fileNamePrefix) {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        String time = sdf.format(new Date(currentTime));
        String outputDir = Environment.getExternalStorageDirectory() + File.separator + UGCKitConstants.OUTPUT_DIR_NAME;
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath;
        if (TextUtils.isEmpty(fileNamePrefix)) {
            tempOutputPath = outputDir + File.separator + "TXUGC_" + time + ".mp4";
        } else {
            tempOutputPath = outputDir + File.separator + "TXUGC_" + fileNamePrefix + time + ".mp4";
        }
        return tempOutputPath;
    }
}

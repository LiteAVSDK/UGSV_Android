package com.tencent.qcloud.ugckit.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;

public class VideoDeviceUtil {
    private static final String TAG = "VideoDeviceUtil";

    public VideoDeviceUtil() {
    }

    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
    }

    @TargetApi(18)
    public static long getAvailableSize(@NonNull StatFs statFs) {
        long availableBytes;
        if (VideoUtil.hasJellyBeanMR2()) {
            availableBytes = statFs.getAvailableBytes();
        } else {
            availableBytes = (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }

        return availableBytes;
    }

    public static boolean isExternalStorageSpaceEnough(long fileSize) {
        return true;
    }

    @Nullable
    public static File getExternalFilesDir(@NonNull Context context, String folder) {
        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir == null) {
            Log.e(TAG, "sdcardDir is null");
            return null;
        }
        String path = sdcardDir.getPath();

        File file = new File(path + File.separator + folder);

        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return file;
    }

    public static long getRuntimeRemainSize(int memoryClass) {
        long remainMemory = Runtime.getRuntime().maxMemory() - getHeapAllocatedSizeInKb() * 1024L;
        switch (memoryClass) {
            case 0:
            default:
                break;
            case 1:
                remainMemory /= 1024L;
                break;
            case 2:
                remainMemory /= 1048576L;
        }

        return remainMemory;
    }

    public static long getHeapAllocatedSizeInKb() {
        long heapAllocated = getRuntimeTotalMemory(1) - getRuntimeFreeMemory(1);
        return heapAllocated;
    }

    private static long getRuntimeTotalMemory(int memoryClass) {
        long totalMemory = 0L;
        switch (memoryClass) {
            case 0:
                totalMemory = Runtime.getRuntime().totalMemory();
                break;
            case 1:
                totalMemory = Runtime.getRuntime().totalMemory() / 1024L;
                break;
            case 2:
                totalMemory = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
                break;
            default:
                totalMemory = Runtime.getRuntime().totalMemory();
        }

        return totalMemory;
    }

    private static long getRuntimeFreeMemory(int memoryClass) {
        long freeMemory = 0L;
        switch (memoryClass) {
            case 0:
                freeMemory = Runtime.getRuntime().freeMemory();
                break;
            case 1:
                freeMemory = Runtime.getRuntime().freeMemory() / 1024L;
                break;
            case 2:
                freeMemory = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
                break;
            default:
                freeMemory = Runtime.getRuntime().freeMemory();
        }

        return freeMemory;
    }

}

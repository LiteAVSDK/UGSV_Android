package com.tencent.qcloud.ugckit.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public class VideoDeviceUtil {
    private static final String TAG = VideoDeviceUtil.class.getSimpleName();

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

    public static boolean isExternalStorageAvailable() {
        if (!"mounted".equals(Environment.getExternalStorageState()) && Environment.isExternalStorageRemovable()) {
            return false;
        } else {
            try {
                new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
                return true;
            } catch (Exception var1) {
                return false;
            }
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
        File sdcard = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(sdcard.getAbsolutePath());
        return getAvailableSize(statFs) > fileSize;
    }

    @Nullable
    private static File getExternalFilesDir(@NonNull Context context) {
        File file = null;
        file = context.getExternalFilesDir((String) null);
        if (file == null) {
            String filesDir = "/Android/data/" + context.getPackageName() + "/files/";
            file = new File(Environment.getExternalStorageDirectory().getPath() + filesDir);
        }

        return file;
    }

    @Nullable
    public static File getExternalFilesDir(@NonNull Context context, String folder) {
        String path = null;
        if (isExternalStorageAvailable() && isExternalStorageSpaceEnough(52428800L)) {
            path = getExternalFilesDir(context).getPath();
        }

        File file = new File(path + File.separator + folder);

        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception var5) {
            ;
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

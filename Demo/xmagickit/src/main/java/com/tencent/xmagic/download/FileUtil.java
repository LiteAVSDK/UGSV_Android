package com.tencent.xmagic.download;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null) {
            return;
        }
        if (!fileOrDirectory.exists()) {
            return;
        }
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static void writeContentIntoFile(String directory, String fileName, String content) {
        if (directory == null || fileName == null || content == null) {
            return;
        }
        FileWriter writer = null;
        try {
            String name = directory + File.separator + fileName;
            writer = new FileWriter(name);
            writer.write(content);
        } catch (IOException e) {
            Log.e(TAG, "writeContentIntoFile: e=" + e.toString());
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readOneLineFromFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, "readContentFromFile: e=" + e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long bytes = availableBlocks * blockSize;
        Log.d(TAG, "getAvailableInternalMemorySize: bytes=" + bytes);
        return bytes;
    }

    public static boolean unzipFile(String directory, String fileName) {
        if (directory == null || fileName == null) {
            return false;
        }
        String zipFilePath = directory + File.separator + fileName;
        Log.d(TAG, "checkAndUnzipFile: zipFilePath=" + zipFilePath);
        if (!new File(zipFilePath).exists()) {
            Log.e(TAG, "checkAndUnzipFile: file not exists");
            return false;
        }

        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipFile zipFile = null;
        try {
            final int CACHE_SIZE = 1024;
            File fileFolder = new File(directory);
            zipFile = new ZipFile(zipFilePath);
            Enumeration<?> emu = zipFile.entries();

            byte[] cache = new byte[CACHE_SIZE];
            while (emu.hasMoreElements()) {
                ZipEntry entry;
                entry = (ZipEntry) emu.nextElement();
                if (entry.getName().contains("../")) { // 处理金刚扫描：目录遍历漏洞
                    continue;
                }
                if (entry.isDirectory()) {
                    new File(fileFolder, entry.getName()).mkdirs();
                    continue;
                }
                bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(fileFolder, entry.getName());
                File parentFile = file.getParentFile();
                if (parentFile != null && (!parentFile.exists())) {
                    parentFile.mkdirs();
                }
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos, CACHE_SIZE);
                int nRead = 0;
                while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                    fos.write(cache, 0, nRead);
                }
                bos.flush();
            }
            Log.d(TAG, "checkAndUnzipFile: success");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage() + ": " + e.getClass().getName());
            return false;
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bos != null) {
                try {
                    bos.close();
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

            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getMd5(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return null;
        }
        FileInputStream in = null;
        String result = "";
        byte[] buffer = new byte[8192];
        int len;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            byte[] bytes = md5.digest();

            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}

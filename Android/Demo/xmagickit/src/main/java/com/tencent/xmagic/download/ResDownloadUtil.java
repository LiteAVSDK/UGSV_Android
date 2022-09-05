package com.tencent.xmagic.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.xmagic.utils.OnDownloadListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResDownloadUtil {
    //下载后的文件夹组织结构如下
    //files
    //--xmagic
    //------downloaded_zip_md5_assets
    //------light_assets
    //------------default
    //------------gan
    //------------images
    //------------js
    //------------material-prebuild
    //------------models
    //------------personface
    //------------shaders
    //------------sticker3d
    //------------template.json
    //------light_material
    //------------lut
    //------MotionRes
    //------------res1
    //------------res2
    //--xmagic_libs
    //------liblibpag.so
    //------liblight-sdk.so
    //------libv8jni.so
    //------downloaded_zip_md5_libs

    public static final int FILE_TYPE_LIBS = 0;
    public static final int FILE_TYPE_ASSETS = 1;
    public static final int FILE_TYPE_MOTION_RES = 2;
    private static final String TAG = "LibDownloadUtil";
    private static final String DL_DIRECTORY_LIBS = "xmagic_libs"; //私有目录/files/xmagic_libs
    private static final String DL_DIRECTORY_ASSETS_PARENT = "xmagic"; //私有目录/files/xmagic
    private static final String DL_DIRECTORY_ASSETS_LIGHT_ASSETS = "light_assets";
    private static final String DL_DIRECTORY_ASSETS_LIGHT_MATERIAL = "light_material";
    private static final String DL_DIRECTORY_ASSETS_LIGHT_MOTION = "MotionRes";
    private static final String DL_ZIP_FILE = "download_zip_file"; //zip文件，解压完成后会删除
    private static final String DL_MD5_FILE_LIBS = "downloaded_zip_md5_libs"; //记录下载的zip文件的md5
    private static final String DL_MD5_FILE_ASSETS = "downloaded_zip_md5_assets"; //记录下载的zip文件的md5

    public static boolean ENABLE_RESUME_FROM_BREAKPOINT = true;//下载服务器是否支持断点续传

    public static String getValidLibsDirectory(Context context, String downloadMd5Libs) {
        String directory = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + File.separator + DL_DIRECTORY_LIBS;
        int libsReady = checkValidLibsExist(directory + File.separator + DL_MD5_FILE_LIBS, downloadMd5Libs);
        if (libsReady != ResDownloadUtil.CHECK_FILE_EXIST) {
            Log.w(TAG, "getValidLibsDirectory: libs not ready");
            return null;
        }
        return directory;
    }

    public static String getValidAssetsDirectory(Context context, String downloadMd5Assets) {
        String directory = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + File.separator + DL_DIRECTORY_ASSETS_PARENT;
        int assetsReady = checkValidLibsExist(directory + File.separator + DL_MD5_FILE_ASSETS, downloadMd5Assets);
        if (assetsReady != ResDownloadUtil.CHECK_FILE_EXIST) {
            Log.w(TAG, "getValidAssetsDirectory: assets not ready");
            return null;
        }
        return directory;
    }

    private static final String MOTION_DL_TEMP_FILE_PREFIX = "temp_zip_file_";
    private static final Set<String> downloadingMotions = new HashSet<>();

    public static void checkOrDownloadMotions(Context context, MotionDLModel model, OnDownloadListener listener) {
        synchronized (downloadingMotions) {
            if (downloadingMotions.contains(model.getCategory() + model.getName())) {
                return;
            }
            downloadingMotions.add(model.getCategory() + model.getName());
        }
        //files/xmagic/MotionRes/model.category/model.name
        String directory = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + File.separator + DL_DIRECTORY_ASSETS_PARENT
                + File.separator + DL_DIRECTORY_ASSETS_LIGHT_MOTION;
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(directory + File.separator + model.getCategory()
                + File.separator + model.getName());
        if (file.exists()) {
            Log.d(TAG, "checkOrDownloadMotions: file exist:" + model.getName());
            // 有一种情况：在解压过程中，进程被杀掉，导致解压出来的文件夹不完整。
            // 因此需要检查该素材对应的zip包是否存在，如果存在，说明解压过程异常中断了，
            // 因此需要删掉file文件夹以及zip包重新下载
            File zipFile = new File(directory + File.separator + model.getCategory()
                    + File.separator + MOTION_DL_TEMP_FILE_PREFIX + model.getName());
            if (zipFile.exists()) {
                Log.d(TAG, "checkOrDownloadMotions: zip file exist");
                FileUtil.deleteRecursive(file);
                FileUtil.deleteRecursive(zipFile);
            } else {
                Log.d(TAG, "checkOrDownloadMotions: zip file NOT exist");
                setModelDownloadFinish(model.getCategory(), model.getName());
                listener.onDownloadSuccess(model.getName());
                return;
            }
        }
        doDownloadMotion(directory, model.getCategory(), model.getName(), model.getUrl(), listener);
    }

    private static void setModelDownloadFinish(String category, String name) {
        synchronized (downloadingMotions) {
            downloadingMotions.remove(category + name);
        }
    }

    private static void doDownloadMotion(String directory, final String category,
                                         final String name, String url, final OnDownloadListener listener) {
        final String parent = directory + File.separator + category;
        File dir = new File(parent);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final String tempZipFile = MOTION_DL_TEMP_FILE_PREFIX + name;
        try {
            download(url, parent, tempZipFile, new OnDownloadListener() {
                @Override
                public void onDownloadSuccess(String downloadedDirectory) {
                    Log.d(TAG, "onDownloadSuccess,downloadDirectory=" + downloadedDirectory);
                    boolean unzipSuccess = FileUtil.unzipFile(downloadedDirectory, tempZipFile);
                    Log.d(TAG, "onDownloadSuccess: unzipSuccess=" + unzipSuccess);
                    //无论解压成功还是失败，都要删掉这个临时zip文件
                    FileUtil.deleteRecursive(new File(downloadedDirectory, tempZipFile));
                    if (unzipSuccess) {
                        setModelDownloadFinish(category, name);
                        listener.onDownloadSuccess(name);
                    } else {
                        //如果解压失败，要把解压后的文件夹也删掉
                        FileUtil.deleteRecursive(new File(downloadedDirectory, name));
                        setModelDownloadFinish(category, name);
                        listener.onDownloadFailed(DownloadErrorCode.UNZIP_FAIL);
                    }
                }

                @Override
                public void onDownloading(int progress) {
                    listener.onDownloading(progress);
                }

                @Override
                public void onDownloadFailed(int errorCode) {
                    //下载失败的话，把那个临时zip文件删掉
                    FileUtil.deleteRecursive(new File(parent, tempZipFile));
                    setModelDownloadFinish(category, name);
                    listener.onDownloadFailed(errorCode);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "doDownloadMotion: e=" + e.toString());
            setModelDownloadFinish(category, name);
            listener.onDownloadFailed(DownloadErrorCode.FILE_IO_ERROR);
        }

    }

    public static void checkOrDownloadFiles(Context context, int fileType, String downloadUrl,
                                            String downloadMd5, OnDownloadListener listener) {
        String directory;
        String existMd5File;
        if (fileType == FILE_TYPE_LIBS) {
            directory = context.getApplicationContext().getFilesDir().getAbsolutePath()
                    + File.separator + DL_DIRECTORY_LIBS;
            existMd5File = DL_MD5_FILE_LIBS;
        } else if (fileType == FILE_TYPE_ASSETS) {
            directory = context.getApplicationContext().getFilesDir().getAbsolutePath()
                    + File.separator + DL_DIRECTORY_ASSETS_PARENT;
            existMd5File = DL_MD5_FILE_ASSETS;
        } else {
            return;
        }
        doCheckOrDownloadFiles(fileType, directory, existMd5File, downloadUrl, downloadMd5, listener);
    }

    private static final Object downloadLock = new Object();
    private static boolean libsIsDownding = false;
    private static boolean assetsIsDownding = false;

    private static void doCheckOrDownloadFiles(final int fileType,
                                               String directory, String existMd5File, String downloadUrl,
                                               String downloadMd5, final OnDownloadListener listener) {
        int fileStatus = checkValidLibsExist(directory + File.separator + existMd5File, downloadMd5);
        if (fileStatus == CHECK_FILE_EXIST) {
            Log.d(TAG, "checkDownload: file exists,valid md5");
            listener.onDownloadSuccess(directory);
            return;
        }

        synchronized (downloadLock) {
            if (fileType == FILE_TYPE_LIBS) {
                if (libsIsDownding) {
                    //忽略即可，不用callback了
                    return;
                }
                libsIsDownding = true;
            } else if (fileType == FILE_TYPE_ASSETS) {
                if (assetsIsDownding) {
                    //忽略即可，不用callback了
                    return;
                }
                assetsIsDownding = true;
            }
        }

        //md5文件不存在，或者md5值不符合，需要删除整个文件夹重新下载
        if (fileType == FILE_TYPE_LIBS) {
            File dir = new File(directory);
            if (fileStatus == CHECK_FILE_MD5_INVALID) {
                //md5值不符合，需要删除 files/xmagic_libs 文件夹重新下载
                FileUtil.deleteRecursive(dir);
            } else if (fileStatus == CHECK_FILE_NOT_EXIST) {
                //MD5文件还不存在，那有可能是还没开始下载，也可能是下载到一半了需要断点续传
                if (ENABLE_RESUME_FROM_BREAKPOINT) {
                    Log.d(TAG, "doCheckOrDownloadFiles: CHECK_FILE_NOT_EXIST,can not delete");
                } else {
                    //如果没开启断点续传，要把可能下了一半的文件删除掉
                    FileUtil.deleteRecursive(dir);
                }
            }
            dir.mkdirs();

        } else if (fileType == FILE_TYPE_ASSETS) {
            //files/xmagic/light_assets
            File dirLightAssets = new File(directory + File.separator + DL_DIRECTORY_ASSETS_LIGHT_ASSETS);
            //files/xmagic/light_material
            File dirLightMaterial = new File(directory + File.separator + DL_DIRECTORY_ASSETS_LIGHT_MATERIAL);
            if (fileStatus == CHECK_FILE_MD5_INVALID) {
                FileUtil.deleteRecursive(dirLightAssets);
                FileUtil.deleteRecursive(dirLightMaterial);
            } else if (fileStatus == CHECK_FILE_NOT_EXIST) {
                if (ENABLE_RESUME_FROM_BREAKPOINT) {
                    Log.d(TAG, "doCheckOrDownloadFiles: CHECK_FILE_NOT_EXIST");
                } else {
                    //如果没开启断点续传，要把可能下了一半的文件删除掉
                    FileUtil.deleteRecursive(dirLightAssets);
                    FileUtil.deleteRecursive(dirLightMaterial);
                }
            }
            dirLightAssets.mkdirs();
            dirLightMaterial.mkdirs();
        }

        try {
            download(downloadUrl, directory, DL_ZIP_FILE, new OnDownloadListener() {
                @Override
                public void onDownloadSuccess(String downloadedDirectory) {
                    Log.d(TAG, "onDownloadSuccess");
                    boolean unzipSuccess = FileUtil.unzipFile(downloadedDirectory, DL_ZIP_FILE);
                    Log.d(TAG, "onDownloadSuccess: unzipSuccess=" + unzipSuccess);
                    if (unzipSuccess) {
                        //写一个标识文件到目录，写入的内容是压缩包的MD5
                        String md5 = FileUtil.getMd5(new File(downloadedDirectory, DL_ZIP_FILE));
                        if (TextUtils.isEmpty(md5)) {
                            FileUtil.deleteRecursive(new File(downloadedDirectory));
                            setDownloadFinish(fileType);
                            listener.onDownloadFailed(DownloadErrorCode.MD5_FAIL);
                            return;
                        }
                        if (fileType == FILE_TYPE_ASSETS) {
                            //assets解压后，还需要整理文件夹，把二级目录抽到根目录
                            if (organizeAssetsDirectory(downloadedDirectory)) {
                                //解压、copy文件都成功，才算成功
                                FileUtil.writeContentIntoFile(downloadedDirectory, DL_MD5_FILE_ASSETS, md5);
                                FileUtil.deleteRecursive(new File(downloadedDirectory, DL_ZIP_FILE));
                                setDownloadFinish(fileType);
                                listener.onDownloadSuccess(downloadedDirectory);
                            } else {
                                //copy文件失败了，删除整个目录
                                FileUtil.deleteRecursive(new File(downloadedDirectory));
                                setDownloadFinish(fileType);
                                listener.onDownloadFailed(DownloadErrorCode.FILE_IO_ERROR);
                            }
                        } else if (fileType == FILE_TYPE_LIBS) {
                            FileUtil.writeContentIntoFile(downloadedDirectory, DL_MD5_FILE_LIBS, md5);
                            //解压成功，删除zip包
                            FileUtil.deleteRecursive(new File(downloadedDirectory, DL_ZIP_FILE));
                            setDownloadFinish(fileType);
                            listener.onDownloadSuccess(downloadedDirectory);
                        }
                    } else {
                        //解压失败，删除整个目录
                        FileUtil.deleteRecursive(new File(downloadedDirectory));
                        setDownloadFinish(fileType);
                        listener.onDownloadFailed(DownloadErrorCode.UNZIP_FAIL);
                    }
                }

                @Override
                public void onDownloading(int progress) {
                    Log.d(TAG, "onDownloading: progress=" + progress);
                    listener.onDownloading(progress);
                }

                @Override
                public void onDownloadFailed(int errorCode) {
                    Log.d(TAG, "onDownloadFailed: ");
                    setDownloadFinish(fileType);
                    listener.onDownloadFailed(errorCode);
                }

            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "doCheckOrDownloadFiles: FileNotFoundException,e=" + e.toString());
            setDownloadFinish(fileType);
            listener.onDownloadFailed(DownloadErrorCode.FILE_IO_ERROR);
        }
    }


    private static void setDownloadFinish(int fileType) {
        synchronized (downloadLock) {
            if (fileType == FILE_TYPE_LIBS) {
                libsIsDownding = false;
            } else if (fileType == FILE_TYPE_ASSETS) {
                assetsIsDownding = false;
            }
        }
    }

    //downloadedDirectory的值是DL_DIRECTORY_ASSETS
    private static boolean organizeAssetsDirectory(String downloadedDirectory) {
        for (String path : new String[]{"Light3DPlugin", "LightCore", "LightHandPlugin", "LightSegmentPlugin"}) {
            if (!copyAssets(downloadedDirectory, path, DL_DIRECTORY_ASSETS_LIGHT_ASSETS)) {
                return false;
            }
            //copy成功则删除这个目录
            FileUtil.deleteRecursive(new File(downloadedDirectory + File.separator + path));
        }

        for (String path : new String[]{"lut"}) {
            if (!copyAssets(downloadedDirectory, path, DL_DIRECTORY_ASSETS_LIGHT_MATERIAL + File.separator + path)) {
                return false;
            }
            //copy成功则删除这个目录
            FileUtil.deleteRecursive(new File(downloadedDirectory + File.separator + path));
        }
        return true;
    }

    private static boolean copyAssets(String parent, String oldPath, String newPath) {
        FileInputStream is = null;
        FileOutputStream fos = null;
        try {
            File file = new File(parent + File.separator + oldPath);
            if (!file.exists()) {
                //有可能指定的文件夹不存在，那就只能copy部分文件，不影响copy结果
                return true;
            }
            if (file.isDirectory()) {
                String[] fileNames = file.list();
                if (fileNames == null || fileNames.length <= 0) {
                    //有可能是空文件夹
                    return true;
                }
                Log.e(TAG, "copyAssets path: " + Arrays.toString(fileNames));
                File newFile = new File(parent + File.separator + newPath);
                newFile.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(parent, oldPath + File.separator + fileName, newPath + File.separator + fileName);
                }
            } else {
                is = new FileInputStream(file);
                fos = new FileOutputStream(new File(parent + File.separator + newPath));
                byte[] buffer = new byte[1024 * 1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {    // 循环从输入流读取
                    fos.write(buffer, 0, byteCount);     // 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
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
        }
    }

    public static final int CHECK_FILE_EXIST = 0;
    public static final int CHECK_FILE_MD5_INVALID = -1;
    public static final int CHECK_FILE_NOT_EXIST = -2;

    private static int checkValidLibsExist(String existMD5File, String downloadMd5) {
        File md5File = new File(existMD5File);
        if (md5File.exists()) {
            long time = System.currentTimeMillis();
            String historyMd5 = FileUtil.readOneLineFromFile(md5File);
            long timeCount = (System.currentTimeMillis() - time);
            Log.d(TAG, "checkDownload: historyMd5 =" + historyMd5 + ",time used = " + timeCount);
            if (historyMd5 != null && historyMd5.equalsIgnoreCase(downloadMd5)) {
                Log.d(TAG, "checkDownload: file exists,valid md5");
                return CHECK_FILE_EXIST;
            }
            return CHECK_FILE_MD5_INVALID;
        }
        return CHECK_FILE_NOT_EXIST;
    }

    private static void download(String downloadUrl, String directory, String dlZipFile,
                                 OnDownloadListener onDownloadSuccess) throws FileNotFoundException {
        if (ENABLE_RESUME_FROM_BREAKPOINT) {
            downloadWithResumeBreakPoint(downloadUrl, directory, dlZipFile, onDownloadSuccess);
        } else {
            downloadWithoutResumeBreakPoint(downloadUrl, directory, dlZipFile, onDownloadSuccess);
        }
    }

    private static void downloadWithoutResumeBreakPoint(final String url,
                                                        final String directory, final String fileName,
                                                        final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "enqueue onFailure: e=" + e.toString());
                listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response == null || response.body() == null || response.body().byteStream() == null) {
                    Log.e(TAG, "onResponse: null or body null");
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
                    return;
                }
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    readData(response, is, fos);
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: e=" + e.toString());
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_FILE_ERROR);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: finally close is,e=" + e.toString());
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: finally close fos,e=" + e.toString());
                    }
                }
            }

            private void readData(Response response, InputStream is, FileOutputStream fos) throws IOException {
                is = response.body().byteStream();
                long total = response.body().contentLength();
                Log.d(TAG, "onResponse: response.body().contentLength() = " + total);
                if (total <= 0) {
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
                    return;
                }
                fos = new FileOutputStream(new File(directory + File.separator + fileName));
                long sum = 0;
                int len;
                byte[] buf = new byte[2048];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    if (progress < 0) {
                        progress = 0;
                    }
                    if (progress > 100) {
                        progress = 100;
                    }
                    listener.onDownloading(progress);
                }
                fos.flush();
                Log.d(TAG, "onResponse: onDownloadSuccess");
                listener.onDownloadSuccess(directory);
            }
        });
    }

    private static void downloadWithResumeBreakPoint(final String url, final String directory,
                                                     final String fileName,
                                                     final OnDownloadListener listener)
            throws FileNotFoundException {
        File file = new File(directory, fileName);
        final RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        // 断点续传：重新开始下载的位置：file.length()
        final long existFileLength = file.exists() ? file.length() : 0;
        Log.d(TAG, "download: file.length=" + existFileLength);
        String range = String.format(Locale.getDefault(), "bytes=%d-", existFileLength);

        Request request = new Request.Builder()
                .url(url)
                .header("range", range)
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "enqueue onFailure: e=" + e.toString());
                listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response == null || response.body() == null || response.body().byteStream() == null) {
                    Log.e(TAG, "onResponse: null or body null");
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
                    return;
                }
                InputStream is = null;
                try {
                    readData(accessFile, existFileLength, response, is);
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: e=" + e.toString());
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_FILE_ERROR);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: finally close is,e=" + e.toString());
                    }
                }
            }

            private void readData(RandomAccessFile accessFile, long existFileLength,
                                  Response response, InputStream is) throws IOException {
                is = response.body().byteStream();
                long total = response.body().contentLength();
                Log.d(TAG, "onResponse: response.body().contentLength() = " + total);
                if (total <= 0) {
                    listener.onDownloadFailed(DownloadErrorCode.NETWORK_ERROR);
                    return;
                }
                accessFile.seek(existFileLength);
                long sum = existFileLength;
                int len;
                total += existFileLength;
                byte[] buf = new byte[2048];
                while ((len = is.read(buf)) != -1) {
                    accessFile.write(buf, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    if (progress < 0) {
                        progress = 0;
                    }
                    if (progress > 100) {
                        progress = 100;
                    }
                    listener.onDownloading(progress);
                }
                Log.d(TAG, "onResponse: onDownloadSuccess");
                listener.onDownloadSuccess(directory);
            }
        });
    }


}
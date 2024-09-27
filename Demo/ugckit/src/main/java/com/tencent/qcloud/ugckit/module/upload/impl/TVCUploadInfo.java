package com.tencent.qcloud.ugckit.module.upload.impl;

import android.text.TextUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video upload parameters
 * 视频上传参数
 */
public class TVCUploadInfo {
    private String fileType;
    private String filePath;
    private long fileLastModTime;
    private String coverType;
    private String coverPath;
    private long coverLastModTime;
    private String fileName = null;
    private long videoFileSize = 0;
    private long coverFileSize = 0;
    private String coverName;

    /**
     * Create upload parameters
     * 创建上传参数
     *
     * @param fileType  File type
     *                  文件类型
     * @param filePath  File local path
     *                  文件本地路径
     * @param coverType Cover image type
     *                  封面图片类型
     * @param coverPath Cover image local path
     *                  封面图片本地路径
     */
    public TVCUploadInfo(String fileType, String filePath, String coverType, String coverPath) {
        this.fileType = fileType;
        this.filePath = filePath;
        this.coverType = coverType;
        this.coverPath = coverPath;
    }

    public TVCUploadInfo(
            String fileType, String filePath, String coverType, String coverPath, String fileName) {
        this.fileType = fileType;
        this.filePath = filePath;
        this.coverType = coverType;
        this.coverPath = coverPath;
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCoverImgType() {
        return coverType;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public boolean isNeedCover() {
        return !TextUtils.isEmpty(coverType) && !TextUtils.isEmpty(coverPath);
    }

    public String getFileName() {
        if (null == fileName) {
            int pos = filePath.lastIndexOf('/');
            if (-1 == pos) {
                pos = 0;
            } else {
                pos++;
            }
            fileName = filePath.substring(pos);
        }

        return fileName;
    }

    public String getCoverName() {
        if (null == coverName) {
            int pos = coverPath.lastIndexOf('/');
            if (-1 == pos) {
                pos = 0;
            } else {
                pos++;
            }
            coverName = coverPath.substring(pos);
        }
        return coverName;
    }

    public long getFileSize() {
        if (0 == videoFileSize) {
            TVCLog.i("getFileSize", "getFileSize: " + filePath);
            File file = new File(filePath);
            if (file.exists()) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                    videoFileSize = randomAccessFile.length();
                } catch (Exception e) {
                    TVCLog.e("getFileSize", "getFileSize: " + e);
                }
            }
        }
        return videoFileSize;
    }

    public long getCoverFileSize() {
        if (0 == coverFileSize) {
            TVCLog.i("getCoverFileSize", "getCoverFileSize: " + coverPath);
            File file = new File(coverPath);
            if (file.exists()) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                    coverFileSize = randomAccessFile.length();
                } catch (Exception e) {
                    TVCLog.e("getCoverFileSize", "getCoverFileSize: " + e);
                }
            }
        }
        return coverFileSize;
    }

    public long getCoverLastModifyTime() {
        if (0 == coverLastModTime) {
            File f = new File(coverPath);
            coverLastModTime = f.lastModified();
        }
        return coverLastModTime;
    }

    public long getFileLastModifyTime() {
        if (0 == fileLastModTime) {
            File f = new File(filePath);
            fileLastModTime = f.lastModified();
        }
        return fileLastModTime;
    }

    public boolean isContainSpecialCharacters(String string) {
        String regEx = "[/:*?\"<>]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }
}

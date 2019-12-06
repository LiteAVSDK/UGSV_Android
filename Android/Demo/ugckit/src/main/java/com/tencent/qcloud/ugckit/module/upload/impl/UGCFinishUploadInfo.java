package com.tencent.qcloud.ugckit.module.upload.impl;

import android.text.TextUtils;

/**
 * 上传结束参数
 */
public class UGCFinishUploadInfo {
    private String fileName;
    private String fileType;
    private long fileSize;
    private String cverImgType="";
    private String videoFileId;
    private String imgFieldId="";
    private String uploadSession;
    private String domain;
    private String vodSessionKey;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setCverImgType(String cverImgType) {
        this.cverImgType = cverImgType;
    }

    public void setVideoFileId(String videoFileId) {
        this.videoFileId = videoFileId;
    }

    public void setImgFieldId(String imgFieldId) {
        this.imgFieldId = imgFieldId;
    }

    public void setUploadSession(String uploadSession) {
        this.uploadSession = uploadSession;
    }

    public void setDomain(String domain) { this.domain = domain; }

    public void setVodSessionKey(String vodSessionKey) { this.vodSessionKey = vodSessionKey; }

    public String getDomain() { return this.domain; }

    public String getVodSessionKey() { return  this.vodSessionKey; }

    public String toRequestString(){
        String requst;
        if(!TextUtils.isEmpty(cverImgType) && !TextUtils.isEmpty(imgFieldId)){
            requst ="&FileName="+fileName
                    +"&FileType="+fileType
                    +"&FileSize="+fileSize
                    +"&CoverImgType="+cverImgType
                    +"&VideoFileId="+ videoFileId
                    +"&ImgFileId="+ imgFieldId
                    +"&UploadSession="+uploadSession;
        }else{
            requst ="&FileName="+fileName
                    +"&FileType="+fileType
                    +"&FileSize="+fileSize
//                    +"&CoverImgType="+cverImgType
                    +"&VideoFileId="+ videoFileId
//                    +"&ImgFileId="+ imgFieldId
                    +"&UploadSession="+uploadSession;
        }

        return requst;
    }
}

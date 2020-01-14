package com.tencent.qcloud.ugckit.module.mixrecord;

import java.util.List;

public class MixRecordConfigBuildInfo {
    private List<String> videoPaths;
    private int recordIndex;
    private int width;
    private int height;
    private int recordRatio;

    public List<String> getVideoPaths() {
        return videoPaths;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRecordRatio() {
        return recordRatio;
    }

    public MixRecordConfigBuildInfo(List<String> videoPaths, int recordIndex, int width, int height, int recordRatio) {
        this.videoPaths = videoPaths;
        this.recordIndex = recordIndex;
        this.width = width;
        this.height = height;
        this.recordRatio = recordRatio;
    }
}

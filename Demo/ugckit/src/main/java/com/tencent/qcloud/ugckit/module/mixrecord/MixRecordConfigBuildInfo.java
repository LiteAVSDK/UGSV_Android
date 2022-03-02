package com.tencent.qcloud.ugckit.module.mixrecord;

import com.tencent.liteav.audio.TXEAudioDef;
import java.util.List;

public class MixRecordConfigBuildInfo {
    private List<String> videoPaths;
    private int          recordIndex;
    private int          width;
    private int          height;
    private int          recordRatio;
    private boolean      isMute;
    private int          aecType;
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

    public boolean isMute() {
        return isMute;
    }

    public int getAecType() {
        return aecType;
    }
    public MixRecordConfigBuildInfo(List<String> videoPaths, int recordIndex, int width, int height, int recordRatio,
         boolean isMute, int aecType) {
        this.videoPaths = videoPaths;
        this.recordIndex = recordIndex;
        this.width = width;
        this.height = height;
        this.recordRatio = recordRatio;
        this.isMute = isMute;
        this.aecType = aecType;
    }

    public MixRecordConfigBuildInfo(List<String> videoPaths, int recordIndex, int width, int height, int recordRatio) {
        this(videoPaths, recordIndex, width, height, recordRatio, true, TXEAudioDef.TXE_AEC_NONE);
    }
}

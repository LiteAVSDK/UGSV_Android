package com.tencent.qcloud.ugckit.module.mixrecord;

public interface IMixRecordJoinListener {

    void onChorusProgress(float progress);

    void onChorusCompleted(String outputPath, boolean success);

}

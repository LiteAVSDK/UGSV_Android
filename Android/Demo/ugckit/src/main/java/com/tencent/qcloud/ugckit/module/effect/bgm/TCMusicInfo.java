package com.tencent.qcloud.ugckit.module.effect.bgm;

import android.support.annotation.Nullable;

public class TCMusicInfo {
    public String name;
    public String url;

    @Nullable
    public String localPath;
    public int status = STATE_UNDOWNLOAD;
    public int progress;

    public static final int STATE_UNDOWNLOAD = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_DOWNLOADED = 3;
    public static final int STATE_USED = 4;
}

package com.tencent.qcloud.ugckit.module.effect.bgm;

import androidx.annotation.Nullable;

public class TCMusicInfo {
    public String name;
    public String url;
    public int    status = STATE_UNDOWNLOAD;
    public int    progress;
    @Nullable
    public String localPath;

    public static final int STATE_UNDOWNLOAD  = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_DOWNLOADED  = 3;
    public static final int STATE_USED        = 4;
}

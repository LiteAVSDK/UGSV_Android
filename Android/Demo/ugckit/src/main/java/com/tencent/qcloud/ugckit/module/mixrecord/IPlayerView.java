package com.tencent.qcloud.ugckit.module.mixrecord;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditConstants;

import java.util.List;

public interface IPlayerView {
    boolean init(int index, String videoPath);

    boolean startVideo();

    void stopVideo();

    void pauseVideo();

    void seekVideo(long timeMs);

    void releaseVideo();

    TXCloudVideoView getVideoView();

    void updateFile(int index,String videoPath);

    List<TXVideoEditConstants.TXAbsoluteRect> getCombineRects(MixRecordConfig config);
}

package com.tencent.qcloud.ugckit.module.cut;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;


import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.module.effect.VideoEditerSDK;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

/**
 * 视频预览播放Layout
 */
public class VideoPlayLayout extends FrameLayout {


    public VideoPlayLayout(@NonNull Context context) {
        super(context);
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 初始化预览播放器
     */
    public void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = this;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        TXVideoEditer videoEditer = VideoEditerSDK.getInstance().getEditer();
        if (videoEditer != null) {
            videoEditer.initWithPreview(param);
        }
    }
}

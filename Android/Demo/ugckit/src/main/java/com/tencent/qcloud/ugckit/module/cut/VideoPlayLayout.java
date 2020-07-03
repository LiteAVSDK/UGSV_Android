package com.tencent.qcloud.ugckit.module.cut;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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

    private FragmentActivity mActivity;
    private FrameLayout mPlayer;

    public VideoPlayLayout(@NonNull Context context) {
        super(context);
        initViews();
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mActivity = (FragmentActivity) getContext();
        inflate(mActivity, R.layout.video_play_layout, this);

        mPlayer = (FrameLayout) findViewById(R.id.layout_player);
    }

    /**
     * 初始化预览播放器
     */
    public void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mPlayer;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        TXVideoEditer videoEditer = VideoEditerSDK.getInstance().getEditer();
        if (videoEditer != null) {
            videoEditer.initWithPreview(param);
        }
    }
}

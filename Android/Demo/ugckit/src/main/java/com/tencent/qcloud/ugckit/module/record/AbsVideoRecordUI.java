package com.tencent.qcloud.ugckit.module.record;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.beauty.constant.BeautyConstants;
import com.tencent.liteav.demo.beauty.model.BeautyInfo;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.qcloud.ugckit.module.effect.bgm.view.SoundEffectsPannel;
import com.tencent.qcloud.ugckit.R;
import com.tencent.qcloud.ugckit.component.TitleBarLayout;

import com.tencent.qcloud.ugckit.module.record.interfaces.IVideoRecordKit;
import com.tencent.rtmp.ui.TXCloudVideoView;

public abstract class AbsVideoRecordUI extends RelativeLayout implements IVideoRecordKit {

    private TitleBarLayout      mTitleBar;
    private TXCloudVideoView    mVideoView;
    private ScrollFilterView    mScrollFilterView;
    private RecordRightLayout   mRecordRightLayout;
    private RecordBottomLayout  mRecordBottomLayout;
    private BeautyPanel         mBeautyPanel;
    private RecordMusicPannel   mRecordMusicPannel;
    private SoundEffectsPannel  mSoundEffectsPannel;
    private ImageSnapShotView   mImageSnapShotView;

    public AbsVideoRecordUI(Context context) {
        super(context);
        initViews();
    }

    public AbsVideoRecordUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public AbsVideoRecordUI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.ugckit_video_rec_layout, this);

        mTitleBar = (TitleBarLayout) findViewById(R.id.titleBar_layout);
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);

        mRecordRightLayout = (RecordRightLayout) findViewById(R.id.record_right_layout);
        mRecordBottomLayout = (RecordBottomLayout) findViewById(R.id.record_bottom_layout);

        mBeautyPanel = (BeautyPanel) findViewById(R.id.beauty_pannel);
        BeautyInfo defaultBeauty = mBeautyPanel.getDefaultBeautyInfo();
        defaultBeauty.setBeautyBg(BeautyConstants.BEAUTY_BG_GRAY);
        mBeautyPanel.setBeautyInfo(defaultBeauty);
        mScrollFilterView = (ScrollFilterView) findViewById(R.id.scrollFilterView);
        mScrollFilterView.setBeautyPannel(mBeautyPanel);

        mRecordMusicPannel = (RecordMusicPannel) findViewById(R.id.record_music_pannel);
        mSoundEffectsPannel = (SoundEffectsPannel) findViewById(R.id.sound_effect_pannel);

        mImageSnapShotView = (ImageSnapShotView) findViewById(R.id.image_snapshot_view);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    public TXCloudVideoView getRecordVideoView() {
        return mVideoView;
    }

    public ScrollFilterView getScrollFilterView() {
        return mScrollFilterView;
    }

    public ImageSnapShotView getSnapshotView() {
        return mImageSnapShotView;
    }

    public RecordRightLayout getRecordRightLayout() {
        return mRecordRightLayout;
    }

    public RecordBottomLayout getRecordBottomLayout() {
        return mRecordBottomLayout;
    }

    public BeautyPanel getBeautyPanel() {
        return mBeautyPanel;
    }

    public RecordMusicPannel getRecordMusicPannel() {
        return mRecordMusicPannel;
    }

    public SoundEffectsPannel getSoundEffectPannel() {
        return mSoundEffectsPannel;
    }

    @Override
    public void disableRecordSpeed() {
        mRecordBottomLayout.disableRecordSpeed();
    }

    @Override
    public void disableTakePhoto() {
        mRecordBottomLayout.disableTakePhoto();
    }

    @Override
    public void disableLongPressRecord() {
        mRecordBottomLayout.disableLongPressRecord();
    }

    @Override
    public void disableRecordMusic() {
        mRecordRightLayout.disableRecordMusic();
    }

    @Override
    public void disableRecordSoundEffect() {
        mRecordRightLayout.disableRecordSoundEffect();
    }

    @Override
    public void disableAspect() {
        mRecordRightLayout.disableAspect();
    }

    @Override
    public void disableBeauty() {
        mRecordRightLayout.disableBeauty();
    }

}
